import os
import json
from pathlib import Path
from agents.config import Config
import re

from agents.agent_utils import load_prompt, chat_with_openai, resolve_class_file_path, save_response, _ensure_dir, _safe_read_text, _fmt_deps, _join_lines, _loads_json_robust


SYSTEM_PROMPT_PATH = "./prompt/gen_scenario/system.txt"
USER_PROMPT_PATH = "./prompt/gen_scenario/user.txt"
AI_PROMPT_PATH = "./prompt/gen_scenario/assistant.txt"




def _safe_filename(name: str) -> str:
    """
    Windows/Unix 모두에서 안전한 파일명으로 변환.
    - 공백/특수문자 정리
    - 길이 제한
    """
    # Windows forbidden: \ / : * ? " < > |  + control chars
    name = re.sub(r'[\\/:*?"<>|\x00-\x1f]', "_", name)
    name = name.strip().strip(".")
    if not name:
        name = "method"
    return name[:180]  # 너무 긴 이름 방지


def _chunk_list(items: list, chunk_size: int) -> list[list]:
    if chunk_size <= 0:
        return [items]
    return [items[i:i + chunk_size] for i in range(0, len(items), chunk_size)]

def generate_scenario(mantis_instance) -> None:
    print(f"[INFO] {mantis_instance.class_name} {mantis_instance.method_name} 시나리오 생성 시작")
    
    model_choice = '6' #o4-mini
    if model_choice in Config.MODEL_MAP:
            model = Config.MODEL_MAP[model_choice]
            print(f"[INFO] {mantis_instance.class_name} 모델 `{model}` 선택됨.\n")
    else:
        print(f"[ERROR] {mantis_instance.class_name} {mantis_instance.method_name} 올바르지 않은 모델 선택입니다.")
        return 0
    
    system_prompt = load_prompt(SYSTEM_PROMPT_PATH) 
    ai_prompt = load_prompt(AI_PROMPT_PATH) 
    user_prompt = load_prompt(USER_PROMPT_PATH)
    json_file = Path(mantis_instance.preprocess_data_path)
    
    if not user_prompt or not system_prompt:
        print(f"[ERROR] {mantis_instance.class_name} {mantis_instance.method_name} 프롬프트 파일이 비어있거나 없습니다. 프롬프트 파일을 확인하세요.")
        return 0
    
    try:
        with json_file.open(encoding="utf-8") as f:
            data = json.load(f)            # → Python list[ dict ]
    except Exception as e:
        print(f"[ERROR] {mantis_instance.class_name} {mantis_instance.method_name} JSON 파일을 여는 중 오류 발생: {e}")
        return 0
    
    print(f"[INFO] {mantis_instance.class_name} {mantis_instance.method_name} 프롬프트 설정 완료!")
    
    file_path = resolve_class_file_path(
        root_dir=mantis_instance.root_dir,
        fqcn=mantis_instance.class_name,
        ext=".java",  # 필요하면 ".kt" 등
    )
    if not file_path:
        print(f"[ERROR] {mantis_instance.class_name} {mantis_instance.method_name} 클래스 파일을 찾을 수 없습니다: {mantis_instance.class_name}")
        return 0
    if os.path.exists(file_path):
        # Java 소스 코드 읽기
        with open(file_path, "r", encoding="utf-8") as code_file:
            original_code = code_file.read()
    else: 
        return file_path
        
    library_name = mantis_instance.project_name
    class_name = mantis_instance.class_name
    target = [item for item in data if item.get("clazz") == class_name and item.get("methodName") == mantis_instance.method_name]
    # 메서드별 프롬프트 삽입용 텍스트 블록 리스트
    method_infomation_list = []
    seen = set()  # (methodName, signature) 기준 중복 제거

    for item in target:
        method_name = item.get("methodName", "")
        signature = item.get("signature", "")
        dedup_key = (method_name, signature)
        if dedup_key in seen:
            continue
        seen.add(dedup_key)

        visibility = item.get("visibility", "unknown")
        method_body = item.get("body", "unknown")
        nodes = item.get("nodes", "unknown")
        edges = item.get("edges", "unknown")
        cc = item.get("cc", "unknown")

        dep_classes = _fmt_deps(item.get("depClasses"))
        dep_methods = _fmt_deps(item.get("depMethods"))

        block_list_text = _join_lines(item.get("blockList"))
        block_edges_text = _join_lines(item.get("blockEdges"))
        flow_summary_text = _join_lines(item.get("flowSummary"))

        # ★ 요청하신 형식 그대로 생성 (형식/줄바꿈 유의)
        method_block = (
            f"====={signature}=====\n"
            f"- Visibility → {visibility}\n"
            # f"- Method Body → {method_body}\n"
            f"- CFG infomation → N : {nodes}, E : {edges}, CC : {cc}\n"
            f"- Dependencies → {dep_classes} {dep_methods}\n"
            f"[FULL CFG]\n"
            f"=== BLOCK LIST ===\n"
            f"{block_list_text}\n"
            f"=== BLOCK EDGES ===\n"
            f"{block_edges_text}\n\n"
            f"[DECISION FLOW SUMMARY]\n"
            f"{flow_summary_text}"
        )

        method_infomation_list.append(method_block)

    # 필요하면 한 번에 합쳐서 프롬프트에 넣기 좋게:
    method_infomation_prompt = "\n\n".join(method_infomation_list)
        
    # user_prompt에서 필요한 부분들을 순차적으로 대체
    user_prompt_modified = user_prompt.replace("{ LIBRARY_NAME }", library_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_NAME }", class_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_BODY }", original_code)
    user_prompt_modified = user_prompt_modified.replace("{ METHOD_NAME }", mantis_instance.method_name)
    user_prompt_modified = user_prompt_modified.replace("{ METHOD_INFOMATIONs }", method_infomation_prompt)

    print(f"[INFO] '\n{file_path}' 파일의 코드를 {model} 모델로 시나리오 생성 처리 중...")
        
    try:
        chain_response_obj, chain_response_text = chat_with_openai(
            model, system_prompt, ai_prompt, user_prompt_modified
        )
    except Exception as e:
        print(f"[ERROR] {mantis_instance.class_name} {mantis_instance.method_name} chat_with_openai 호출 중 예외 발생: {e}")
       
        
    if chain_response_obj is None:
        print(f"[ERROR] {mantis_instance.class_name} {mantis_instance.method_name} OpenAI 응답이 없습니다.")
       
    if chain_response_obj:
        save_response(chain_response_obj, model, system_prompt, user_prompt_modified, directory=f"{Path(mantis_instance.output_dir)}/{library_name}/{mantis_instance.class_name.split('.')[-1]}/scenario_output/{mantis_instance.method_name}", number=0, type="scenario")
        print(f"\n[INFO] {mantis_instance.class_name} {mantis_instance.method_name} 시나리오 생성이 완료되었습니다!")


                        
