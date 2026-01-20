import argparse
import glob
import json
import os
import re
import statistics
from typing import Dict, List, Tuple

import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter


OFFSET_PATTERN = re.compile(r"offset_(\d+)_run(\d+)\.json$")


def parse_offset_and_run(filepath: str) -> Tuple[int, int]:
    filename = os.path.basename(filepath)
    m = OFFSET_PATTERN.match(filename)
    if not m:
        raise ValueError(f"Unexpected filename: {filename}")
    return int(m.group(1)), int(m.group(2))


def read_latency_ms(filepath: str) -> Tuple[float, float]:
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    metrics = data.get("metrics", {})
    http = metrics.get("http_req_duration", {})

    p50 = http.get("med")
    p95 = http.get("p(95)")

    if p50 is None or p95 is None:
        raise ValueError(f"Missing p50/p95 in {filepath}")

    return float(p50), float(p95)


def aggregate_by_offset(files: List[str]) -> Dict[int, Dict[str, List[float]]]:
    by_offset: Dict[int, Dict[str, List[float]]] = {}
    for path in files:
        offset, _run = parse_offset_and_run(path)
        p50, p95 = read_latency_ms(path)

        if offset not in by_offset:
            by_offset[offset] = {"p50": [], "p95": []}

        by_offset[offset]["p50"].append(p50)
        by_offset[offset]["p95"].append(p95)

    return by_offset


def median(values: List[float]) -> float:
    return float(statistics.median(values))


def load_series(input_dir: str) -> Tuple[List[int], List[float], List[float]]:
    file_pattern = os.path.join(input_dir, "offset_*_run*.json")
    files = glob.glob(file_pattern)
    if not files:
        raise ValueError(f"No files matched: {file_pattern}")

    by_offset = aggregate_by_offset(files)
    offsets = sorted(by_offset.keys())

    p50_medians = [median(by_offset[o]["p50"]) for o in offsets]
    p95_medians = [median(by_offset[o]["p95"]) for o in offsets]

    return offsets, p50_medians, p95_medians


def format_offset(x: float, _pos: int) -> str:
    if x >= 1_000_000:
        v = x / 1_000_000
        text = f"{v:.1f}".rstrip("0").rstrip(".")
        return f"{text}M"
    if x >= 1_000:
        return f"{x / 1_000:.0f}k"
    return str(int(x))


def format_ms_as_s(y: float, _pos: int) -> str:
    if y >= 1000:
        sec = y / 1000.0
        if sec < 10:
            text = f"{sec:.1f}".rstrip("0").rstrip(".")
            return f"{text}s"
        return f"{sec:.0f}s"
    return f"{int(y)}ms"


def plot_compare(series: List[Tuple[str, List[int], List[float]]], title: str, outpath: str) -> None:
    plt.figure()

    for label, x, y in series:
        plt.plot(x, y, marker="o", label=label)

    plt.title(title)
    plt.xlabel("OFFSET")
    plt.ylabel("Latency")

    ax = plt.gca()
    ax.xaxis.set_major_formatter(FuncFormatter(format_offset))
    ax.yaxis.set_major_formatter(FuncFormatter(format_ms_as_s))

    plt.grid(True)
    plt.legend()
    plt.tight_layout()
    plt.savefig(outpath, dpi=200)
    plt.close()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--series",
        action="append",
        required=True,
        help="Format: label=dir (e.g., two_step=out/raw/two_step)",
    )
    parser.add_argument("--output-dir", default="out/report/compare")
    parser.add_argument("--tag", default="")
    args = parser.parse_args()

    parsed: List[Tuple[str, str]] = []
    for s in args.series:
        if "=" not in s:
            raise SystemExit(f"--series must be label=dir. got: {s}")
        label, input_dir = s.split("=", 1)
        parsed.append((label.strip(), input_dir.strip()))

    os.makedirs(args.output_dir, exist_ok=True)
    suffix = f"_{args.tag}" if args.tag else ""

    p50_series: List[Tuple[str, List[int], List[float]]] = []
    p95_series: List[Tuple[str, List[int], List[float]]] = []

    for label, input_dir in parsed:
        offsets, p50, p95 = load_series(input_dir)
        p50_series.append((label, offsets, p50))
        p95_series.append((label, offsets, p95))

    plot_compare(
        p50_series,
        "OFFSET vs latency (p50, median of runs)",
        os.path.join(args.output_dir, f"offset_vs_p50_compare{suffix}.png"),
    )
    plot_compare(
        p95_series,
        "OFFSET vs latency (p95, median of runs)",
        os.path.join(args.output_dir, f"offset_vs_p95_compare{suffix}.png"),
    )

    print("Generated:")
    print(os.path.join(args.output_dir, f"offset_vs_p50_compare{suffix}.png"))
    print(os.path.join(args.output_dir, f"offset_vs_p95_compare{suffix}.png"))


if __name__ == "__main__":
    main()
