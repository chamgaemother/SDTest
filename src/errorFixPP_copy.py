import os
import csv
import json
import re
import io
import sys


from Utils import count_txt_files_in_scenarios, count_txt_files_in_enhance, count_java_files, count_txt_files_in_enhance2

CSV_FILE = "path_temp.csv"  # CSV 파일
JSON_FOLDER = "./result"        # JSON 파일들이 들어있는 폴더
SOURCE_DIR = "./result"

def main():
    # 1) CSV 읽어서 rows에 저장
    rows = []
    with open(CSV_FILE, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            rows.append(row)

     # 2) 각 row별 JSON 처리
    for row in rows:
        lib = row["lib"]
        name = row["name"]
        class_name = row["class"]
        
        fix_list = []

        if count_txt_files_in_enhance2() > 0 :

                    out_txt = os.path.join(SOURCE_DIR, f"{class_name}_{name}_2_Test_outMsg.txt")

                    if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                        print(f"파일 존재 & 내용 있음: {out_txt}")
                        fix_list.append(out_txt)
                        # 여기에 처리할 로직 작성
        elif count_txt_files_in_enhance() > 0 :

                    out_txt = os.path.join(SOURCE_DIR, f"{class_name}_{name}_1_Test_outMsg.txt")

                    if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                        print(f"파일 존재 & 내용 있음: {out_txt}")
                        fix_list.append(out_txt)
                        # 여기에 처리할 로직 작성
        else : # 초기 테스트
                out_txt = os.path.join("./error_logs", f"{class_name}_{name}_0_Test_outMsg.txt")

                if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                    print(f"파일 존재 & 내용 있음: {out_txt}")
                    fix_list.append(out_txt)
                    # 여기에 처리할 로직 작성

        for f in fix_list:
            json_path = f.replace("Test_outMsg.txt", "fix_Test.json").replace("error_logs", "result")
            save_path = f.replace("Test_outMsg.txt", "Test.java").replace("error_logs", "result")
            if not os.path.exists(json_path):
                print(f"[ERROR] JSON 파일 없음: {json_path}")
                continue

            try:
                with open(json_path, "r", encoding="utf-8") as jf:
                    data = json.load(jf)
            except Exception as e:
                print(f"[ERROR] JSON 로드 실패: {json_path} ({e})")
                continue

            if "response" not in data:
                print(f"[WARN] {json_path} 내에 'response' 키가 없습니다. 건너뜁니다.")
                continue

            response_text = data["response"]
            CODE_BLOCK_PATTERN = re.compile(r'```(?:json)?\s*(\{.*?\})\s*```', re.DOTALL)

            # 1) 정규식으로 ```json { ... } ``` 내부의 실제 JSON 문자열 찾기
            match = CODE_BLOCK_PATTERN.search(response_text)
            if not match:
                print(f"[WARN] {json_path}에서 ```json {{...}}``` 구조를 찾지 못했습니다.")
                continue

            # 2) 그룹(1)에 있는 { ... } 부분이 실제 JSON 객체
            json_string = match.group(1).strip()
            test_match = re.search(r'"FixedTest"\s*:\s*"(?P<content>.*?)"\s*,\s*"note"', json_string, re.DOTALL)
            if not test_match:
                print(f"[ERROR] 내부에서 'Test' 필드를 추출하지 못했습니다.")
                print("------ 문제가 된 코드블록 ------")
                continue
            raw_code_escaped = test_match.group(1)
            java_code = bytes(raw_code_escaped, "utf-8").decode("unicode_escape")

            improvement_code = java_code

            if "```java" in improvement_code:
                # 1단계: 정규식으로 ```java { ... } ``` 내부의 실제 Java 문자열 찾기
                match = re.search(r'```java\s*(.*?)\s*```', improvement_code, re.DOTALL)
                if match:
                    # 2단계: 그룹(1)에 있는 { ... } 부분이 실제 Java 코드
                    java_code = match.group(1).strip()
                    improvement_code = java_code
                else : 
                    improvement_code = improvement_code.replace("```java", "")

            # 결과 저장

            with open(save_path, "w", encoding="utf-8") as out_file:
                out_file.write(improvement_code)

            print(f"[INFO] {save_path} 저장 완료.")


    return
    


        
main()