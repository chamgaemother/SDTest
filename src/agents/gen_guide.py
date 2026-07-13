import os
import json
from pathlib import Path
from agents.config import Config
import re
from agents.agent_utils import load_prompt, chat_with_openai, resolve_class_file_path, save_response, extract_maven_file_errors, _safe_read_text, _fmt_deps, _join_lines, _loads_json_robust


SYSTEM_PROMPT_PATH = "./prompt/gen_guide/system.txt"
USER_PROMPT_PATH = "./prompt/gen_guide/user.txt"
AI_PROMPT_PATH = "./prompt/gen_guide/assistant.txt"


def generate_guide(mantis_instance, _method_name, _number) -> None:
    print(f"[INFO] {mantis_instance.class_name} {_method_name} 오류 수정 가이드 생성 시작")
    
    model_choice = '2' #gpt-4.1-mini
    if model_choice in Config.MODEL_MAP:
            model = Config.MODEL_MAP[model_choice]
            print(f"[INFO] {mantis_instance.class_name} {_method_name}  모델 `{model}` 선택됨.\n")
    else:
        print(f"[ERROR] {mantis_instance.class_name} {_method_name}  올바르지 않은 모델 선택입니다.")
        return 0
    
    system_prompt = load_prompt(SYSTEM_PROMPT_PATH) 
    ai_prompt = load_prompt(AI_PROMPT_PATH) 
    user_prompt = load_prompt(USER_PROMPT_PATH)
    json_file = Path(mantis_instance.preprocess_data_path)

    if not user_prompt or not system_prompt:
        print(f"[ERROR] {mantis_instance.class_name} {_method_name}  프롬프트 파일이 비어있거나 없습니다. 프롬프트 파일을 확인하세요.")
        return 0
    
    try:
        with json_file.open(encoding="utf-8") as f:
            data = json.load(f)            # → Python list[ dict ]
    except Exception as e:
        print(f"[ERROR] {mantis_instance.class_name} {_method_name}  JSON 파일을 여는 중 오류 발생: {e}")
        return 0
    
    print(f"[INFO] {mantis_instance.class_name} {_method_name}  프롬프트 설정 완료!")
    
    file_path = resolve_class_file_path(
        root_dir=mantis_instance.root_dir,
        fqcn=mantis_instance.class_name,
        ext=".java",  # 필요하면 ".kt" 등
    )
    if not file_path:
        print(f"[ERROR] {mantis_instance.class_name} {_method_name}  클래스 파일을 찾을 수 없습니다: {mantis_instance.class_name}")
        return 0
    if os.path.exists(file_path):
        # Java 소스 코드 읽기
        with open(file_path, "r", encoding="utf-8") as code_file:
            original_code = code_file.read()
    library_name = mantis_instance.project_name
    class_name = mantis_instance.class_name
    target = [item for item in data if item.get("clazz") == class_name and item.get("methodName") == _method_name]
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
            #f"- body → {method_body}\n"
            f"- CFG infomation → N : {nodes}, E : {edges}, CC : {cc}\n"
            f"- Dependencies → {dep_classes} {dep_methods}\n"
            #f"[FULL CFG]\n"
            #f"=== BLOCK LIST ===\n"
            #f"{block_list_text}\n"
            #f"=== BLOCK EDGES ===\n"
            #f"{block_edges_text}\n\n"
            #f"[DECISION FLOW SUMMARY]\n"
            #f"{flow_summary_text}"
        )
        method_infomation_list.append(method_block)

    # 필요하면 한 번에 합쳐서 프롬프트에 넣기 좋게:
    method_infomation_prompt = "\n\n".join(method_infomation_list)

        
    _class_name = mantis_instance.class_name.split('.')[-1]        
    test_file_path = os.path.join(f"{mantis_instance.root_dir}", rf"MANTIS-tests/{'/'.join(mantis_instance.class_name.split('.')[:-1])}/{_class_name}_{_method_name}_{mantis_instance.current_enhance_count}_Test.java")
    if not os.path.exists(test_file_path):
        return f"[ERROR] junit5 java 파일 없음: {test_file_path}"
    
    with open(test_file_path, "r", encoding="utf-8") as pf:
        junitcode = pf.read()
    
    error_file_path = os.path.join(f"{Path(mantis_instance.output_dir)}/{mantis_instance.project_name}/{_class_name}/execution_logs", f"{_class_name}_{_method_name}_outLog.txt")
    if not os.path.exists(error_file_path):
        print(f"-- 오류 파일 없음: {error_file_path}")
        return f"-- 오류 파일 없음: {error_file_path}"
  
    with open(error_file_path, "r", encoding="utf-8") as pf:
        out_Msg = pf.read()
        error_msg = extract_maven_file_errors(out_Msg)
    
    
    user_prompt_modified = user_prompt.replace("{ LIBRARY_NAME }", library_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_NAME }", class_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_BODY }", original_code)
    user_prompt_modified = user_prompt_modified.replace("{ TARGET_METHOD }", _method_name)
    user_prompt_modified = user_prompt_modified.replace("{ TARGET_METHOD_INFOMATION }", method_infomation_prompt)
    # user_prompt_modified = user_prompt_modified.replace("{ TEST_SCENARIOS }", scenarios)
    user_prompt_modified = user_prompt_modified.replace("{ EXISTING_JUNIT5_TEST_CODE }", junitcode)
    user_prompt_modified = user_prompt_modified.replace("{ ERROR_LOG_MESSAGE }", "\n".join(error_msg))
    
    
    print(f"[INFO] '\n{file_path}' 파일의 코드를 {model} 모델로 테스트 생성 처리 중...")
        
    try:
        chain_response_obj, chain_response_text = chat_with_openai(
            model, system_prompt, ai_prompt, user_prompt_modified
        )
    except Exception as e:
        print(f"[ERROR] {mantis_instance.class_name}  chat_with_openai 호출 중 예외 발생: {e}")
        
    if chain_response_obj is None:
        print(f"[ERROR] {mantis_instance.class_name}  OpenAI 응답이 없습니다.")
     
    if chain_response_obj:
        save_response(chain_response_obj, model, system_prompt, user_prompt_modified, directory=f"{Path(mantis_instance.output_dir)}/{library_name}/{_class_name}/test_output/{_method_name}", number=_number, type="guide")
            
        save_path = f"{Path(mantis_instance.output_dir)}/{library_name}/{_class_name}/test_output/{_method_name}/guide_{_number}.txt"
        with open(save_path, "w", encoding="utf-8") as sf:
            sf.write(chain_response_obj.choices[0].message.content)
                
        print(f"\n[INFO] {mantis_instance.class_name} {_method_name} 오류 수정 가이드 생성이 완료되었습니다!")
            
