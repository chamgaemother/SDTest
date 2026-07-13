import os
import pandas as pd

# 모델별 요금표 (단위: 달러/USD)
PRICING = {
    "o1": {"prompt": 15.0 / 1_000_000, "completion": 60.0 / 1_000_000},
    "o4-mini": {"prompt": 1.1 / 1_000_000, "completion": 4.4 / 1_000_000},
    "gpt-4o": {"prompt": 2.5 / 1_000_000, "completion": 10.0 / 1_000_000},
    "gpt-4o-mini": {"prompt": 0.15 / 1_000_000, "completion": 0.6 / 1_000_000},
}


def get_model_key(model_name):
    if pd.isna(model_name):
        return None

    model_name = str(model_name).lower()

    if "gpt-4o-mini" in model_name:
        return "gpt-4o-mini"
    elif "gpt-4o" in model_name:
        return "gpt-4o"
    elif "o4-mini" in model_name:
        return "o4-mini"
    elif "o1" in model_name:
        return "o1"
    else:
        return None


def calculate_costs(row):
    model_key = get_model_key(row.get("model", ""))

    prompt_tokens = pd.to_numeric(row.get("prompt_tokens", 0), errors="coerce")
    completion_tokens = pd.to_numeric(row.get("completion_tokens", 0), errors="coerce")

    prompt_tokens = 0 if pd.isna(prompt_tokens) else prompt_tokens
    completion_tokens = 0 if pd.isna(completion_tokens) else completion_tokens

    if model_key in PRICING:
        input_cost = prompt_tokens * PRICING[model_key]["prompt"]
        output_cost = completion_tokens * PRICING[model_key]["completion"]
        total_cost = input_cost + output_cost
        return pd.Series([input_cost, output_cost, total_cost, model_key])

    return pd.Series([0.0, 0.0, 0.0, "unknown"])


def process_csv_file(input_csv_path, output_csv_path):
    df = pd.read_csv(input_csv_path)

    required_cols = {"model", "prompt_tokens", "completion_tokens"}
    missing = required_cols - set(df.columns)
    if missing:
        print(f"[SKIP] 컬럼 부족: {input_csv_path} / missing={sorted(missing)}")
        return

    df[["input_cost", "output_cost", "total_cost", "model_key"]] = df.apply(
        calculate_costs, axis=1
    )

    df.to_csv(output_csv_path, index=False, encoding="utf-8-sig")
    print(f"[OK] 저장 완료: {output_csv_path}")


def process_all_csvs(input_dir, output_dir):
    os.makedirs(output_dir, exist_ok=True)

    for file_name in os.listdir(input_dir):
        if not file_name.lower().endswith(".csv"):
            continue

        input_csv_path = os.path.join(input_dir, file_name)
        output_csv_path = os.path.join(output_dir, file_name)

        process_csv_file(input_csv_path, output_csv_path)


if __name__ == "__main__":
    input_dir = r"./csv_output"
    output_dir = r"./cost_output"

    process_all_csvs(input_dir, output_dir)
    print("모든 CSV 비용 계산 완료!")