from pathlib import Path
from typing import Iterable, Optional
import os
import json
import openai
import datetime
from typing import Any, Dict, Optional,List
import re
from agents.config import Config

client = openai.OpenAI(api_key=Config.get_api_key())

SOURCE_ROOT_CANDIDATES = (
    "src/main/java",
    "src/main",
    "src/java",
)

def fqcn_to_relpath(fqcn: str, ext: str = ".java") -> Path:
    """
    com.example.Foo -> com/example/Foo.java
    (내부 클래스로 com.example.Outer$Inner 같은 형태가 오면 $ 이후는 잘라 Outer로 찾도록 처리)
    """
    fqcn = fqcn.strip()

    # 내부 클래스 표기($)가 있으면 소스 파일은 보통 바깥 클래스로 존재
    if "$" in fqcn:
        fqcn = fqcn.split("$", 1)[0]

    parts = fqcn.split(".")
    if len(parts) < 2:
        # 패키지 없는 단일 클래스명 같은 경우도 처리
        return Path(parts[0] + ext)

    return Path(*parts).with_suffix(ext)


def pick_source_root(
    root_dir: Path,
    candidates: Iterable[str] = SOURCE_ROOT_CANDIDATES,
) -> Optional[Path]:
    """
    후보 경로 중 실제로 존재하는 첫 번째 경로를 반환.
    """
    root_dir = Path(root_dir)
    for c in candidates:
        p = root_dir / c
        if p.exists() and p.is_dir():
            return p
    return None


def resolve_class_file_path(
    root_dir: Path,
    fqcn: str,
    ext: str = ".java",
    candidates: Iterable[str] = SOURCE_ROOT_CANDIDATES,
    fallback_search: bool = True,
) -> Path:
    """
    root_dir 아래에서 소스 루트를 찾고, fqcn을 파일 경로로 변환해 최종 Path 반환.
    - candidates에서 찾으면 바로 사용
    - 없으면(fallback_search=True) root_dir 아래에서 **해당 상대경로로 끝나는 파일**을 찾아봄
    """
    root_dir = Path(root_dir)
    rel = fqcn_to_relpath(fqcn, ext=ext)

    src_root = pick_source_root(root_dir, candidates)
    if src_root is not None:
        return (src_root / rel).resolve()

    if not fallback_search:
        raise FileNotFoundError(
            f"Source root not found under {root_dir}. Tried: {list(candidates)}"
        )

    # 후보 루트가 없으면 프로젝트 구조가 다를 수 있으니 전체에서 찾아보기
    # (큰 프로젝트면 비용이 있으니 필요할 때만)
    pattern = str(rel.as_posix())
    for p in root_dir.rglob(f"*{ext}"):
        # path 끝부분이 com/example/Foo.java 와 일치하면 hit
        try:
            if p.as_posix().endswith(pattern):
                return p.resolve()
        except Exception:
            continue

    raise FileNotFoundError(
        f"[ERROR] Class file not found for fqcn={fqcn}, ext={ext} under root={root_dir}"
    )
    

def find_mantis_test_files(
    root_dir: Path,
    fqcn: str,
    method_name: str,
    current_enhance_count: int,
) -> List[Path]:
    """
    root_dir / MANTIS-tests / <package_path> 아래에서
    <Class>_<method>_<count>_Test.java 파일을 0..current_enhance_count 범위에서 전부 찾아 리스트로 반환.

    예)
      fqcn = "org.apache.commons.codec.digest.Md5Crypt"
      method_name = "md5Crypt"
      current_enhance_count = 3

    찾는 파일명:
      Md5Crypt_md5Crypt_0_Test.java
      Md5Crypt_md5Crypt_1_Test.java
      Md5Crypt_md5Crypt_2_Test.java
      Md5Crypt_md5Crypt_3_Test.java
    """
    root_dir = Path(root_dir)
    fqcn = (fqcn or "").strip().strip('"').strip("'")
    method_name = (method_name or "").strip()

    if not fqcn or "." not in fqcn:
        raise ValueError(f"Invalid fqcn: {fqcn}")
    if not method_name:
        raise ValueError("method_name is empty")
    if current_enhance_count < 0:
        return []

    pkg_parts = fqcn.split(".")[:-1]          # package parts
    class_simple = fqcn.split(".")[-1]        # class name

    base_dir = root_dir / "MANTIS-tests" / Path(*pkg_parts)

    results: List[Path] = []
    for count in range(current_enhance_count):
        filename = f"{class_simple}_{method_name}_{count}_Test.java"
        p = base_dir / filename
        if p.exists() and p.is_file():
            results.append(p.resolve())

    return results

def load_prompt(file_path):
    """프롬프트 파일 로드"""
    if os.path.exists(file_path):
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read().strip()
    return None

