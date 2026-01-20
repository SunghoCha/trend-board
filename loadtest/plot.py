import argparse
import glob
import json
import os
import re
import statistics
from typing import Dict, List, Tuple

import matplotlib.pyplot as plt


def parse_offset_and_run(filepath: str) -> Tuple[int, int]:
    filename = os.path.basename(filepath)
    m = re.match(r"offset_(\d+)_run(\d+)\.json$", filename)
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


def plot_line(x: List[int], y: List[float], title: str, outpath: str) -> None:
    plt.figure()
    plt.plot(x, y, marker="o")
    plt.title(title)
    plt.xlabel("OFFSET")
    plt.ylabel("Latency (ms)")
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(outpath, dpi=200)
    plt.close()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--input-dir",
        default="out/raw",
        help="Directory containing k6 summary JSON files (default: out/raw)",
    )
    parser.add_argument(
        "--output-dir",
        default="out/report",
        help="Directory to write report PNGs (default: out/report)",
    )
    parser.add_argument(
        "--tag",
        default="",
        help="Optional tag suffix for output filenames (e.g., no_index, idx_createdAt_id)",
    )
    args = parser.parse_args()

    input_dir = args.input_dir
    output_dir = args.output_dir
    tag = args.tag.strip()

    file_pattern = os.path.join(input_dir, "offset_*_run*.json")
    files = glob.glob(file_pattern)
    if not files:
        raise SystemExit(f"No files matched: {file_pattern}")

    by_offset = aggregate_by_offset(files)
    offsets = sorted(by_offset.keys())

    p50_medians = [median(by_offset[o]["p50"]) for o in offsets]
    p95_medians = [median(by_offset[o]["p95"]) for o in offsets]

    os.makedirs(output_dir, exist_ok=True)

    suffix = f"_{tag}" if tag else ""

    plot_line(
        offsets,
        p50_medians,
        f"OFFSET vs latency (p50, median of runs){(' - ' + tag) if tag else ''}",
        os.path.join(output_dir, f"offset_vs_p50{suffix}.png"),
    )

    plot_line(
        offsets,
        p95_medians,
        f"OFFSET vs latency (p95, median of runs){(' - ' + tag) if tag else ''}",
        os.path.join(output_dir, f"offset_vs_p95{suffix}.png"),
    )

    print("Generated:")
    print(f" - {os.path.join(output_dir, f'offset_vs_p50{suffix}.png')}")
    print(f" - {os.path.join(output_dir, f'offset_vs_p95{suffix}.png')}")


if __name__ == "__main__":
    main()
