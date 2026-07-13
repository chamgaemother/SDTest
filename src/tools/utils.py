from __future__ import annotations

import os
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple
from pathlib import Path
from agents.agent_utils import extract_maven_file_errors
# -----------------------------
# Regex helpers
# -----------------------------
RE_PACKAGE = re.compile(r"^\s*package\s+([a-zA-Z_][\w\.]*)\s*;\s*$", re.MULTILINE)

# top-level public class/interface/enum/record or non-public
# group(1): "public " (optional)
# group(2): type keyword
# group(3): name
RE_TOPLEVEL_TYPE = re.compile(
    r"(^|\n)\s*"
    r"(public\s+)?"
    r"(final\s+|abstract\s+|sealed\s+|non-sealed\s+)?"
    r"(class|interface|enum|record)\s+"
    r"([A-Za-z_][A-Za-z0-9_]*)",
    re.MULTILINE,
)

RE_COMMENT_BLOCK = re.compile(r"/\*.*?\*/", re.DOTALL)
RE_COMMENT_LINE = re.compile(r"//.*?$", re.MULTILINE)


def _safe_read_text(path: Path, encodings=("utf-8", "utf-8-sig", "cp949")) -> str:
    last_err = None
    for enc in encodings:
        try:
            return path.read_text(encoding=enc)
        except Exception as e:
            last_err = e
    raise last_err


def _safe_write_text(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="\n")


def _strip_comments_for_search(src: str) -> str:
    # search-only: strip comments to avoid matching commented code
    src = RE_COMMENT_BLOCK.sub("", src)
    src = RE_COMMENT_LINE.sub("", src)
    return src


