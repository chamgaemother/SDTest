import os
import shutil
import subprocess
import re
import xml.etree.ElementTree as ET
from pathlib import Path

import signal # 추가

TEST_SUMMARY_PATTERN = re.compile(
    r"(?:\[\w+\])?\s*"
    r"Tests run:\s*(\d+),\s*"
    r"Failures:\s*(\d+),\s*"
    r"Errors:\s*(\d+),\s*"
    r"Skipped:\s*(\d+)"
    r"(?:,\s*Time elapsed:\s*([0-9.]+)\s*s)?"
)


def preprocess_excuteion(mantis_instance):
    _method_name = mantis_instance.method_name
    class_name = mantis_instance.class_name.split('.')[-1]
    test_file_path = os.path.join(f"{mantis_instance.root_dir}", f"MANTIS-tests/{'/'.join(mantis_instance.class_name.split('.')[:-1])}/{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java")
    source_file_path = f"{Path(mantis_instance.output_dir)}/{mantis_instance.project_name}/{class_name}/test_output/{_method_name}/{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java"
    # source_file_path = (
    #     Path(mantis_instance.output_dir)
    #     / mantis_instance.project_name
    #     / class_name
    #     / "test_output"
    #     / _method_name
    #     / f"{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java"
    # )
    try:
        os.makedirs(os.path.dirname(test_file_path), exist_ok=True)
        shutil.copy(source_file_path, test_file_path)
        print(f"Preprocessed: {source_file_path} -> {test_file_path}")
    except Exception as e:
        print(f"Error during preprocessing execution: {e}")
    

