import subprocess
import sys
import io
import os
import shutil
import csv
from datetime import datetime
from collections import defaultdict
import re

from Utils import count_java_files, count_txt_files_in_scenarios, count_txt_files_in_enhance, count_txt_files_in_enhance2
step_count = defaultdict(int)
LOG_FILE_PATH = './result/enhance_logs.txt'
SOURCE_DIR = "./result" 
input_file = 'lib_path_total.csv'
output_file = 'path_temp.csv'

from genEnhanceScenario import main as EnhanceScenario
from enhancePrompt_PP import main as enhancePrompt_PP
from enhanceScenario_PP import main as enhanceScenario_PP
from genEnhancePartTest import main as genEnhanceTest
from enhanceTest_PP import main as enhanceTest_PP
from changeClassNameFromFile import main as changeClassNameFromFile
from positionCopy_copy import initialCopy, enhanceCopy, errorCopy
from errorFix_copy import main as errorFix
from errorFixPP_copy import main as errorFixPP
from Comment import main as Comment, all_comment, target_main, target_all
from AllinOneTest import main as AllInOneTest
from ExeAndCov_copy import main as ExeAndCov

from ErrorGuide import main as ErrorGuide



def log_step(step_name, phase):
    global LOG_FILE_PATH
    """
    step_name : "GenPrompt" 등 단계를 식별할 수 있는 이름
    phase : "START" or "END"
    """
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    log_line = f"[{timestamp}] [{step_name}] {phase}"
    
    # 콘솔 출력
    print(log_line)
    # 파일에 저장
    os.makedirs('./result', exist_ok=True)
    if count_txt_files_in_enhance() > 0:
        LOG_FILE_PATH = './result/2enhance_logs.txt'
    with open(LOG_FILE_PATH, 'a', encoding='utf-8') as f:
        f.write(log_line + '\n')
    if phase == "END":
        step_count[step_name] += 1

def change_class_name_from_file():
    step = "ChangeName_PP"
    log_step(step, "START")
    changeClassNameFromFile()
    log_step(step, "END")

def execute_test(final_loop_flag=0):
    global line_cov, branch_cov
    step = "Execute"
    log_step(step, "START")
    line_cov, branch_cov = ExeAndCov()

    if line_cov == "-" or branch_cov =="-":
        #에러 발생
        return 2
    else :
        print("라인 커버리지:", line_cov)
        print("브랜치 커버리지:", branch_cov)
        return 1
        
def error_fix():
    step = "Error_Fix"
    log_step(step, "START")
    errorFix()
    errorFixPP()
    log_step(step, "END")       

def gen_prompt():
    step = "Enhance Scenario_Gen"
    log_step(step, "START")
    EnhanceScenario()
    log_step(step, "END")

def prompt_post_process():
    step = "Enhance Scenario_PP"
    log_step(step, "START")
    enhancePrompt_PP()
    enhanceScenario_PP()
    log_step(step, "END")

def gen_enhance_test():
    step = "enhanceTest_Gen"
    log_step(step, "START")
    genEnhanceTest()
    log_step(step, "END")

def enhance_test_post_process():
    step = "enhanceTest_PP"
    log_step(step, "START")
    enhanceTest_PP()
    log_step(step, "END")

def count_check(class_value, name_value):
    fix_list = []
    scenario_n = count_txt_files_in_enhance()
    scenario2_n = count_txt_files_in_enhance2()

    if scenario2_n > 0 :
        for i in range(1, count_txt_files_in_scenarios() + 1) :
            out_txt = os.path.join(SOURCE_DIR, f"{class_value}_{name_value}_2_{i}_Test_outMsg.txt")

            if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                print(f"파일 존재 & 내용 있음: {out_txt}")
                fix_list.append(f"{class_value}_{name_value}_2_{i}_Test")
    else :
        for i in range(1, count_txt_files_in_scenarios() + 1) :
            out_txt = os.path.join(SOURCE_DIR, f"{class_value}_{name_value}_1_{i}_Test_outMsg.txt")

            if os.path.exists(out_txt) and os.path.getsize(out_txt) > 0 :
                print(f"파일 존재 & 내용 있음: {out_txt}")
                fix_list.append(f"{class_value}_{name_value}_1_{i}_Test")

    return fix_list
def delete_path(path):
    if os.path.isfile(path):
        os.remove(path)
        print(f"✅ 파일 삭제: {path}")
    elif os.path.isdir(path):
        shutil.rmtree(path)
        print(f"✅ 폴더 및 내부 전체 삭제: {path}")
    else:
        print(f"⚠️ 존재하지 않는 경로: {path}")

