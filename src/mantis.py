from __future__ import annotations
from concurrent.futures import wait, FIRST_COMPLETED
from concurrent.futures import ThreadPoolExecutor, as_completed

from queue import Queue, Empty
import threading

import os
from datetime import datetime

import time
import json
from enum import Enum, auto

from pathlib import Path
from dataclasses import dataclass, field
import argparse
import csv
from pathlib import Path
from typing import List, Optional
from tools.utils import check_java, annotation_error_test, annotation_error_test_all
from tools.excution import preprocess_excuteion, main_excution
from tools.coverage import check_coverage

from agents.gen_enhance import generate_enhance
from agents.gen_fix import generate_fix, postprocess_fix
from agents.gen_test import generate_test, postprocess_tests
from agents.gen_scenario import generate_scenario, postprocess_scenarios
from agents.gen_guide import generate_guide

_log_lock = threading.Lock()

def _now_ts() -> str:
    # [YYYY-MM-DD HH:MM:SS]
    return datetime.now().strftime("[%Y-%m-%d %H:%M:%S]")

def make_log_path(output_dir: Path, project_name: str) -> Path:
    return Path(output_dir) / f"{project_name}_time_logs.txt"

def write_log(log_path: Path, mantis: "MANTIS", msg: str) -> None:
    """
    스레드 세이프 + append + 즉시 flush(+fsync 옵션)로 중단돼도 남게 기록.
    """
    line = f"{_now_ts()} {mantis.class_name}#{mantis.method_name} : {msg}\n"
    with _log_lock:
        log_path.parent.mkdir(parents=True, exist_ok=True)
        with log_path.open("a", encoding="utf-8") as f:
            f.write(line)
            f.flush()
            # 더 강하게 남기고 싶으면 fsync(느려질 수 있음)
            os.fsync(f.fileno())


@dataclass
class MANTIS:
    # 필수 정보
    root_dir: Path
    output_dir: Path 
    project_name: str
    class_name: str  # FQCN 스타일 (예: com.example.Foo)
    method_name : str

    # 추가: preprocess data 경로
    preprocess_data_path: Path

    # 상태값 (초기화)
    current_enhance_count: int = 0
    current_branch_coverage: float = 0.0
    current_line_coverage: float = 0.0
    current_method_coverage: float = 0.0
    bug_fix_count: int = 0

    # 실행 설정(객체가 알고 있으면 편한 값들)
    max_enhance_count: int = field(default=0)
    coverage_threshold: float = field(default=0.0)
    
    finish_flag: bool = field(default=False)

    def __post_init__(self) -> None:
        self.root_dir = Path(self.root_dir)
        self.output_dir = Path(self.output_dir)  
        self.preprocess_data_path = Path(self.preprocess_data_path)

    # -----------------------
    # Scenario 단계
    # -----------------------
    def generate_scenario(self) -> None:
        result = generate_scenario(self)
        
        with open(r"/workspace/MANTIS/check.txt", "w", encoding="utf-8") as f:
            f.write(str(result))
        
        if result == 0:
            raise RuntimeError(f"[ERROR] generate_scenario failed for {self.class_name} {self.method_name}")

    def postprocess_scenario(self) -> None:
        result = postprocess_scenarios(self)
        if result == 0:
            print(f"[ERROR] postprocess_scenarios failed for {self.class_name} {self.method_name}")
            return 0
        else :
            return 1

    # -----------------------
    # Test 단계
    # -----------------------
    def generate_test(self) -> None:
        result = generate_test(self, self.method_name, self.current_enhance_count)
        if result == 0:
            raise RuntimeError(f"[ERROR] generate_test failed for {self.class_name}, {self.method_name}")

    def postprocess_test(self) -> None:
        result = postprocess_tests(self, self.method_name, self.current_enhance_count)
        if result == 0:
            raise RuntimeError(f"[ERROR] postprocess_tests failed for {self.class_name}, {self.method_name}")
        
        result = check_java(self.class_name, [self.method_name], self.project_name, self)
        # 결과 출력 예시
        for method, fixes in result.items():
            for r in fixes:
                if r.error:
                    print(f"[ERROR] {method}: {r.file} -> {r.error}")
                elif r.changed:
                    print(f"[FIXED] {method}: {r.file} -> {r.actions}")
                else:
                    print(f"[OK] {method}: {r.file}")

    # -----------------------
    # Fix 단계
    # -----------------------
    def preprocess_fix(self) -> None:
        result = generate_guide(self, self.method_name, self.current_enhance_count)
        if result == 0:
            raise RuntimeError(f"[ERROR] generate_guide failed for {self.class_name} {self.method_name}")
    def generate_fix(self) -> None:
        result = generate_fix(self, self.method_name,self.current_enhance_count)
        if result == 0:
            raise RuntimeError(f"[ERROR] generate_fix failed for {self.class_name} { self.method_name}")

    def postprocess_fix(self) -> None:
        result = postprocess_fix(self, self.method_name,self.current_enhance_count)
        if result == 0:
            raise RuntimeError(f"[ERROR] postprocess_fix failed for {self.class_name} { self.method_name}")
        
        result = check_java(self.class_name, [self.method_name], self.project_name, self)
        # 결과 출력 예시
        for method, fixes in result.items():
            for r in fixes:
                if r.error:
                    print(f"[ERROR] {method}: {r.file} -> {r.error}")
                elif r.changed:
                    print(f"[FIXED] {method}: {r.file} -> {r.actions}")
                else:
                    print(f"[OK] {method}: {r.file}")

    # -----------------------
    # Enhance 단계
    # -----------------------

    def generate_enhance(self) -> None:
        result = generate_enhance(self)
        if result == 0:
            raise RuntimeError(f"[ERROR] generate_enhance failed for {self.class_name} {self.method_name}")



