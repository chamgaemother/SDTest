import os
import json

input_dir = "./result"
output_dir = "./result/enhance_scenarios"

from Utils import count_txt_files_in_enhance
def main() :
    global output_dir
    # 출력 디렉토리 생성 (없으면)
    target = "2enhance_scenarios.txt"
    for filename in os.listdir(input_dir):
        if filename.endswith(target):
            output_dir = "./result/2enhance_scenarios"
        else :
            output_dir = "./result/enhance_scenarios"
    os.makedirs(output_dir, exist_ok=True)

    # 결과 출력용
    print(f"Splitting JSON 'enhance_scenarios' into chunks of 5...")

    # .txt 파일 반복 처리
    for filename in os.listdir(input_dir):
        target = "enhance_scenarios.txt"
        if count_txt_files_in_enhance() > 0:
            target = "2enhance_scenarios.txt"
        if filename.endswith(target):
            filepath = os.path.join(input_dir, filename)

            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
                try:
                    data = json.loads(content)
                except json.JSONDecodeError as e:
                    print(f"❌ Error parsing {filename}: {e}")
                    continue

            # 시나리오 추출 및 분할
            scenarios = data.get("scenarios", [])
            chunks = [scenarios[i:i + 5] for i in range(0, len(scenarios), 5)]

            base_name = os.path.splitext(filename)[0]
            for idx, chunk in enumerate(chunks, start=1):
                output_data = {"scenarios": chunk}
                out_filename = f"{base_name}_part_{idx}.txt"
                out_path = os.path.join(output_dir, out_filename)
                with open(out_path, "w", encoding="utf-8") as out_f:
                    json.dump(output_data, out_f, indent=2, ensure_ascii=False)

            print(f"✅ {filename} → {len(chunks)} parts saved to {output_dir}/")

    print("🎉 Done.")