def chat_with_openai(model, system_prompt, ai_prompt, user_prompt, _temperature: float = 0.5):
    try:
        messages = []
        if system_prompt and model not in  ["o1-mini", "o3-mini", "o4-mini"]:
            messages.append({"role": "system", "content": system_prompt})
        else:
            messages.append({"role": "user", "content": system_prompt})

        messages.append({"role": "user", "content": user_prompt})

        if model in ["o1", "o1-mini", "o3-mini", "o4-mini"]:
            response = client.chat.completions.create(
                model=model,
                messages=messages,
                max_completion_tokens=Config.get_max_tokens(),
                reasoning_effort="low"
            )
        else:
            response = client.chat.completions.create(
                model=model,
                messages=messages,
                max_tokens=Config.get_max_tokens(),
                temperature=_temperature,   # ✅ 추가
            )

        reply = response.choices[0].message.content
        return response, reply

    except openai.OpenAIError as e:
        return f"[ERROR] OpenAI API 오류 발생: {e}", ""
    
def save_response(response_obj, model, system_prompt, user_prompt, directory: str = "./result", number: int = 0, type:str = "scenario") -> None:
    """AI 응답을 JSON 파일로 저장"""
    now = datetime.datetime.now()
    
    # 저장 디렉토리
    os.makedirs(directory, exist_ok=True)  # 디렉토리가 없으면 생성
    filepath = os.path.join(directory, f"{type}_{number}.json")

    # JSON 형식으로 데이터 저장
    response_data = {
        "info": {
            "model": model,
            "finish_reason": response_obj.choices[0].finish_reason,
            "usage": {
                "completion_tokens": response_obj.usage.completion_tokens,
                "prompt_tokens": response_obj.usage.prompt_tokens,
                "total_tokens": response_obj.usage.total_tokens,
                "completion_tokens_details": {
                    "accepted_prediction_tokens": response_obj.usage.completion_tokens_details.accepted_prediction_tokens,
                    "audio_tokens": response_obj.usage.completion_tokens_details.audio_tokens,
                    "reasoning_tokens": response_obj.usage.completion_tokens_details.reasoning_tokens,
                    "rejected_prediction_tokens": response_obj.usage.completion_tokens_details.rejected_prediction_tokens
                },
                "prompt_tokens_details": {
                    "audio_tokens": response_obj.usage.prompt_tokens_details.audio_tokens,
                    "cached_tokens": response_obj.usage.prompt_tokens_details.cached_tokens
                }
            }
        },
        "prompt": {
            "system_prompt": system_prompt if system_prompt else "None",
            "user_prompt": user_prompt
        },
        "response": response_obj.choices[0].message.content
    }

    with open(filepath, "w", encoding="utf-8") as file:
        json.dump(response_data, file, indent=4, ensure_ascii=False)

    print(f"[INFO] 응답이 JSON 파일로 저장되었습니다: {filepath}")
    
def _fmt_deps(dep_list):
    # depClasses / depMethods 가 [] 인 경우도 있으니 보기 좋게 처리
    if not dep_list:
        return "[]"
    # 문자열 리스트라고 가정 (혹시 dict면 str로)
    return ", ".join(map(str, dep_list))

def _join_lines(lines):
    # blockList, flowSummary 같이 list[str] → 여러 줄 텍스트로
    if not lines:
        return ""
    return "\n".join(lines)

def _ensure_dir(path: str) -> None:
    os.makedirs(path, exist_ok=True)

def _safe_read_text(path: str, encodings=("utf-8", "utf-8-sig", "cp949")) -> str:
    last_err = None
    for enc in encodings:
        try:
            with open(path, "r", encoding=enc) as f:
                return f.read()
        except Exception as e:
            last_err = e
    raise last_err

def _extract_json_object_substring(s: str) -> Optional[str]:
    if not s:
        return None
    start = s.find("{")
    end = s.rfind("}")
    if start == -1 or end == -1 or end <= start:
        return None
    return s[start:end + 1]

def _loads_json_robust(raw: str) -> Any:
    raw = raw.strip()

    # 1) direct
    try:
        return json.loads(raw)
    except Exception:
        pass

    # 2) try extracting object substring
    extracted = _extract_json_object_substring(raw)
    if extracted:
        try:
            return json.loads(extracted)
        except Exception:
            pass

    # 3) clean common junk chars and retry
    cleaned = raw.replace("\u0000", "").strip()
    extracted2 = _extract_json_object_substring(cleaned)
    if extracted2:
        try:
            return json.loads(extracted2)
        except Exception:
            pass

    raise ValueError("Robust JSON parsing failed")


_ERROR_FILE_LINE_RE = re.compile(
    r'^\[ERROR\]\s+(.+):\[(\d+),(\d+)\]\s+(.+)$'
)
def extract_maven_file_errors(log_text: str) -> List[str]:
    """
    Maven 로그에서 다음 형태만 추출:
    [ERROR] <file>:[line,col] <message>
    """
    results: List[str] = []
    for line in log_text.splitlines():
        line = line.rstrip()
        if _ERROR_FILE_LINE_RE.match(line):
            results.append(line)
    return results