def main(class_value, method_value, test_path, enhance_falg):
    try : 
        try_count = 0
        while True :
            try_count += 1
            EnhanceScenario()
            prompt_post_process()
            print("현재 시나리오 파일 수:", count_txt_files_in_enhance(), count_txt_files_in_enhance2(), enhance_falg)
            if count_txt_files_in_enhance () > 0 and enhance_falg == 1:

                print("[INFO] 1차 개선 시나리오 생성 완료")

                break
            elif count_txt_files_in_enhance2() > 0 and enhance_falg ==2:

                print("[INFO] 2차 개선 시나리오 생성 완료")

                break
            elif try_count > 2 :
                return
    except Exception as e :
        print(e)

    try : 
        try_count = 0
        while True:
            try_count += 1
            gen_enhance_test()
            enhance_test_post_process()

            if enhance_falg == 1 and count_java_files() == count_txt_files_in_enhance():
                break
            elif enhance_falg == 2 and count_java_files() == count_txt_files_in_enhance2():
                break
            elif try_count > 2 :
                return
    except Exception as e :
        print(e)

    try :
        change_class_name_from_file()
    except Exception as e :
        print(e)

    try :
        AllInOneTest()
        enhanceCopy()
    except Exception :
        print(Exception)
        return


    scenario_n = count_txt_files_in_enhance()
    scenario2_n = count_txt_files_in_enhance2()

    target_n = scenario_n
    if scenario2_n > 0 :
        target_n = scenario2_n

    scenario_fix_count = dict()
    if scenario2_n > 0 :
        for i in range(1, scenario2_n+1) :
            scenario_fix_count[f"{class_value}_{method_value}_2_Test"] = 0
    else : 
        for i in range(1, scenario_n+1) :
            scenario_fix_count[f"{class_value}_{method_value}_1_Test"] = 0

    fix_count = 0

    while(fix_count < max(target_n, 3)) :
        result = execute_test()    
        if result == 0:
                print("⚠️⚠️Execute InitialTest Error")
                return 0

        elif result == 1 and line_cov > 0: # 정상 실행
                break

        elif result == 2 or (result == 1 and line_cov == 0): #에러 발생 
                fix_count += 1
                print("---------------------------------------------------------")
                print(f"⚠️⚠️ Error Fix Count: {fix_count}")     
                print("---------------------------------------------------------") 

                if fix_count > 0 :
                    try :
                        ErrorGuide()
                        error_fix()
                        change_class_name_from_file()
                        errorCopy()
                    except Exception :
                        print(Exception)

                else :
                    try :
                        error_fix()
                        change_class_name_from_file()
                        errorCopy()
                    except Exception :
                        print(Exception)


    if fix_count  ==  max(target_n, 3):
        print("---------------------------------------------------------")
        print(f"마지막 수정 확인")
        print("---------------------------------------------------------")
        
        result = execute_test(1)      
        if result != 1 or line_cov < 0:
            print("코드를 주석 처리 합니다.")
            Comment()
            result = execute_test()

            if result == 1 and line_cov >= 0:
                print("코드 주석 처리 완료. 다음으로 진행 합니다.")
            else :
                print("삭제 추후 재 실행")
                all_comment()
                input("press")
                return 0
                # delete_path(SOURCE_DIR)

                # for i in range(1, target + 1) :
                #     if scenario2_n > 0 :
                #         print(os.path.join(test_path, f"{class_value}_{method_value}_2_{i}_Test.java"))
                #         delete_path(os.path.join(test_path, f"{class_value}_{method_value}_2_{i}_Test.java"))
                #     else : 
                #         print(os.path.join(test_path, f"{class_value}_{method_value}_1_{i}_Test.java"))
                #         delete_path(os.path.join(test_path, f"{class_value}_{method_value}_1_{i}_Test.java"))
                # return 0


    # if line_cov == 0 or branch_cov == 0:
    #         print("삭제 추후 재 실행")
    #         delete_path(SOURCE_DIR)
    #         for i in range(1, target + 1) :
    #                 if scenario2_n > 0 :
    #                     print(os.path.join(test_path, f"{class_value}_{method_value}_2_{i}_Test.java"))
    #                     delete_path(os.path.join(test_path, f"{class_value}_{method_value}_2_{i}_Test.java"))
    #                 else : 
    #                     print(os.path.join(test_path, f"{class_value}_{method_value}_1_{i}_Test.java"))
    #                     delete_path(os.path.join(test_path, f"{class_value}_{method_value}_1_{i}_Test.java"))
    #         return 0

    print("\n========================")
    print("📊 Step Execution Summary")
    print("========================")
    result_data = []
    for step, count in step_count.items():
            result_data.append(f"- {step} : {count}")
    cov_data = f"line_cov : {line_cov}, branch_cov : {branch_cov}"
    result_data.append(cov_data)
    result_data.append(str(scenario_fix_count))
    results = "\n".join(result_data)
    print(results)
    os.makedirs('./result', exist_ok=True)
    with open(LOG_FILE_PATH, 'a', encoding='utf-8') as f:
            f.write(results + '\n')
    print("========================")
    return 1


