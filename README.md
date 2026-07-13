# SDTest — Scenario-Driven Unit Test Generation with Multi-Agent LLMs

SDTest automatically generates JUnit tests for Java methods. It runs a static
analysis over each target method and then uses an LLM-based pipeline to produce,
run, and refine the tests.

The workflow has two stages:

1. **Static analysis** (`methodanalyzer_*`) — extracts per-method information and
   writes it to JSON.
2. **Test generation** (`src/main.py`) — consumes that JSON and generates tests.

## Repository Layout

```
.
├── methodanalyzer_sootup/   # method analyzer (JDK 17+)
├── methodanalyzer_soot/     # method analyzer (JDK 8+)
├── src/
│   ├── main.py              # pipeline entry point
│   ├── agents/              # generation modules
│   ├── tools/               # coverage / execution / utils
│   ├── prompt/              # prompt templates
│   ├── method-json/         # preprocessed method metadata
│   └── results/             # generated tests & logs
├── Benchmark/               # datasets & targets
└── requirement.txt
```

## Requirements

- Python 3+ (virtual environment recommended)
- Java 8+ and Maven (`mvn`) on the system PATH
- An OpenAI API key

```bash
python -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirement.txt
```

## Stage 1 — Extract method metadata

Build and run the analyzer over your target projects:

```bash
cd methodanalyzer_sootup
mvn -q compile
mvn exec:java -Dexec.mainClass=MethodAnalysisToJson \
  -Dexec.args="--root /path/to/projects --csv targets.csv --out /path/to/method-json --skipExisting"
```

- `--csv` must contain `project` and `vid` columns.
- Output is written to `{out}/{project}/{vid}/methods.json`.

## Stage 2 — Generate tests

Set your OpenAI key and model in `src/agents/config.py`, then run:

```bash
cd src
python main.py \
  --root-dir /path/to/target/project \
  --project-name Lang \
  --csv-path targets.csv \
  --preprocess-data-path /path/to/method-json \
  --output-dir ./results \
  --max-enhance-count 2 \
  --coverage-threshold 0.9
```

All arguments are required except `--class-column` (defaults to `class_name`).

| Argument | Description |
|---|---|
| `--root-dir` | Target project root directory |
| `--project-name` | Project name (used for logging/output) |
| `--csv-path` | CSV listing the target classes |
| `--class-column` | CSV column holding the class name (default: `class_name`) |
| `--preprocess-data-path` | Path to the JSON produced in Stage 1 |
| `--output-dir` | Directory for generated tests and logs |
| `--max-enhance-count` | Maximum enhancement iterations |
| `--coverage-threshold` | Target coverage ratio (e.g. `0.9`) |

## Results

Generated tests, logs, and coverage are written under `--output-dir`.
For an aggregate report, run JaCoCo on the target project:

```bash
mvn jacoco:report
```

## Benchmark

Evaluation datasets and target lists are under `Benchmark/`.

## Notes

- **Do not commit your API key.** Keep `config.py` secrets local or load them
  from an environment variable / untracked file.
- Build artifacts (`target/`) are excluded via `.gitignore`.