def postprocess_scenarios(mantis_instance) -> None:
    print(f"[INFO] {mantis_instance.class_name} {mantis_instance.method_name} 시나리오 후처리 시작")

    base_dir = f"{Path(mantis_instance.output_dir)}/{mantis_instance.project_name}/{mantis_instance.class_name.split('.')[-1]}/scenario_output/{mantis_instance.method_name}"
    _ensure_dir(base_dir)

    # 기존 생성 파일
    scenario_file_path = f"{base_dir}/scenario_{mantis_instance.current_enhance_count}.json"
    if not os.path.exists(scenario_file_path):
        raise FileNotFoundError(f"[ERROR] Scenario file not found: {scenario_file_path}")

    raw_text = _safe_read_text(scenario_file_path)

    # 1) wrapper JSON 파싱
    try:
        wrapper_obj = _loads_json_robust(raw_text)
    except Exception as e:
        debug_path = f"{base_dir}/scenario_{mantis_instance.current_enhance_count}_raw_wrapper.txt"
        with open(debug_path, "w", encoding="utf-8") as f:
            f.write(raw_text)
        if response_raw == "":
            return 0
        else :
            raise RuntimeError(f"[ERROR] Failed to parse wrapper JSON. Saved raw to {debug_path}") from e

    # 2) wrapper에서 response 꺼내기
    if isinstance(wrapper_obj, dict) and "response" in wrapper_obj:
        response_payload = wrapper_obj["response"]
    else:
        response_payload = wrapper_obj

    # 3) response_payload가 문자열이면 내부 JSON 파싱
    if isinstance(response_payload, str):
        response_raw = response_payload.strip()
        try:
            response_obj = _loads_json_robust(response_raw)
        except Exception as e:
            if response_raw == "" or response_raw.lower() == "null":
                return 0
            else :
                debug_path = f"{base_dir}/{mantis_instance.method_name}_{mantis_instance.current_enhance_count}.json"
                with open(debug_path, "w", encoding="utf-8") as f:
                    f.write(response_raw)
                return 1

            raise RuntimeError(f"[ERROR] Failed to parse response JSON. Saved raw to {debug_path}") from e
    elif isinstance(response_payload, dict):
        response_obj = response_payload
    else:
        raise TypeError(f"[ERROR] Unexpected type for response payload: {type(response_payload)}")

    # 4) scenarios 추출
    if not (isinstance(response_obj, dict) and "scenarios" in response_obj):
        debug_path = f"{base_dir}/scenario_{mantis_instance.current_enhance_count}_response_obj.json"
        with open(debug_path, "w", encoding="utf-8") as f:
            json.dump(response_obj, f, ensure_ascii=False, indent=2)
        raise KeyError(f"[ERROR] 'scenarios' key not found in response_obj. Dumped to {debug_path}")

    scenarios = response_obj["scenarios"]

    # (A) 구버전: scenarios가 리스트인 경우 → 한 파일로 저장
    if not isinstance(scenarios, dict):
        class_short = mantis_instance.class_name.split(".")[-1]
        out_path = f"{base_dir}/{mantis_instance.method_name}_{mantis_instance.current_enhance_count}.json"
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump({"scenarios": scenarios}, f, ensure_ascii=False, indent=2)
        print(f"[WARN] scenarios is not a dict. Saved as single file: {out_path}")
        print(f"[INFO] {mantis_instance.class_name} 시나리오 후처리가 완료되었습니다!")
        return

    # ✅ 여기부터: 메서드명 기준으로 {method_name}_{k}.json 저장
    # 옵션: 파일이 너무 커지는 걸 막고 싶으면 chunk_size를 200~500 같은 값으로 설정
    chunk_size = getattr(mantis_instance, "scenario_chunk_size", 0)  # 0이면 chunking 안 함

    index_entries = []

    for method_name, arr in scenarios.items():
        if not isinstance(arr, list):
            print(f"[WARN] scenarios['{method_name}'] is not a list. Skipped.")
            continue

        safe_method = _safe_filename(method_name)
        chunks = _chunk_list(arr, chunk_size)

        for k, chunk in enumerate(chunks):
            out_path = f"{base_dir}/{safe_method}_{0}.json"
            out_obj = {
                "method": method_name,
                "scenarios": chunk
            }
            with open(out_path, "w", encoding="utf-8") as f:
                json.dump(out_obj, f, ensure_ascii=False, indent=2)

            index_entries.append({
                "file": os.path.basename(out_path),
                "method": method_name,
                "chunk_index": k,
                "scenario_count": len(chunk)
            })

    # 메타: 어떤 파일이 어떤 메서드/청크인지 추적
    index_path = f"{base_dir}/scenario_{mantis_instance.current_enhance_count}_index.json"
    with open(index_path, "w", encoding="utf-8") as f:
        json.dump(index_entries, f, ensure_ascii=False, indent=2)

    print(f"[INFO] {mantis_instance.class_name} {mantis_instance.method_name} 시나리오 후처리가 완료되었습니다!")