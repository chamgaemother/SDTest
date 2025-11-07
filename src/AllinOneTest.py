from pathlib import Path
import re
from typing import List, Tuple

# ---------- 재사용: prefix·split 로직 ----------

_FILE_RE = re.compile(r'^(.+?)_(.+?)_(\d+)_(\d+)_Test\.java$')
_CLASS_DECL_RE = re.compile(r'public\s+class\s+.+?Test\b')


# ---- 패키지·임포트·클래스 선언 판별용 ----
RE_PACKAGE = re.compile(r'^\s*package\s+')
RE_IMPORT  = re.compile(r'^\s*import\s+')
RE_CLASS   = re.compile(r'\bclass\b')

# ---- "메서드 시그니처 + 여는 {" 패턴 ----
RE_METHOD_HEAD = re.compile(
    r'^\s*(public|protected|private)\s+'        # 가시성
    r'(static\s+)?'                            # static 가능
    r'[\w\<\>\[\]]+\s+'                        # 반환 타입
    r'\w+\s*\([^)]*\)\s*'                      # 메서드명(파라미터)
    r'(throws\s+[^{]+)?\{'                     # throws … {  까지
)


def collect_prefixes(root_dir: str | Path) -> List[str]:
    """Class_Method_Num1_Num2_Test.java  →  Class_Method_Num1  추출"""
    root = Path(root_dir)
    prefixes = dict()
    for p in root.rglob("*.java"):
        m = _FILE_RE.match(p.name)
        if m:
            prefix = f"{m.group(1)}_{m.group(2)}_{m.group(3)}"
            prefixes.setdefault(prefix, None)
    return list(prefixes.keys())


def _split_parts(java_files: List[Path]) -> Tuple[List[str], List[str], List[str]]:
    """package / import / method 세 구역으로 분리해서 반환"""
    package_lines, import_lines = dict(), dict()
    method_lines: List[str] = []
    in_method = False

    for fp in java_files:
        with fp.open(encoding="utf-8") as f:
            for raw in f:
                line = raw.rstrip("\n")
                stripped = line.strip()

                if stripped.startswith("package "):
                    package_lines.setdefault(line, None)
                    continue
                if stripped.startswith("import "):
                    import_lines.setdefault(line, None)
                    continue
                if _CLASS_DECL_RE.search(stripped):
                    continue          # class 선언 skip
                if stripped.startswith("//"):
                    continue          # 한 줄 주석 skip

                if "{" in stripped:
                    in_method = True
                    method_lines.append(line)
                    continue
                if stripped == "}":
                    if in_method:
                        method_lines.append(line)
                        in_method = False
                    # in_method 가 False 면 skip
                    continue

                method_lines.append(line)

    return list(package_lines.keys()), list(import_lines.keys()), method_lines




