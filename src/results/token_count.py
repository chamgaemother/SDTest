import os
import json
import csv
import re


def extract_info_from_json(json_path):
    try:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        info = data.get("info", {})
        usage = info.get("usage", {})

        return {
            "file_path": json_path,
            "model": info.get("model", ""),
            "finish_reason": info.get("finish_reason", ""),
            "completion_tokens": usage.get("completion_tokens", 0),
            "prompt_tokens": usage.get("prompt_tokens", 0),
            "total_tokens": usage.get("total_tokens", 0),
        }
    except Exception as e:
        print(f"Error reading {json_path}: {e}")
        return None


def gather_json_data(project_folder):
    rows = []
    for dirpath, _, filenames in os.walk(project_folder):
        for file in filenames:
            if file.endswith(".json"):
                json_path = os.path.join(dirpath, file)
                info = extract_info_from_json(json_path)
                if info:
                    rows.append(info)
    return rows


def save_to_csv(data, output_csv):
    fieldnames = [
        "file_path",
        "model",
        "finish_reason",
        "completion_tokens",
        "prompt_tokens",
        "total_tokens",
    ]

    with open(output_csv, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data)


def extract_suffix(folder_name):
    """
    예:
      RQ2-01 -> 01
      ABC-12 -> 12
    """
    m = re.search(r"-(\d+)$", folder_name)
    return m.group(1) if m else folder_name


def process_root(root_folder, output_dir):
    os.makedirs(output_dir, exist_ok=True)

    # root 바로 아래의 실험 폴더들 (예: RQ2-01, RQ2-02)
    for batch_name in os.listdir(root_folder):
        batch_path = os.path.join(root_folder, batch_name)

        if not os.path.isdir(batch_path):
            continue

        suffix = extract_suffix(batch_name)

        # batch_path 바로 아래의 프로젝트 폴더들
        for project_name in os.listdir(batch_path):
            project_path = os.path.join(batch_path, project_name)

            if not os.path.isdir(project_path):
                continue

            all_data = gather_json_data(project_path)

            if not all_data:
                print(f"[SKIP] JSON 없음: {project_path}")
                continue

            output_csv_name = f"{project_name}-{suffix}.csv"
            output_csv_path = os.path.join(output_dir, output_csv_name)

            save_to_csv(all_data, output_csv_path)
            print(f"[OK] {len(all_data)}개 추출 -> {output_csv_path}")


if __name__ == "__main__":
    root_folder = r"./"
    output_dir = r"./csv_output"

    process_root(root_folder, output_dir)
    print("완료")