import os
import pandas as pd


def normalize_model_name(model_name: str) -> str:
    """
    모델명을 통일해서 비교하기 위한 함수
    o4-mini / gpt-4o-mini 둘 다 처리
    """
    if pd.isna(model_name):
        return ""

    model_name = str(model_name).strip().lower()

    # o4-mini, gpt-4o-mini 둘 다 잡기
    if "gpt-4o-mini" in model_name:
        return "gpt-4o-mini"
    if "o4-mini" in model_name:
        return "o4-mini"

    return model_name


def safe_sum(series):
    return pd.to_numeric(series, errors="coerce").fillna(0).sum()


def summarize_one_csv(csv_path: str) -> dict:
    df = pd.read_csv(csv_path)

    # 혹시 모델명이 비어 있어도 처리되도록
    if "model" not in df.columns:
        df["model"] = ""

    # 숫자 컬럼 안전 변환
    for col in ["prompt_tokens", "completion_tokens", "input_cost", "output_cost"]:
        if col not in df.columns:
            df[col] = 0
        df[col] = pd.to_numeric(df[col], errors="coerce").fillna(0)

    df["normalized_model"] = df["model"].apply(normalize_model_name)

    o4mini_df = df[df["normalized_model"] == "o4-mini"]
    gpt4omini_df = df[df["normalized_model"] == "gpt-4o-mini"]

    input_token_sum = safe_sum(df["prompt_tokens"])
    output_token_sum = safe_sum(df["completion_tokens"])

    o4mini_input_token_sum = safe_sum(o4mini_df["prompt_tokens"])
    gpt4omini_input_token_sum = safe_sum(gpt4omini_df["prompt_tokens"])

    o4mini_output_token_sum = safe_sum(o4mini_df["completion_tokens"])
    gpt4omini_output_token_sum = safe_sum(gpt4omini_df["completion_tokens"])

    input_cost_sum = safe_sum(df["input_cost"])
    output_cost_sum = safe_sum(df["output_cost"])

    o4mini_input_cost_sum = safe_sum(o4mini_df["input_cost"])
    gpt4omini_input_cost_sum = safe_sum(gpt4omini_df["input_cost"])

    o4mini_output_cost_sum = safe_sum(o4mini_df["output_cost"])
    gpt4omini_output_cost_sum = safe_sum(gpt4omini_df["output_cost"])

    total_token_sum = input_token_sum + output_token_sum
    total_cost_sum = input_cost_sum + output_cost_sum

    folder_name = os.path.splitext(os.path.basename(csv_path))[0]

    return {
        "folder_name": folder_name,

        # token 합
        "input_token_sum": input_token_sum,
        "output_token_sum": output_token_sum,
        "total_token_sum": total_token_sum,

        # 모델별 token 합
        "o4-mini_input_token_sum": o4mini_input_token_sum,
        "gpt-4o-mini_input_token_sum": gpt4omini_input_token_sum,
        "o4-mini_output_token_sum": o4mini_output_token_sum,
        "gpt-4o-mini_output_token_sum": gpt4omini_output_token_sum,

        # cost 합
        "input_cost_sum": input_cost_sum,
        "output_cost_sum": output_cost_sum,
        "total_cost_sum": total_cost_sum,

        # 모델별 cost 합
        "o4-mini_input_cost_sum": o4mini_input_cost_sum,
        "gpt-4o-mini_input_cost_sum": gpt4omini_input_cost_sum,
        "o4-mini_output_cost_sum": o4mini_output_cost_sum,
        "gpt-4o-mini_output_cost_sum": gpt4omini_output_cost_sum,

        # 모델별 total cost
        "o4-mini_total_cost_sum": o4mini_input_cost_sum + o4mini_output_cost_sum,
        "gpt-4o-mini_total_cost_sum": gpt4omini_input_cost_sum + gpt4omini_output_cost_sum,
    }


def summarize_cost_output_folder(input_dir: str, output_csv_path: str):
    rows = []

    for file_name in sorted(os.listdir(input_dir)):
        if not file_name.lower().endswith(".csv"):
            continue

        csv_path = os.path.join(input_dir, file_name)

        try:
            row = summarize_one_csv(csv_path)
            rows.append(row)
            print(f"[OK] summarized: {file_name}")
        except Exception as e:
            print(f"[ERROR] {file_name}: {e}")

    summary_df = pd.DataFrame(rows)

    # 보기 좋게 정렬
    if not summary_df.empty:
        summary_df = summary_df.sort_values(by="folder_name")

    summary_df.to_csv(output_csv_path, index=False, encoding="utf-8-sig")
    print(f"\n저장 완료: {output_csv_path}")


if __name__ == "__main__":
    input_dir = r"./cost_output"
    output_csv_path = r"./cost_output/total_summary.csv"

    summarize_cost_output_folder(input_dir, output_csv_path)