def parse_fqcn_and_method(raw: str) -> tuple[str, Optional[str]]:
    raw = (raw or "").strip()
    if not raw:
        return "", None

    if "#" in raw:
        cls, mtd = raw.split("#", 1)   # ✅ 첫 # 기준으로만 split
        cls = cls.strip()
        mtd = mtd.strip() or None
        return cls, mtd

    return raw, None
def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="MANTIS runner")

    parser.add_argument("--root-dir", required=True, help="Project root directory")
    parser.add_argument("--project-name", required=True, help="Project name")
    parser.add_argument(
        "--max-enhance-count",
        type=int,
        required=True,
        help="Maximum enhance iterations",
    )
    parser.add_argument(
        "--coverage-threshold",
        type=float,
        required=True,
        help="Coverage threshold",
    )

    # 추가: preprocess data 경로 arg
    parser.add_argument(
        "--preprocess-data-path",
        required=True,
        help="Path to preprocess data directory/file",
    )

    parser.add_argument("--csv-path", required=True, help="Path to CSV containing FQCN")
    parser.add_argument(
        "--class-column",
        default="class_name",
        help="CSV column name that contains FQCN (default: class_name)",
    )
    
    parser.add_argument(
    "--output-dir",
    required=True,
    help="Directory to store outputs/logs",
    )

    return parser.parse_args()


def read_class_names(csv_path: Path, class_column: str) -> List[tuple[str, Optional[str]]]:
    targets: List[tuple[str, Optional[str]]] = []

    with csv_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        if reader.fieldnames is None:
            raise ValueError("CSV has no header row.")
        if class_column not in reader.fieldnames:
            raise ValueError(
                f"CSV missing required column '{class_column}'. "
                f"Available columns: {reader.fieldnames}"
            )

        for row in reader:
            raw = (row.get(class_column) or "").strip()
            if not raw:
                continue

            cls, mtd = parse_fqcn_and_method(raw)
            if cls:
                targets.append((cls, mtd))

    return targets