def main_excution(mantis_instance):
    _method_name = mantis_instance.method_name
    class_name = mantis_instance.class_name.split('.')[-1]
    test_file_path = os.path.join(
        f"{mantis_instance.root_dir}",
        f"MANTIS-tests/{'/'.join(mantis_instance.class_name.split('.')[:-1])}/{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java"
    )
    target_class_name = f"{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test"

    # jdk_home = r"C:\Users\00000\.jdks\ms-11.0.26"  # 삭제: 윈도우 고정 경로
    # jdk_home = r"/usr/lib/jvm/java-11-openjdk-amd64"  # 수정: 리눅스 JDK 11 경로(컨테이너 기준)

    env = os.environ.copy()
    # env["JAVA_HOME"] = jdk_home
    # env["PATH"] = fr"{jdk_home}\bin;{env['PATH']}"  # 삭제: 윈도우 전용 PATH 구분자/슬래시
    # env["PATH"] = str(Path(jdk_home) / "bin") + os.pathsep + env.get("PATH", "")  # 수정: OS 중립 PATH

    mvn_exe = shutil.which("mvn", path=env["PATH"])
    if not mvn_exe:  # 추가: mvn을 못 찾을 때 대비
        mvn_exe = "mvn"

    mvn_cmd = [
        mvn_exe,
        "clean",
        "test",
        f"-Dtest={target_class_name}",
        "-Dmaven.test.failure.ignore=true",
        "-Drat.skip=true",
        "org.jacoco:jacoco-maven-plugin:report"
    ]

    print(f"\n=== Processing {class_name} {_method_name} ===")
    print("[INFO] Project root :", mantis_instance.root_dir)
    print("[INFO] Running Maven:", " ".join(mvn_cmd))

    # 1) Maven 실행
    try:
        # ===== OS별 Popen 옵션 구성 =====
        popen_kwargs = dict(  # 추가
            env=env,
            cwd=mantis_instance.root_dir,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            shell=False,
        )

        if os.name == "nt":  # 추가: Windows
            popen_kwargs["creationflags"] = subprocess.CREATE_NEW_PROCESS_GROUP  # 수정(윈도우에서만)
        else:  # 추가: Linux/Mac
            popen_kwargs["start_new_session"] = True  # 추가: 프로세스 그룹 분리(리눅스 timeout kill용)

        # proc = subprocess.Popen(... creationflags=...)  # 삭제: 윈도우 전용 옵션 고정
        proc = subprocess.Popen(mvn_cmd, **popen_kwargs)  # 수정: OS 분기된 kwargs 사용

        timeout_sec = 150
        try:
            # timeout 내에서 실행 시도
            stdout, stderr = proc.communicate(timeout=timeout_sec)

        except subprocess.TimeoutExpired:
            print(f"[TIMEOUT] Maven execution timed out for {_method_name}. Killing process tree...")

            if os.name == "nt":  # 추가: Windows는 taskkill
                subprocess.run(
                    ["taskkill", "/F", "/T", "/PID", str(proc.pid)],
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.DEVNULL,
                )
            else:  # 추가: Linux/Mac은 killpg
                try:
                    os.killpg(proc.pid, signal.SIGKILL)  # 추가
                except ProcessLookupError:
                    pass

            # 안전하게 communicate 종료 (버퍼 정리)
            try:
                stdout, stderr = proc.communicate(timeout=5)
            except Exception:
                stdout, stderr = "", "[ERROR] Process force-killed due to timeout."

        # CompletedProcess 객체로 결과 일관성 유지
        result = subprocess.CompletedProcess(
            args=mvn_cmd,
            returncode=proc.returncode,
            stdout=stdout,
            stderr=stderr,
        )

    except Exception as e:
        print(f"[ERROR] Maven execution failed for {_method_name}: {e}")
        return None, False

    # 2) stderr / stdout 처리
    stdout_text = result.stdout

    # 로그 폴더
    log_dir = f"{Path(mantis_instance.output_dir)}/{mantis_instance.project_name}/{class_name}/execution_logs"
    os.makedirs(log_dir, exist_ok=True)

    if stdout_text is not None:
        out_filename = f"{class_name}_{_method_name}_outLog.txt"
        out_filepath = os.path.join(log_dir, out_filename)
        with open(out_filepath, "w", encoding="utf-8") as f:
            f.write(stdout_text)
        print(f"** Saved stdout to {out_filepath}")

    # 3) Surefire summary 파싱
    tests_run = failures = errors = skipped = None
    summary_lines = []
    for line in stdout_text.splitlines():
        if "Tests run:" in line:
            summary_lines.append(line)

    if summary_lines:
        match = TEST_SUMMARY_PATTERN.search(summary_lines[-1])
        if match:
            tests_run = int(match.group(1))
            failures = int(match.group(2))
            errors = int(match.group(3))
            skipped = int(match.group(4))
            print(f"[저장된 정보] Tests run: {tests_run}, Failures: {failures}, Errors: {errors}, Skipped: {skipped}")
        else:
            print(f"[ERROR] Could not parse test summary line for {_method_name}")
            return {"found": False, "line_pct": 0.0, "branch_pct": 0.0}
    else:
        print(f"[INFO] No test summary line found for {_method_name}")
        return {"found": False, "line_pct": 0.0, "branch_pct": 0.0}

    # 4) jacoco.xml 파싱
    jacoco_xml_path = os.path.join(
        mantis_instance.root_dir, "target", "site", "jacoco", "jacoco.xml"
    )

    if not os.path.exists(jacoco_xml_path):
        print("[ERROR] jacoco.xml not found:", jacoco_xml_path)
        return {"found": True, "line_pct": 0.0, "branch_pct": 0.0}

    fqcn = (mantis_instance.class_name or "").strip().strip('"').strip("'")
    method_name = (getattr(mantis_instance, "method_name", None) or "").strip()

    if not fqcn or "." not in fqcn or not method_name:
        print(f"[ERROR] Invalid input fqcn/method_name: fqcn={fqcn}, method={method_name}")
        return {"found": True, "line_pct": 0.0, "branch_pct": 0.0}

    # JaCoCo XML은 class를 슬래시 경로로 가짐
    class_path = fqcn.replace(".", "/")

    tree = ET.parse(jacoco_xml_path)
    root = tree.getroot()

    line_missed = line_covered = 0
    branch_missed = branch_covered = 0
    matched_any = False

    for pkg in root.findall("./package"):
        for cls in pkg.findall("./class"):
            cls_name = cls.get("name")
            if cls_name != class_path:
                continue

            for m in cls.findall("./method"):
                if m.get("name") != method_name:
                    continue

                matched_any = True

                for c in m.findall("./counter"):
                    ctype = c.get("type")
                    missed = int(c.get("missed", "0"))
                    covered = int(c.get("covered", "0"))

                    if ctype == "LINE":
                        line_missed += missed
                        line_covered += covered
                    elif ctype == "BRANCH":
                        branch_missed += missed
                        branch_covered += covered

    def pct(missed, covered):
        total = missed + covered
        return (covered / total * 100.0) if total > 0 else 0.0

    if not matched_any:
        print(f"[WARN] No matching method in jacoco.xml: {fqcn}#{method_name}")
        return {"found": True, "line_pct": 0.0, "branch_pct": 0.0}

    line_pct = pct(line_missed, line_covered) or 0.0
    branch_pct = pct(branch_missed, branch_covered) or 0.0

    print(f"[COVERAGE-METHOD] {fqcn}#{method_name}")
    print(f"  - LINE   : missed={line_missed}, covered={line_covered}, pct={line_pct:.2f}%")
    print(f"  - BRANCH : missed={branch_missed}, covered={branch_covered}, pct={branch_pct:.2f}%")

    return {"found": True, "line_pct": line_pct, "branch_pct": branch_pct}