def extract_coverage_from_logs(base_dir="./result"):
    results = []
    pattern = re.compile(r"line_cov\s*:\s*([\d.]+)\s*,\s*branch_cov\s*:\s*([\d.]+)")
    
    for root, _, files in os.walk(base_dir):
        for file in files:
            if file.endswith("logs.txt"):
                file_path = os.path.join(root, file)
                with open(file_path, "r", encoding="utf-8") as f:
                    for line in f:
                        match = pattern.search(line)
                        if match:
                            line_cov = float(match.group(1))
                            branch_cov = float(match.group(2))
                            results.append({
                                "file": file_path,
                                "line_cov": line_cov,
                                "branch_cov": branch_cov
                            })
    return results

if __name__ == "__main__":
    with open(input_file, 'r', encoding='utf-8') as infile:
        reader = list(csv.reader(infile))  # 리스트로 변환해서 인덱싱 가능하게
        header = reader[0]  # 첫 줄 고정
        data_rows = reader[1:]  # 나머지 줄 반복 대상

        for i, row in enumerate(data_rows, start=2):  # 줄 번호는 보기 편하게 2부터 시작

            lib = row[0]
            class_name = row[1]
            name = row[2]
            test = row[4]

            enhance_falg = 1


            old_folder = f"{lib}_{class_name}_{name}_result"
            new_folder = f"{lib}_{class_name}_{name}_e1_result"
            
            if os.path.exists(os.path.join(os.getcwd(), new_folder)): # 두번째 개선
                old_folder = f"{lib}_{class_name}_{name}_e1_result"
                new_folder = f"{lib}_{class_name}_{name}_e2_result"
                enhance_falg = 2
                if os.path.exists(new_folder): # 두번째 개선도 이미함.
                    print(f"이미 2차 개선된 항목: {new_folder} → main() 건너뜀")
                    continue

            #커버리지가 개선이 필요 없는지 확인하기
            current_cov_datas = extract_coverage_from_logs(old_folder)

            if len(current_cov_datas) == 0 :
                print(f"로그 파일에서 커버리지 정보를 찾을 수 없음: {old_folder} → main() 건너뜀")
                continue
            if current_cov_datas[0]['branch_cov'] >= 90 :
                print(f"이미 커버리지 충분함: {old_folder} → main() 건너뜀")
                continue

            # 커버리지가 90보다 작은 데이터에 대해서 개선 시작
            print("=======coverage 개선 필요 target========")

            # if os.path.exists(new_folder): # 두번째 개선
            #     new_folder = f"{lib}_{class_name}_{name}_2enhance_result"
            #     old_folder = f"{lib}_{class_name}_{name}_enhance_result"
            #     print(f"1차 개선된 파일 2차 개선 시작.")
            #     if os.path.exists(new_folder): # 두번째 개선도 이미함.
            #         print(f"이미 2차 개선된 항목: {new_folder} → main() 건너뜀")
            #         input("press")
            #         continue
            # else :
            #     continue




            test_folder = "./result"
            folder_path = os.path.join(os.getcwd(), test_folder)

            # 폴더 존재 여부 확인 후 없으면 생성
            if not os.path.exists(folder_path):
                os.makedirs(folder_path)
                print(f"'{test_folder}' 폴더가 없어서 새로 생성했습니다.")
            else:
                print(f"'{test_folder}' 폴더가 이미 존재합니다.")
            #폴더이름 변경안할거임.

            # if os.path.exists(old_folder):
            #     # 기존 새 폴더 있으면 삭제하고 덮어쓰기
            #     if os.path.exists(test_folder):
            #         shutil.rmtree(test_folder)
            #     os.rename(old_folder, test_folder)
            #     print(f"폴더 이름 변경 완료: {old_folder} → {test_folder}")
            # else:
            #     print(f"폴더 없음: {old_folder}")   


            with open(output_file, 'w', encoding='utf-8', newline='') as outfile:
                writer = csv.writer(outfile)
                writer.writerow(header)  # 고정 1줄
                writer.writerow(row)     # 현재 줄

            print(f"{i}번째 줄 처리 중:", row)
            main(class_name, name, test, enhance_falg)

            if os.path.exists(test_folder):
                # 기존 새 폴더 있으면 삭제하고 덮어쓰기
                if os.path.exists(new_folder):
                    shutil.rmtree(new_folder)
                os.rename(test_folder, new_folder)
                print(f"폴더 이름 변경 완료: {test_folder} → {new_folder}")
            else:
                print(f"폴더 없음: {old_folder}")   