def build_mantis_instances(
    root_dir: Path,
    output_dir: Path,                  # ✅ 추가
    project_name: str,
    preprocess_data_path: Path,
    max_enhance_count: int,
    coverage_threshold: float,
    targets: List[tuple[str, Optional[str]]],
) -> List[MANTIS]:
    return [
    MANTIS(
        root_dir=root_dir,
        output_dir=output_dir,          # ✅ 추가
        project_name=project_name,
        class_name=cls,
        method_name=mtd,
        preprocess_data_path=preprocess_data_path,
        max_enhance_count=max_enhance_count,
        coverage_threshold=coverage_threshold,
    )
    for (cls, mtd) in targets
    ]


class Stage(Enum):
    # 생성 파트(병렬)
    GEN_SCENARIO = auto()
    GEN_TEST = auto()
    GEN_GUIDE = auto()
    GEN_FIX = auto()
    GEN_ENHANCE = auto()

    # 실행 파트(직렬)
    EXECUTE = auto()

    DONE = auto()

class ExecResult(Enum):
    OK_DONE = auto()          # 끝
    NEED_ENHANCE = auto()     # enhance 필요
    NEED_FIX = auto()         # fix 필요(직렬에서 처리했다면 OK)

@dataclass
class TaskState:
    """클래스(=MANTIS 인스턴스)별 진행 상태"""
    scenario_done: bool = False
    test_done: bool = False
    guide_done: bool = False
    fix_done: bool = False

    # 실행 실패 횟수(혹은 재시도 횟수) 추적
    exec_fail_count: int = 0

    # 현재 단계(디버깅용)
    stage: Stage = Stage.GEN_SCENARIO
    
    enhance_inflight: bool = False   # ✅ 추가


@dataclass
class WorkItem:
    """큐에 넣고 빼는 작업 단위"""
    mantis: "MANTIS"
    stage: Stage
    error: Optional[str] = None
    
# ----------------------------
# "생성" 작업 래퍼들 (병렬)
# ----------------------------
def gen_scenario_job(m: "MANTIS", log_path: Path) -> None:
    write_log(log_path, m, "[INFO] scenario generation START")
    try:
        for i in range(2):
            m.generate_scenario()
            result = m.postprocess_scenario()
            if result != 0:
                write_log(log_path, m, "[INFO] scenario generation END (success)")
                return
        write_log(log_path, m, "[ERROR] scenario generation END (postprocess failed)")
    except Exception as e:
        write_log(log_path, m, f"[ERROR] scenario generation EXCEPTION: {e}")
        raise

def gen_test_job(m: "MANTIS", log_path: Path) -> None:
    write_log(log_path, m, "[INFO] test generation START")
    try:
        m.generate_test()
        m.postprocess_test()
        write_log(log_path, m, "[INFO] test generation END (success)")
    except Exception as e:
        write_log(log_path, m, f"[ERROR] test generation EXCEPTION: {e}")
        raise
    
def gen_enhance_job(m: "MANTIS", log_path: Path) -> None:
    write_log(log_path, m, "[INFO] enhance-scenario generation START")
    try:
        for i in range(2):
            m.generate_enhance()
            result = m.postprocess_scenario()
            if result != 0:
                write_log(log_path, m, "[INFO] enhance-scenario generation END (success)")
                return
        write_log(log_path, m, "[ERROR] enhance-scenario generation END (postprocess failed)")
    except Exception as e:
        write_log(log_path, m, f"[ERROR] enhance-scenario generation EXCEPTION: {e}")
        raise
    
# ----------------------------
# "직렬 실행" 작업(소비자)
# ----------------------------
def execute_serial(m: "MANTIS") -> None:
    """
    직렬 실행에서 해야 하는 것들:
    - 생성된 시나리오/테스트를 실제 프로젝트에 반영/실행
    - 커버리지 측정/판단/강화 루프 제어 등
    실패하면 예외를 던지도록 만들면 큐 처리하기 쉬움.
    """
    if m.current_branch_coverage >= m.coverage_threshold:
        return ExecResult.OK_DONE

    if m.current_enhance_count >= m.max_enhance_count:
        return ExecResult.OK_DONE

    return ExecResult.NEED_ENHANCE