def main_excution_window(mantis_instance):
    _method_name = mantis_instance.method_name
    class_name = mantis_instance.class_name.split('.')[-1]
    test_file_path = os.path.join(f"{mantis_instance.root_dir}", f"MANTIS-tests/{'/'.join(mantis_instance.class_name.split('.')[:-1])}/{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java")
    target_class_name = f"{class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test"
    jdk_home = r"C:\Users\00000\.jdks\ms-11.0.26"
    env = os.environ.copy()
    env["JAVA_HOME"] = jdk_home
    env["PATH"] = fr"{jdk_home}\bin;{env['PATH']}" 
    
    mvn_exe = shutil.which("mvn", path=env["PATH"]) 
    mvn_cmd = [
        mvn_exe,
        "clean",
        "test", 
        f"-Dtest={target_class_name}",
        "-Dmaven.test.failure.ignore=true",
        "-Drat.skip=true",
        "org.jacoco:jacoco-maven-plugin:report"
    ]
    
    print(f"\n=== Processing {class_name} {_method_name} ===")
    print("[INFO] Project root :", mantis_instance.root_dir)
    print("[INFO] Running Maven:", " ".join(mvn_cmd))
    
    # 1) Maven 실행
    try:
        # 새로운 프로세스 그룹으로 mvn 실행 (cmd chain 방지)
        proc = subprocess.Popen(
            mvn_cmd,
            env=env,
            cwd=mantis_instance.root_dir,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            shell=False,
            creationflags=subprocess.CREATE_NEW_PROCESS_GROUP
        )
        timeout_sec = 150
        try:
            # timeout 내에서 실행 시도
            stdout, stderr = proc.communicate(timeout=timeout_sec)

        except subprocess.TimeoutExpired:
            print(f"[TIMEOUT] Maven execution timed out for {_method_name}. Killing process tree...")

            # 1️⃣ taskkill 명령으로 전체 트리 종료 (/T: 자식 포함, /F: 강제)
            subprocess.run(
                ["taskkill", "/F", "/T", "/PID", str(proc.pid)],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
            )

            # 2️⃣ 안전하게 communicate 종료 (버퍼 정리)
            try:
                stdout, stderr = proc.communicate(timeout=5)
            except Exception:
                stdout, stderr = "", "[ERROR] Process force-killed due to timeout."

        # 3️⃣ CompletedProcess 객체로 결과 일관성 유지
        result = subprocess.CompletedProcess(
            args=mvn_cmd,
            returncode=proc.returncode,
            stdout=stdout,
            stderr=stderr,
        )


    except Exception as e:
        print(f"[ERROR] Maven execution failed for {_method_name}: {e}")
        return None, False
    
    # 2) stderr / stdout 처리
    stdout_text = result.stdout
    # 로그 폴더
    log_dir = f"{Path(mantis_instance.output_dir)}/{mantis_instance.project_name}/{class_name}/execution_logs"
    os.makedirs(log_dir, exist_ok=True)
    
    if stdout_text != None: 
        # print(stdout_text)  # 정상 로그 출력
        # 정상 로그 저장
        out_filename = f"{class_name}_{_method_name}_outLog.txt"
        out_filepath = os.path.join(log_dir, out_filename)
        with open(out_filepath, "w", encoding="utf-8") as f:
            f.write(stdout_text)
        print(f"** Saved stdout to {out_filepath}")
        
    # 3) Surefire summary 파싱
    tests_run = failures = errors = skipped = None
    summary_lines = []
    for line in stdout_text.splitlines():
        if "Tests run:" in line:
            summary_lines.append(line)

    if summary_lines:
        match = TEST_SUMMARY_PATTERN.search(summary_lines[-1])
        if match:
            tests_run = int(match.group(1))
            failures = int(match.group(2))
            errors = int(match.group(3))
            skipped = int(match.group(4))
            print(f"[저장된 정보] Tests run: {tests_run}, Failures: {failures}, Errors: {errors}, Skipped: {skipped}")

        else :
            print(f"[ERROR] Could not parse test summary line for {_method_name}")
            return {"found": False}
    else:
        print(f"[INFO] No test summary line found for {_method_name}")
        return {"found": False}
    
    # 4) jacoco.csv 파싱
    jacoco_xml_path = os.path.join(
        mantis_instance.root_dir, "target", "site", "jacoco", "jacoco.xml"
    )

    if not os.path.exists(jacoco_xml_path):
        print("[ERROR] jacoco.xml not found:", jacoco_xml_path)
        return {"found": False, "line_pct": None, "branch_pct": None}

    fqcn = (mantis_instance.class_name or "").strip().strip('"').strip("'")
    method_name = (getattr(mantis_instance, "method_name", None) or "").strip()

    if not fqcn or "." not in fqcn or not method_name:
        print(f"[ERROR] Invalid input fqcn/method_name: fqcn={fqcn}, method={method_name}")
        return {"found": False, "line_pct": None, "branch_pct": None}

    # JaCoCo XML은 class를 슬래시 경로로 가짐: org/apache/.../Md5Crypt
    class_path = fqcn.replace(".", "/")

    tree = ET.parse(jacoco_xml_path)
    root = tree.getroot()

    # method counter 합산
    line_missed = line_covered = 0
    branch_missed = branch_covered = 0
    matched_any = False

    # 모든 package/class/method를 훑되, class가 일치하는 곳만
    for pkg in root.findall("./package"):
        for cls in pkg.findall("./class"):
            cls_name = cls.get("name")  # e.g. org/apache/.../Md5Crypt
            if cls_name != class_path:
                # 내부/중첩 클래스 보정이 필요하면 여기서 추가 가능
                continue

            # class 내 method들
            for m in cls.findall("./method"):
                if m.get("name") != method_name:
                    continue

                matched_any = True

                # method 내 counter들에서 LINE/BRANCH만 합산
                for c in m.findall("./counter"):
                    ctype = c.get("type")
                    missed = int(c.get("missed", "0"))
                    covered = int(c.get("covered", "0"))

                    if ctype == "LINE":
                        line_missed += missed
                        line_covered += covered
                    elif ctype == "BRANCH":
                        branch_missed += missed
                        branch_covered += covered

    def pct(missed, covered):
        total = missed + covered
        return (covered / total * 100.0) if total > 0 else 0.0

    if not matched_any:
        print(f"[WARN] No matching method in jacoco.xml: {fqcn}#{method_name}")
        return {"found": False, "line_pct": None, "branch_pct": None}

    line_pct = pct(line_missed, line_covered)
    branch_pct = pct(branch_missed, branch_covered)

    print(f"[COVERAGE-METHOD] {fqcn}#{method_name}")
    print(f"  - LINE   : missed={line_missed}, covered={line_covered}, pct={line_pct:.2f}%")
    print(f"  - BRANCH : missed={branch_missed}, covered={branch_covered}, pct={branch_pct:.2f}%")

    return {"found": True, "line_pct": line_pct, "branch_pct": branch_pct}
