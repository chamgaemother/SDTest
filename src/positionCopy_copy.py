import csv
import os
import shutil

import sys
import io

from Utils import count_txt_files_in_scenarios, count_java_files, count_txt_files_in_enhance, count_txt_files_in_enhance2

# 콘솔 출력 인코딩 설정


CSV_FILE = "path_temp.csv"       # CSV 파일 경로
SOURCE_DIR = "./result" # 복사할 원본 파일들이 있는 디렉터리


def initialCopy():
    # CSV 파일 열기
    with open(CSV_FILE, mode='r', encoding='utf-8', newline='') as f:
        reader = csv.DictReader(f)  # 첫 줄 헤더(lib, class, path, test, name)를 기준으로 DictReader 사용
        for row in reader:
            # CSV 각 행에서 필요한 값 가져오기
            class_value = row['class']
            test_path   = row['test']
            name_value  = row['name']

            try :
                    source_file = os.path.join(SOURCE_DIR, f"{class_value}_{name_value}_0_Test.java")
                    target_file = os.path.join(test_path, f"{class_value}_{name_value}_0_Test.java")

                    os.makedirs(os.path.dirname(target_file), exist_ok=True)
                    # 파일 복사
                    shutil.copy(source_file, target_file)
                    print(f"Copied: {source_file} -> {target_file}")
            except Exception as e:
                print(f"Error: {e}")
def enhanceCopy() :
    if count_txt_files_in_enhance2() > 0 :
        for i in range(count_txt_files_in_enhance2()) :
            enhanceCopy_m()
    else :
        for i in range(count_txt_files_in_enhance()) :
            enhanceCopy_m()
def enhanceCopy_m():
     with open(CSV_FILE, mode='r', encoding='utf-8', newline='') as f:
        reader = csv.DictReader(f)  # 첫 줄 헤더(lib, class, path, test, name)를 기준으로 DictReader 사용
        for row in reader:
            # CSV 각 행에서 필요한 값 가져오기
            class_value = row['class']
            test_path   = row['test']
            name_value  = row['name']
            lib_name  = row['lib']

            try :

                    source_file = os.path.join(SOURCE_DIR, f"{class_value}_{name_value}_1_Test.java")
                    target_file = os.path.join(test_path, f"{class_value}_{name_value}_1_Test.java")
                    if os.path.exists(f'{lib_name}_{class_value}_{name_value}_e1_result'):
                        source_file = os.path.join(SOURCE_DIR, f"{class_value}_{name_value}_2_Test.java")
                        target_file = os.path.join(test_path, f"{class_value}_{name_value}_2_Test.java")

                    if os.path.exists(target_file):
                        continue
                    else:
                        # 테스트 디렉터리가 존재하지 않을 수도 있으므로, 필요 시 생성
                        os.makedirs(os.path.dirname(target_file), exist_ok=True)
                        # 파일 복사
                        shutil.copy(source_file, target_file)
                        print(f"Copied: {source_file} -> {target_file}")
                        break
            except Exception as e:
                print(f"Error: {e}")

def errorCopy():
    with open(CSV_FILE, mode='r', encoding='utf-8', newline='') as f:
        reader = csv.DictReader(f)  # 첫 줄 헤더(lib, class, path, test, name)를 기준으로 DictReader 사용
        for row in reader:
            # CSV 각 행에서 필요한 값 가져오기
            class_value = row['class']
            test_path   = row['test']
            name_value  = row['name']
            lib_name  = row['lib']

            fix_list = []

            if os.path.exists(f'{lib_name}_{class_value}_{name_value}_e1_result'):
                    out_txt = os.path.join("error_logs", f"{class_value}_{name_value}_2_Test_outMsg.txt")

                    if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                        print(f"파일 존재 & 내용 있음: {out_txt}")
                        fix_list.append(out_txt)
            elif os.path.exists(f'{lib_name}_{class_value}_{name_value}_result'):
                    out_txt = os.path.join("error_logs", f"{class_value}_{name_value}_1_Test_outMsg.txt")

                    if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                        print(f"파일 존재 & 내용 있음: {out_txt}")
                        fix_list.append(out_txt)
                        # 여기에 처리할 로직 작성
            else : # 초기 테스트
                    out_txt = os.path.join("error_logs", f"{class_value}_{name_value}_0_Test_outMsg.txt")

                    if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                        print(f"파일 존재 & 내용 있음: {out_txt}")
                        fix_list.append(out_txt)


            for f in fix_list :
                source_file = f.replace("_outMsg.txt", ".java").replace("error_logs", SOURCE_DIR)
                target_file = source_file.replace(SOURCE_DIR, test_path)
                # 파일 복사
                try :
                    shutil.copy(source_file, target_file)
                    print(f"Copied: {source_file} -> {target_file}")
  
                except Exception as e:
                    print(f"Error: {e}")

            return

    
if __name__ == "__main__": 
    enhanceCopy()