def _ensure_dir(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def _class_short(fqcn: str) -> str:
    return fqcn.split(".")[-1]


def _expected_package(fqcn: str) -> str:
    parts = fqcn.split(".")
    return ".".join(parts[:-1]) if len(parts) > 1 else ""


def _find_package_decl(src: str) -> Optional[Tuple[str, int, int]]:
    """
    Returns (package_name, start_idx, end_idx) of the matched package line, if exists.
    """
    m = RE_PACKAGE.search(src)
    if not m:
        return None
    return m.group(1), m.start(), m.end()


def _ensure_package(src: str, expected_pkg: str) -> Tuple[str, bool]:
    """
    Ensure package declaration exists and matches expected_pkg.
    - If expected_pkg == "" (default package), remove any existing package decl (rare but safe).
    Returns (new_src, changed)
    """
    if expected_pkg is None:
        expected_pkg = ""

    found = _find_package_decl(src)

    if expected_pkg == "":
        # default package: remove any existing package line
        if found:
            _, s, e = found
            new_src = src[:s] + src[e:]
            return new_src.lstrip("\n"), True
        return src, False

    pkg_line = f"package {expected_pkg};"

    if found:
        cur_pkg, s, e = found
        if cur_pkg == expected_pkg:
            return src, False
        # replace only the package line
        new_src = src[:s] + pkg_line + src[e:]
        return new_src, True

    # no package decl -> insert after any leading comments/shebang-ish whitespace
    # typical: license block at top, then package
    insertion_point = 0

    # If file starts with BOM/whitespace/newlines, keep them
    # If it starts with /* ... */ license, insert after it
    m_block = re.match(r"^\s*/\*.*?\*/\s*", src, flags=re.DOTALL)
    if m_block:
        insertion_point = m_block.end()
    else:
        # if it starts with // comment lines, include them
        m_lines = re.match(r"^(\s*//.*\n)+\s*", src, flags=re.MULTILINE)
        if m_lines:
            insertion_point = m_lines.end()

    prefix = src[:insertion_point]
    suffix = src[insertion_point:]
    # ensure a blank line after package decl for readability
    new_src = prefix + pkg_line + "\n\n" + suffix.lstrip("\n")
    return new_src, True


def _find_first_toplevel_type(src: str) -> Optional[Tuple[re.Match, str]]:
    """
    Find the first top-level type declaration (class/interface/enum/record) in the source.
    Returns (match, original_matched_text_context).
    Uses comment-stripped src for search, but match indices must apply to original string,
    so we search on original while being careful.
    """
    # We keep it simple: search on original; this can be fooled by comments,
    # but in tests it's rare. If you want stricter, we can map indices, but it's heavier.
    m = RE_TOPLEVEL_TYPE.search(src)
    if not m:
        return None
    return m, src[m.start():m.end()]


def _ensure_public_and_rename_class(src: str, desired_class_name: str) -> Tuple[str, bool, Optional[str], Optional[str]]:
    """
    Ensure the first top-level class is 'public' and named desired_class_name.
    Returns (new_src, changed, old_name, new_name)
    """
    found = _find_first_toplevel_type(src)
    if not found:
        return src, False, None, None

    m, _ = found
    public_kw = m.group(2)  # "public " or None
    type_kw = m.group(4)    # class/interface/enum/record
    name = m.group(5)

    changed = False
    old_name = name
    new_name = name

    # build replacement header
    # We will replace only "public? modifiers? type name" part preserving modifiers beyond the ones we capture?
    # Current regex captures: public?, (final|abstract|sealed|non-sealed)?, type, name
    # We'll reconstruct as: "public " + (modifier?) + type + desired_name
    modifier = m.group(3) or ""
    modifier = modifier.strip()
    modifier_part = (modifier + " ") if modifier else ""

    # enforce public
    desired_public = "public "

    # enforce name
    new_name = desired_class_name

    # create replacement string for the matched portion (not including group(1) newline prefix)
    # m.group(1) is (^|\n) but included in match; keep it by using span replace carefully.
    prefix = m.group(1)  # "" or "\n"
    replacement = f"{prefix}{desired_public}{modifier_part}{type_kw} {new_name}"

    if public_kw is None or name != desired_class_name:
        src = src[:m.start()] + replacement + src[m.end():]
        changed = True

    return src, changed, old_name, new_name


@dataclass
class FileFixResult:
    file: str
    changed: bool
    actions: List[str]
    error: Optional[str] = None


def check_java(class_name: str, method_list: List[str], library_name: str, mantis_instance) -> Dict[str, List[FileFixResult]]:
    """
    For each method in method_list, scan:
      {Path(mantis_instance.output_dir)}/{library_name}/{class_short}/test_output/{method_name}/*.java

    Apply:
      1) public class name == filename stem; if mismatch -> rename class to filename stem
      2) if top-level type is not public -> add public
      3) package decl must equal expected package; if missing -> add; if different -> replace

    Returns dict: {method_name: [FileFixResult, ...]}
    """
    results: Dict[str, List[FileFixResult]] = {}
    class_short = _class_short(class_name)
    expected_pkg = _expected_package(class_name)

    base_root = Path(f"{Path(mantis_instance.output_dir)}") / library_name / class_short / "test_output"

    for method_name in method_list:
        method_dir = base_root / method_name
        method_key_results: List[FileFixResult] = []
        results[method_name] = method_key_results

        if not method_dir.exists() or not method_dir.is_dir():
            method_key_results.append(
                FileFixResult(
                    file=str(method_dir),
                    changed=False,
                    actions=[],
                    error=f"Method directory not found: {method_dir}",
                )
            )
            continue

        java_files = sorted(method_dir.glob("*.java"))
        for java_path in java_files:
            actions: List[str] = []
            try:
                original = _safe_read_text(java_path)
                src = original
                changed = False

                # (3) package sync
                src2, pkg_changed = _ensure_package(src, expected_pkg)
                if pkg_changed:
                    changed = True
                    src = src2
                    actions.append(f"package -> '{expected_pkg or '(default)'}'")

                # (1) & (2) public + class name must match filename stem
                desired_class = java_path.stem  # filename without .java
                src2, cls_changed, old_name, new_name = _ensure_public_and_rename_class(src, desired_class)
                if cls_changed:
                    changed = True
                    src = src2
                    if old_name and new_name and old_name != new_name:
                        actions.append(f"class rename: {old_name} -> {new_name}")
                    # public ensured (even if only public added)
                    actions.append("ensure public top-level type")

                # write back if changed
                if changed and src != original:
                    # optional backup
                    backup_path = java_path.with_suffix(java_path.suffix + ".bak")
                    if not backup_path.exists():
                        _safe_write_text(backup_path, original)
                    _safe_write_text(java_path, src)

                method_key_results.append(
                    FileFixResult(
                        file=str(java_path),
                        changed=changed,
                        actions=actions,
                    )
                )

            except Exception as e:
                method_key_results.append(
                    FileFixResult(
                        file=str(java_path),
                        changed=False,
                        actions=actions,
                        error=str(e),
                    )
                )

    return results

def comment_out_error_blocks(file_path, error_lines, method_blocks):
    """
    file_path: 수정할 java 파일 경로 (Path or str)
    error_lines: 에러가 발생한 라인 번호 리스트
    method_blocks: [[start_line, end_line], ...] 테스트 메서드 라인 범위 리스트

    에러 라인이 포함된 메서드 블록을 주석 처리 (블록 주석 /* ... */)
    """
    path = Path(file_path)
    lines = path.read_text(encoding='utf-8').splitlines()

    # 에러 라인이 포함된 블록 인덱스 찾기
    blocks_to_comment = []
    for idx, (start, end) in enumerate(method_blocks):
        for err_line in error_lines:
            if start <= err_line <= end:
                blocks_to_comment.append(idx)
                break

    #print(blocks_to_comment)
    for idx in blocks_to_comment:
        start, end = method_blocks[idx]
        
        for i in range(start - 1, end):
            if not lines[i].lstrip().startswith("//"):
                lines[i] = "// " + lines[i]

    # 결과 저장 또는 출력
    new_code = "\n".join(lines)
    return new_code

def extract_test_method_line_blocks(file_lines):
    """
    file_lines: 파일 내용을 라인별로 나눈 리스트
    return: [[start_line, end_line], ...] (1-based line numbering)
    """
    blocks = []
    pattern_test_anno = re.compile(r'^\s*@Test\b')
    pattern_method_decl = re.compile(r'^\s*(public\s+)?void\s+\w+\s*\([^)]*\)\s*(throws\s+\w+(<[^>]+>)?(\s*,\s*\w+(<[^>]+>)?)*)?\s*\{')

    i = 0
    n = len(file_lines)
    while i < n:
        #print(f"라인 {i}: {file_lines[i]}")
        if pattern_test_anno.match(file_lines[i]):
            # 다음 줄부터 메서드 선언 찾기 (한두 줄 안에 있을 거라 가정)
            j = i + 1
            while j < n and not pattern_method_decl.match(file_lines[j]):
                j += 1

            if j == n:
                print(f"@Test 발견: {i}, 메서드 선언 못 찾고 j == {j}")
                # 메서드 선언 못 찾음
                i = j
                continue

            start_line = j -1 # 1-based 라인번호
            brace_count = 0
            # 현재 줄부터 중괄호 열고 닫히는 지점 찾기
            for k in range(j, n):
                brace_count += file_lines[k].count('{')
                brace_count -= file_lines[k].count('}')
                if brace_count == 0:
                    end_line = k + 1
                    blocks.append([start_line, end_line])
                    i = k
                    break
        i += 1

    return blocks
def parse_error_lines(error_log_lines):
    """
    error_log_lines: 에러 로그 문자열 리스트
    return: 에러가 발생한 라인 번호 리스트(int)
    """
    error_line_numbers = []
    pattern = re.compile(r'\[(\d+),\d+\]')  # [filepath:[line,col]]

    for line in error_log_lines:
        if 'package' in line.lower():
            continue
        m = pattern.search(line)
        if m:
            line_num = int(m.group(1))
            error_line_numbers.append(line_num)

    return error_line_numbers

def annotation_error_test (mantis_instance) -> int :
    _method_name = mantis_instance.method_name
    class_name = mantis_instance.class_name.split('.')[-1]
    error_file_path = os.path.join(f"{Path(mantis_instance.output_dir)}/{mantis_instance.project_name}/{mantis_instance.class_name.split('.')[-1]}/execution_logs", rf"{class_name}_{_method_name}_outLog.txt")
    # test_file_path = os.path.join(f"{mantis_instance.root_dir}", rf"MANTIS-tests/{'/'.join(mantis_instance.class_name.split('.')[:-1])}/{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java")
    pkg_path  = os.path.join(*mantis_instance.class_name.split(".")[:-1])
    test_file_path = os.path.join(
        str(mantis_instance.root_dir),
        "MANTIS-tests",
        pkg_path,
        f"{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java",
    )
    
    if not os.path.exists(error_file_path):
            print(f"-- 오류 파일 없음: {error_file_path}")
            return False
        
    with open(error_file_path, "r", encoding="utf-8") as pf:
        out_Msg = pf.read()
        error_msg = extract_maven_file_errors(out_Msg)
        
    print(test_file_path)
        
    if not os.path.exists(test_file_path):
        print(f"❌ 주석 처리할 파일이 존재하지 않습니다: {test_file_path}")
        return False
    else:
        path = Path(test_file_path)
        lines = path.read_text(encoding='utf-8').splitlines()
        method_blocks = extract_test_method_line_blocks(lines)
        #print(f"테스트 메서드 블록: {method_blocks}")
        error_lines = parse_error_lines(error_msg)
        #print(f"에러 라인: {error_lines}")

        result_code = comment_out_error_blocks(test_file_path, error_lines, method_blocks)

        # 결과 저장
        path.write_text(result_code, encoding='utf-8')
        print(f"✅ 주석 처리 완료: {test_file_path}")
        return True
        
def annotation_error_test_all (mantis_instance) -> None :
    class_name = mantis_instance.class_name.split('.')[-1]
    _method_name = mantis_instance.method_name
    test_file_path = os.path.join(f"{mantis_instance.root_dir}", rf"MANTIS-tests/{'/'.join(mantis_instance.class_name.split('.')[:-1])}/{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java")
       
    path = Path(test_file_path) 
    if not path.exists():
        print(f"❌ 주석 처리할 파일이 존재하지 않습니다: {test_file_path}")
    else:
        
        lines = path.read_text(encoding='utf-8').splitlines()
        result_codes = []

        for line in lines:
            stripped_line = line.lstrip()
            if (stripped_line.startswith("//")):
                result_codes.append(line)
            else:
                result_codes.append(f"// {line}")
        # result_codes.append("}")

        result_code = "\n".join(result_codes)
        path.write_text(result_code, encoding='utf-8')
        print(f"✅ 주석 처리 완료: {test_file_path}")