def main() -> None:
    args = parse_args()

    root_dir = Path(args.root_dir).resolve()
    output_dir = Path(args.output_dir).resolve()
    output_dir.mkdir(parents=True, exist_ok=True)  
    csv_path = Path(args.csv_path).resolve()
    preprocess_data_path = Path(args.preprocess_data_path).resolve()

    if not root_dir.exists() or not root_dir.is_dir():
        raise FileNotFoundError(f"[ERROR] root-dir not found or not a directory: {root_dir}")
    if not csv_path.exists() or not csv_path.is_file():
        raise FileNotFoundError(f"[ERROR] csv-path not found or not a file: {csv_path}")
    if not preprocess_data_path.exists():
        raise FileNotFoundError(
            f"[ERROR] preprocess-data-path not found: {preprocess_data_path}"
        )

    targets  = read_class_names(csv_path, args.class_column)

    mantis_instances = build_mantis_instances(
    root_dir=root_dir,
    output_dir=output_dir,             # ✅ 추가
    project_name=args.project_name,
    preprocess_data_path=preprocess_data_path,
    max_enhance_count=args.max_enhance_count,
    coverage_threshold=args.coverage_threshold,
    targets=targets,
    )

    
    

    print(f"[INFO] Loaded {len(mantis_instances)} MANTIS instances.")
    
    log_path = make_log_path(output_dir, args.project_name)
    with _log_lock:
        log_path.parent.mkdir(parents=True, exist_ok=True)
        with log_path.open("a", encoding="utf-8") as f:
            f.write(f"{_now_ts()} [INFO] RUN START project={args.project_name} total={len(mantis_instances)}\n")
            f.flush()
            os.fsync(f.fileno())
        
        
    # 병렬 생성 작업
    # 직렬 실행 대기열
    ready_q: "Queue[WorkItem]" = Queue()
    enhance_request_q: "Queue[MANTIS]" = Queue()

    # 종료 플래그
    stop_event = threading.Event()

    # 상태 관리
    state = {id(m): TaskState() for m in mantis_instances}
    
        # 직렬 소비자(테스트 실행 + 오류수정까지 "직렬" 처리)
    def serial_worker():
        while not stop_event.is_set():
            try:
                item = ready_q.get(timeout=0.2)
            except Empty:
                continue

            m = item.mantis
            st = state[id(m)]

            try:
                if item.stage == Stage.EXECUTE:
                    try :
                        for j in range (3) :
                            preprocess_excuteion(m)
                            try :
                                write_log(log_path, m, "[INFO] EXECUTE START")
                                result = main_excution(m)
                                write_log(log_path, m, "[INFO] EXECUTE END")
                            except Exception as e:
                                print(f"[ERROR] Execution failed for {m.class_name} , {m.method_name} : , {e}")
                                write_log(log_path, m, "[ERROR] EXECUTE END")
                                result = {"found" : False, 'branch_pct' : 0.0, 'line_pct' : 0.0}
                                
                            if result["found"] :
                                write_log(
                                    log_path,
                                    m,
                                    f"[INFO] main_excution TRY {j+1}/3 END (found) "
                                    f"branch={result['branch_pct']:.2f} line={result['line_pct']:.2f}"
                                )
                                #write_log(log_path, m, f"[INFO] main_excution TRY {j+1}/3 END (found) branch= {result['branch_pct']} line={result['line_pct']}")
                                m.current_branch_coverage = result['branch_pct']
                                m.current_line_coverage = result['line_pct']
                                break   
                            else :
                                if j == 2 :
                                    # 3회 시도 후에도 실패 시 에러 처리
                                    print(f"[ERROR] Execution failed for {m.class_name}, {m.method_name} -> Maven execution error after 3 attempts")
                                    for i in range(2) :
                                        try :
                                            result = annotation_error_test(m)   
                                        except Exception as e:
                                            print(f"[ERROR] Annotation error test failed for {m.class_name} , {m.method_name} : , {e}")
                                        try :
                                            write_log(log_path, m, "[INFO] EXECUTE START")
                                            result_exe = main_excution(m)
                                            write_log(log_path, m, "[INFO] EXECUTE END")
                                        except Exception as e:
                                            print(f"[ERROR] Execution failed for {m.class_name} , {m.method_name} : , {e}")
                                            write_log(log_path, m, "[ERROR] EXECUTE END")
                                            result = {"found" : False, 'branch_pct' : 0.0, 'line_pct' : 0.0}    
                                            
                                        if result_exe["found"] :
                                            break
                                    if result_exe["found"] :
                                        write_log(
                                            log_path,
                                            m,
                                            f"[INFO] main_excution TRY {j+1}/3 END (found) "
                                            f"branch={result['branch_pct']:.2f} line={result['line_pct']:.2f}"
                                        )
                                        #write_log(log_path, m, f"[INFO] main_excution TRY {j+1}/3 END (found) branch= {result['branch_pct']} line={result['line_pct']}")
                                        m.current_branch_coverage = result['branch_pct']
                                        m.current_line_coverage = result['line_pct']
                                        break
                                    else :
                                        print(f"[ERROR] Execution failed for {m.class_name}, {m.method_name} -> Maven execution error after annotation attempts")                  
                                        try :
                                            annotation_error_test_all(m)
                                        except Exception as e:
                                            print(f"[ERROR] Annotation error test failed for {m.class_name} , {m.method_name} : , {e}")
                                        break                  
                                        
                                    
                                else :
                                    print(f"[ERROR] Execution failed for {m.class_name}, {m.method_name} -> Maven execution error")
                                    write_log(log_path, m, f"[INFO] FIX START (try={j+1}/3")
                                    try :
                                        m.preprocess_fix()
                                        m.generate_fix()
                                        m.postprocess_fix()
                                        write_log(log_path, m, f"[INFO] FIX END (success)")
                                    except Exception as e:
                                        write_log(log_path, m, f"[ERROR] FIX END (success)")
                                        print(f"[ERROR] Fix process failed for {m.class_name} , {m.method_name} : , {e}")
                         
                    except Exception as e:
                        print(f"[ERROR] Execution failed for {m.class_name} , {m.method_name} : , {e}")
                        
                    if m.current_enhance_count > 0 :        
                        try :       
                            result = check_coverage(m)
                            m.current_branch_coverage = result['branch_pct']
                            m.current_line_coverage = result['line_pct']
                        except Exception as e :
                            print(f"[ERROR] Coverage failed for {m.class_name} , {m.method_name} : , {e}")
                            write_log(log_path, m, "[ERROR] EXECUTE END")
                            
                    result = execute_serial(m)

                    if result == ExecResult.OK_DONE:
                            st.stage = Stage.DONE
                            m.finish_flag = True 
                            # 선택: ready_q에 DONE을 넣을 필요는 없음(상태만 DONE이면 done_count 계산 가능)
                            print(f"[DONE] {m.class_name} {m.method_name} finished execution.")
                            write_log(log_path, m, f"[DONE] END (success)")

                    elif result == ExecResult.NEED_ENHANCE:
                            # enhance는 병렬로 돌릴 거니까 "enhance 요청 큐" 같은 곳으로 넘기거나
                            # 지금 구조면 futures 제출 로직을 여기서 못하니(스레드/락 문제) 아래처럼 요청 큐를 권장
                            m.current_enhance_count += 1
                            enhance_request_q.put(m)
                            # enhance_request_q.put(m)  # 아래 2)에서 설명

                elif item.stage == Stage.DONE:
                    st.stage = Stage.DONE

                else:
                    # 확장 가능
                    pass

            except Exception as e:
                print(f"[ERROR] EXECUTE failed: {m.class_name} -> {e}")
                write_log(log_path, m, f"[ERROR] END (failed) {e}")

            finally:
                ready_q.task_done()
                
    consumer = threading.Thread(target=serial_worker, daemon=True)
    consumer.start()

    max_workers = min(8, len(mantis_instances))

    # Futures 추적
    # 어떤 future가 어떤 (mantis, stage)인지 매핑
    futures = {}

    # 완료 개수 추적(종료 조건)
    done_count = 0
    total = len(mantis_instances)

    # Stage에 enhance가 없어서 새로 추가하는 게 제일 깔끔합니다.
    # 아래 한 줄을 위 Stage enum에 추가하는 걸 추천:
    #   GEN_ENHANCE = auto()
    #
    # 일단 여기서는 "문자열 키"로도 추적 가능하게 작성했습니다.
    class _LocalStage:
        GEN_SCENARIO = "GEN_SCENARIO"
        GEN_TEST = "GEN_TEST"
        GEN_ENHANCE = "GEN_ENHANCE"

    def submit_scenario(ex, m: MANTIS):
        f = ex.submit(gen_scenario_job, m, log_path)
        futures[f] = (m, _LocalStage.GEN_SCENARIO)

    def submit_test(ex, m: MANTIS):
        f = ex.submit(gen_test_job, m, log_path)
        futures[f] = (m, _LocalStage.GEN_TEST)

    def submit_enhance(ex, m: MANTIS):
        f = ex.submit(gen_enhance_job, m, log_path)
        futures[f] = (m, _LocalStage.GEN_ENHANCE)

    
    with ThreadPoolExecutor(max_workers=max_workers) as ex:
        # 1) scenario 병렬 생성 시작
        for m in mantis_instances:
            submit_scenario(ex, m)

        # 2) 완료되는 대로 다음 작업 투입 / 큐 투입 / enhance 재투입
        while done_count < total:
            # as_completed를 매번 걸면 불편하니, "현재 futures 스냅샷"으로 한 번씩 처리
            
            def all_finished():
                return all(m.finish_flag for m in mantis_instances)

            
            while True:
                if all_finished() and not futures and ready_q.empty() and enhance_request_q.empty():
                    break
                time.sleep(0.1)
                try:
                    m_req = enhance_request_q.get_nowait()
                except Empty:
                    break

                st_req = state[id(m_req)]
                if not st_req.enhance_inflight:
                    st_req.enhance_inflight = True
                    submit_enhance(ex, m_req)

                enhance_request_q.task_done()
                
            if not futures:
                done_count = sum(1 for mm in mantis_instances if state[id(mm)].stage == Stage.DONE)
                if done_count >= total:
                    break
                time.sleep(0.05)
                continue

            done, _ = wait(futures.keys(), return_when=FIRST_COMPLETED)
            for fut in done:
                m, stage = futures.pop(fut)
                st = state[id(m)]

                try:
                    fut.result()

                    # --- scenario 완료 -> test 생성 요청을 즉시 병렬 투입
                    if stage == _LocalStage.GEN_SCENARIO:
                        st.scenario_done = True
                        print(f"[OK] scenario generated: {m.class_name}")
                        submit_test(ex, m)  # "scenario 완료 순서대로" test 생성 요청

                    # --- test 완료 -> 직렬 실행 큐로
                    elif stage == _LocalStage.GEN_TEST:
                        st.test_done = True
                        print(f"[OK] test generated: {m.class_name}")
                        ready_q.put(WorkItem(mantis=m, stage=Stage.EXECUTE))

                    # --- enhance 완료 -> 다시 직렬 실행 큐로 복귀
                    elif stage == _LocalStage.GEN_ENHANCE:
                        st.enhance_inflight = False  
                        print(f"[OK] enhance generated: {m.class_name}")
                        submit_test(ex, m)
                except Exception as e:
                    print(f"[ERROR] {stage} failed: {m.class_name} -> {e}")
                    ready_q.put(WorkItem(mantis=m, stage=Stage.EXECUTE))


            # ---- 종료 조건을 “DONE 상태”로 카운팅하고 싶으면 여기서 계산
            done_count = sum(1 for mm in mantis_instances if state[id(mm)].stage == Stage.DONE)

    # 3) 종료 처리
    stop_event.set()
    consumer.join(timeout=2.0)
    print("[INFO] orchestrator finished.")

if __name__ == "__main__":
    main()
