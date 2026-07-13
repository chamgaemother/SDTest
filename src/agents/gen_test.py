import os
import json
from pathlib import Path
from agents.config import Config
import re

from agents.agent_utils import load_prompt, chat_with_openai, resolve_class_file_path, save_response, _ensure_dir, _safe_read_text, _fmt_deps, _join_lines, _loads_json_robust

SYSTEM_PROMPT_PATH = "./prompt/gen_test/system.txt"
USER_PROMPT_PATH = "./prompt/gen_test/user.txt"
AI_PROMPT_PATH = "./prompt/gen_test/assistant.txt"


def generate_test(mantis_instance, _method_name, _number) -> None:
    print(f"[INFO] {mantis_instance.class_name} {_method_name} 테스트 코드 생성 시작")

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
    if not file_path:
        print(f"[ERROR] {mantis_instance.class_name}  클래스 파일을 찾을 수 없습니다: {mantis_instance.class_name}")
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
        
    senario_file_path = os.path.join(f"{Path(mantis_instance.output_dir)}", f"{library_name}/{mantis_instance.class_name.split('.')[-1]}/scenario_output/{mantis_instance.method_name}/{_method_name}_{_number}.json")
        
    if not os.path.exists(senario_file_path):
        print(f"[ERROR] 시나리오 프롬프트 파일 없음: {senario_file_path}")
        scenarios = "No scenarios available."
    else :
        with open(senario_file_path, "r", encoding="utf-8") as pf:
            scenarios = pf.read()
            
        
    # user_prompt에서 필요한 부분들을 순차적으로 대체
        
    user_prompt_modified = user_prompt.replace("{ LIBRARY_NAME }", library_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_NAME }", class_name)
    user_prompt_modified = user_prompt_modified.replace("{ CLASS_BODY }", original_code)
    user_prompt_modified = user_prompt_modified.replace("{ TARGET_METHOD }", _method_name)
    user_prompt_modified = user_prompt_modified.replace("{ TARGET_METHOD_INFOMATION }", method_infomation_prompt)
    user_prompt_modified = user_prompt_modified.replace("{ TEST_SCENARIOS }", scenarios)
        
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
        save_response(chain_response_obj, model, system_prompt, user_prompt_modified, directory=f"{Path(mantis_instance.output_dir)}/{library_name}/{mantis_instance.class_name.split('.')[-1]}/test_output/{_method_name}", number=_number, type="test")
        print(f"\n[INFO] {mantis_instance.class_name}  테스트 생성이 완료되었습니다!")

    
    

    
def postprocess_tests(mantis_instance, _method_name, _number) -> None:
    print(f"[INFO] {mantis_instance.class_name} {_method_name} 테스트 코드 후처리 시작")

    # 후처리할 JSON 파일들이 있는 폴더 경로
    base_dir = f"{Path(mantis_instance.output_dir)}/{mantis_instance.project_name}/{mantis_instance.class_name.split('.')[-1]}/test_output/{_method_name}"
    _ensure_dir(base_dir)
    
    test_file_path = f"{base_dir}/test_{_number}.json"
    
    if not os.path.exists(test_file_path):
        raise FileNotFoundError(f"[ERROR] Test Response file not found: {test_file_path}")
    
    raw_text = _safe_read_text(test_file_path)
    
    try:
        wrapper_obj = _loads_json_robust(raw_text)
    except Exception as e:
        debug_path = f"{base_dir}/scenario_{_number}_raw_wrapper.txt"
        with open(debug_path, "w", encoding="utf-8") as f:
            f.write(raw_text)
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
            if "```java" in response_raw:
                # 1단계: 정규식으로 ```java { ... } ``` 내부의 실제 Java 문자열 찾기
                match = re.search(r'```java\s*(.*?)\s*```', response_raw, re.DOTALL)
                if match:
                    # 2단계: 그룹(1)에 있는 { ... } 부분이 실제 Java 코드
                    java_code = match.group(1).strip()
                    test_code = java_code
                    # 결과 저장
                    output_filename = os.path.join(
                        base_dir, f"{mantis_instance.class_name.split('.')[-1]}_{_method_name}_{_number}_Test.java"
                    )
                    with open(output_filename, "w", encoding="utf-8") as out_file:
                        out_file.write(test_code)

                    print(f"[INFO] {mantis_instance.class_name}_{_method_name}_{_number}_Test.java 저장 완료.")
                    return True
            else :
                debug_path = f"{base_dir}/test_{_number}_raw_response.txt"
                with open(debug_path, "w", encoding="utf-8") as f:
                    f.write(response_raw)
                raise RuntimeError (f"[WARN] Failed to parse response JSON. Saved raw to {debug_path}") from e
    elif isinstance(response_payload, dict):
        response_obj = response_payload
    else:
        raise TypeError(f"[ERROR] Unexpected type for response payload: {type(response_payload)}")
    
    # 4) Test 추출
    if not (isinstance(response_obj, dict) and "Test" in response_obj):
        debug_path = f"{base_dir}/test_{_number}_response_obj.json"
        with open(debug_path, "w", encoding="utf-8") as f:
            json.dump(response_obj, f, ensure_ascii=False, indent=2)
        raise KeyError(f"[ERROR] 'Test' key not found in response_obj. Dumped to {debug_path}")

    test = response_obj["Test"]
    
    if "```java" in test:
                # 1단계: 정규식으로 ```java { ... } ``` 내부의 실제 Java 문자열 찾기
                match = re.search(r'```java\s*(.*?)\s*```', test, re.DOTALL)
                if match:
                    # 2단계: 그룹(1)에 있는 { ... } 부분이 실제 Java 코드
                    java_code = match.group(1).strip()
                    test_code = java_code
                else : 
                    test_code = test.replace("```java", "")
                    test_code = test_code.replace("```", "")
                    
    else :
        test_code = test
        
    # 결과 저장
    output_filename = os.path.join(
        base_dir, f"{mantis_instance.class_name.split('.')[-1]}_{_method_name}_{_number}_Test.java"
    )
    with open(output_filename, "w", encoding="utf-8") as out_file:
        out_file.write(test_code)

    print(f"[INFO] {mantis_instance.class_name}_{_method_name}_{_number}_Test.java 저장 완료.")