#!/usr/bin/env bash
set -euo pipefail

# 사용법:
#   ./sum_mantis_time.sh [ROOT_DIR] [OUTPUT_CSV]
#
# 예:
#   ./sum_mantis_time.sh . mantis_time_summary.csv

ROOT_DIR="${1:-.}"
OUT_CSV="${2:-mantis_time_summary.csv}"

if [[ ! -d "$ROOT_DIR" ]]; then
  echo "[ERROR] root directory not found: $ROOT_DIR" >&2
  exit 1
fi

# bash associative array: key="project,round"
declare -A SUM_MS
declare -A PROJECT_SEEN

# 루트 바로 아래의 프로젝트 폴더 목록 확보
projects=()
while IFS= read -r -d '' projdir; do
  projname="$(basename "$projdir")"
  projects+=("$projname")
  PROJECT_SEEN["$projname"]=1
done < <(find "$ROOT_DIR" -mindepth 1 -maxdepth 1 -type d -print0 | sort -z)

# 재귀적으로 time txt 탐색
while IFS= read -r -d '' file; do
  base="$(basename "$file")"

  # 파일명에서 회차 추출: mantis-...-01-time.txt ~ mantis-...-10-time.txt
  if [[ "$base" =~ ^mantis-.*-([0-9]{2})-time\.txt$ ]]; then
    round="${BASH_REMATCH[1]}"
  else
    continue
  fi

  # 01~10만 허용
  case "$round" in
    01|02|03|04|05|06|07|08|09|10) ;;
    *) continue ;;
  esac

  # 루트 기준 첫 번째 경로 조각 = 프로젝트명
  rel="${file#$ROOT_DIR/}"
  project="${rel%%/*}"

  # 루트 바로 아래 프로젝트가 아닌 경우는 건너뜀
  if [[ -z "${PROJECT_SEEN[$project]+x}" ]]; then
    continue
  fi

  # ELAPSED_MS 읽기
  elapsed_ms="$(awk -F= '/^ELAPSED_MS=/{print $2; exit}' "$file")"

  if [[ -z "${elapsed_ms:-}" ]]; then
    echo "[WARN] ELAPSED_MS not found: $file" >&2
    continue
  fi

  if [[ ! "$elapsed_ms" =~ ^[0-9]+$ ]]; then
    echo "[WARN] invalid ELAPSED_MS ($elapsed_ms): $file" >&2
    continue
  fi

  key="$project,$round"
  prev="${SUM_MS[$key]:-0}"
  SUM_MS["$key"]=$(( prev + elapsed_ms ))
done < <(find "$ROOT_DIR" -type f -name 'mantis-*-time.txt' -print0)

# CSV 출력
{
  printf '"project"'
  for r in 01 02 03 04 05 06 07 08 09 10; do
    printf ',"%s_seconds"' "$r"
  done
  printf '\n'

  for project in "${projects[@]}"; do
    printf '"%s"' "$project"
    for r in 01 02 03 04 05 06 07 08 09 10; do
      ms="${SUM_MS[$project,$r]:-0}"
      sec="$(awk -v ms="$ms" 'BEGIN { printf "%.3f", ms/1000.0 }')"
      printf ',"%s"' "$sec"
    done
    printf '\n'
  done
} > "$OUT_CSV"

echo "saved: $OUT_CSV"