def split_java_parts(files: List[Path]) -> Tuple[List[str], List[str], List[str]]:
    """
    여러 .java 파일을 읽어
      • package 라인
      • import 라인(중복 제거)
      • 모든 메서드 블록(깊이 2)
    세 구역으로 분리해 반환한다.
    클래스 선언 { … } 및 마지막 } 는 자동으로 제외된다.
    """
    packages, imports = dict(), dict()
    methods: List[str] = []

    depth = 0            # 전체 중괄호 깊이
    capturing = False    # 현재 메서드 블록을 수집 중인가
    buf: List[str] = []  # 어노테이션·시그니처 임시 저장

    for fp in files:
        with fp.open(encoding="utf-8") as fh:
            for raw in fh:
                line   = raw.rstrip("\n")
                strip  = line.strip()

                # ---------- package / import ----------
                if RE_PACKAGE.match(strip):
                    packages.setdefault(line, None)
                    continue
                if RE_IMPORT.match(strip):
                    imports.setdefault(line, None)
                    continue

                # ---------- 한 줄 주석 ----------
                if strip.startswith("//") or strip.startswith("/*") or strip.startswith("*/") or strip.startswith("*"):
                    continue

                # ---------- 깊이 계산 ----------
                opens  = strip.count("{")
                closes = strip.count("}")
                depth_before = depth
                depth += opens - closes

                # ---------- 클래스 선언 ----------
                if depth_before == 0 and RE_CLASS.search(strip):
                    # 클래스 라인은 기록하지 않음
                    continue

                # ---------- 클래스 본문 (depth == 1) ----------
                if depth_before == 1 and not capturing:
                    # 메서드 앞의 어노테이션(@...) 또는 시그니처 일부만 저장
                    if strip.startswith("@") or "(" in strip or buf:
                        buf.append(line)

                    # '{' 가 있으면서 깊이 2 이상으로 진입 = 메서드 시작
                    if depth_before == 1 and depth >= 2:
                        capturing = True
                        methods.extend(buf)
                        if line not in buf:      # buf 에 중복 저장 안 된 경우
                            methods.append(line)
                        buf.clear()
                    continue

                # ---------- 메서드 내부 ----------
                if capturing:
                    methods.append(line)
                    # 메서드 종료: 깊이 2 → 1
                    if depth == 1:
                        capturing = False
                    continue

                # ---------- 클래스 닫는 '}' ----------
                # depth_before==1 & depth==0  → 마지막 '}' 줄 → skip
                if depth_before == 1 and depth == 0:
                    buf.clear()
                    continue
                # 나머지 (예: 필드 선언 등)는 무시

    return (list(packages.keys()),
            list(imports.keys()),
            methods)

def _gather_files(root_dir: Path, prefix: str) -> List[Path]:
    """prefix_숫자_Test.java 에 매칭되는 파일 리스트"""
    pat = re.compile(rf'^{re.escape(prefix)}_\d+_Test\.java$')
    return [p for p in Path(root_dir).rglob("*.java") if pat.match(p.name)]


# ---------- 새 기능: 파일 합치기 & 저장 ----------

def build_combined_test(root_dir: str | Path, prefix: str) -> Path:
    """
    1. root_dir 아래서 같은 prefix를 공유하는 테스트 파일 모음
    2. package / import / method 부분으로 분해
    3. 하나로 합쳐  <prefix>_Test.java  파일을 기존 파일이 있던 폴더에 저장
       - package는 첫 번째 라인만 사용
       - import는 중복 제거 후 알파벳 정렬
    Returns:
        Path 객체 (생성된 파일 경로)
    """
    root_dir = Path(root_dir)
    java_files = _gather_files(root_dir, prefix)
    if not java_files:
        raise FileNotFoundError(f"'{prefix}_*_Test.java' 파일을 찾을 수 없습니다.")

    package_lines, import_lines, method_lines = split_java_parts(java_files)

    package_line = package_lines[0] if package_lines else ""
    import_lines_sorted = sorted(import_lines)

    content: List[str] = []
    if package_line:
        content.append(package_line)
        content.append("")                 # 빈 줄

    if import_lines_sorted:
        content.extend(import_lines_sorted)
        content.append("")

    content.append(f"public class {prefix}_Test " + "{")
    content.append("")

    content.extend(method_lines)
    if method_lines and method_lines[-1].strip():
        content.append("")                 # 마지막 줄이 코드라면 빈 줄 하나 추가

    content.append("}")                    # 클래스 닫기
    content.append("")                     # EOF newline

    # 새 파일은 첫 번째 원본과 같은 디렉터리에 저장
    dest_dir = java_files[0].parent
    dest_path = dest_dir / f"{prefix}_Test.java"
    dest_path.write_text("\n".join(content), encoding="utf-8")

    return dest_path


def main() :
    ROOT = r"./result"
    for pf in collect_prefixes(ROOT):
        out = build_combined_test(ROOT, pf)
        print(f"✔  {out.relative_to(ROOT)} 생성 완료")

# ------------- 간단 사용 예 -------------
if __name__ == "__main__":
    ROOT = r"./result"
    for pf in collect_prefixes(ROOT):
        out = build_combined_test(ROOT, pf)
        print(f"✔  {out.relative_to(ROOT)} 생성 완료")
