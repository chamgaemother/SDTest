import os
import json
from pathlib import Path
from agents.config import Config
import re

from agents.agent_utils import load_prompt, chat_with_openai, resolve_class_file_path, save_response, find_mantis_test_files, _fmt_deps, _join_lines, _loads_json_robust

SYSTEM_PROMPT_PATH = "./prompt/gen_enhance/system.txt"
USER_PROMPT_PATH = "./prompt/gen_enhance/user.txt"
AI_PROMPT_PATH = "./prompt/gen_enhance/assistant.txt"

def generate_enhance(mantis_instance) -> None:
    class_name = mantis_instance.class_name
    _method_name = mantis_instance.method_name
    _number = mantis_instance.current_enhance_count
    print(f"[INFO] {mantis_instance.class_name} {_method_name} 개선 {_number} 생성 시작")
    
    model_choice = '6' #o4-mini
    if model_choice in Config.MODEL_MAP:
            model = Config.MODEL_MAP[model_choice]
            print(f"[INFO] {mantis_instance.class_name} 모델 `{model}` 선택됨.\n")
    else:
        print(f"[ERROR] {mantis_instance.class_name} 올바르지 않은 모델 선택입니다.")
        return 0
    
    system_prompt = load_prompt(SYSTEM_PROMPT_PATH) 
    ai_prompt = load_prompt(AI_PROMPT_PATH) 
    user_prompt = load_prompt(USER_PROMPT_PATH)
    json_file = Path(mantis_instance.preprocess_data_path)

    if not user_prompt or not system_prompt:
        print(f"[ERROR] {mantis_instance.class_name} 프롬프트 파일이 비어있거나 없습니다. 프롬프트 파일을 확인하세요.")
        return 0
    
    try:
        with json_file.open(encoding="utf-8") as f:
            data = json.load(f)            # → Python list[ dict ]
    except Exception as e:
        print(f"[ERROR] {mantis_instance.class_name} JSON 파일을 여는 중 오류 발생: {e}")
        return 0
    
    print(f"[INFO] {mantis_instance.class_name} 프롬프트 설정 완료!")
    
    file_path = resolve_class_file_path(
        root_dir=mantis_instance.root_dir,
        fqcn=mantis_instance.class_name,
        ext=".java",  # 필요하면 ".kt" 등
    )
    
    test_path = find_mantis_test_files(
        root_dir=mantis_instance.root_dir,
        fqcn=mantis_instance.class_name,
        method_name=_method_name,
        current_enhance_count=_number,
    )
        
    
    if not file_path:
        print(f"[ERROR] {mantis_instance.class_name}  클래스 파일을 찾을 수 없습니다: {mantis_instance.class_name}")
        return 0
    if os.path.exists(file_path):
        # Java 소스 코드 읽기
        with open(file_path, "r", encoding="utf-8") as code_file:
            original_code = code_file.read()
            
    existing_test_code = ""
    for t in test_path :
        with open(t, "r", encoding="utf-8") as test_file:
            existing_test_code += f"---------{t}---------\n" + test_file.read()
            
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
    
    user_prompt_modified = user_prompt.replace("{ LIBRARY_NAME }", library_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_NAME }", class_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_BODY }", original_code)
    user_prompt_modified = user_prompt_modified.replace("{ METHOD_NAME }", mantis_instance.method_name)
    user_prompt_modified = user_prompt_modified.replace("{ METHOD_INFOMATIONs }", method_infomation_prompt)
    user_prompt_modified = user_prompt_modified.replace("{ LINE_COV }", str(mantis_instance.current_line_coverage))
    user_prompt_modified = user_prompt_modified.replace("{ BRANCH_COV }", str(mantis_instance.current_branch_coverage))
    user_prompt_modified = user_prompt_modified.replace("{ EXISTING_TEST_SUITE }", existing_test_code)

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
        output_directory = (Path(mantis_instance.output_dir)
            / library_name
            / mantis_instance.class_name.split('.')[-1] 
            / "scenario_output"
            / mantis_instance.method_name)
        
        save_response(chain_response_obj, model, system_prompt, user_prompt_modified, directory=output_directory, number=_number, type="scenario")
        print(f"\n[INFO] {mantis_instance.class_name} {mantis_instance.method_name} 시나리오 생성이 완료되었습니다!")
