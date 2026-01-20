from __future__ import annotations

import argparse
import glob
import json
import os
import re
import statistics
from typing import Dict, List, Tuple

import matplotlib.pyplot as plt


OFFSET_PATTERN = re.compile(r"offset_(\d+)_run(\d+)\.json$")


def parse_offset_and_run(path: str) -> Tuple[int, int]:
    filename = os.path.basename(path)
    m = OFFSET_PATTERN.search(filename)
    if not m:
        raise ValueError(f"Unexpected filename format: {filename}")
    return int(m.group(1)), int(m.group(2))


def read_tps(path: str) -> float:
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    metrics = data.get("metrics", {})
    http_reqs = metrics.get("http_reqs")
    if not http_reqs:
        raise KeyError(f"metrics.http_reqs not found in {path}")

    rate = http_reqs.get("rate")
    if rate is None:
        raise KeyError(f"metrics.http_reqs.rate not found in {path}")

    return float(rate)


def group_by_offset(files: List[str]) -> Dict[int, List[float]]:
    grouped: Dict[int, List[float]] = {}
    for path in files:
        offset, _run = parse_offset_and_run(path)
        tps = read_tps(path)
        grouped.setdefault(offset, []).append(tps)
    return grouped


def median(values: List[float]) -> float:
    if not values:
        raise ValueError("values is empty")
    return float(statistics.median(values))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--input-dir",
        default=os.path.join("out", "raw"),
        help="Directory containing k6 summary JSON files (default: out/raw)",
    )
    parser.add_argument(
        "--output-dir",
        default=os.path.join("out", "report"),
        help="Directory to write report PNGs (default: out/report)",
    )
    parser.add_argument(
        "--tag",
        default="",
        help="Optional tag suffix for output filename (e.g., no_index, idx_createdAt_id)",
    )
    args = parser.parse_args()

    input_dir = args.input_dir
    output_dir = args.output_dir
    tag = args.tag.strip()

    raw_glob = os.path.join(input_dir, "offset_*_run*.json")
    files = sorted(glob.glob(raw_glob))
    if not files:
        raise FileNotFoundError(
            f"No files found: {raw_glob}\n"
            "Expected: <input-dir>/offset_<offset>_run<run>.json"
        )

    grouped = group_by_offset(files)

    # offset별 run 개수 확인(실수 방지용)
    for offset, values in sorted(grouped.items()):
        print(f"offset={offset} runs={len(values)} tps_values={values}")

    offsets = sorted(grouped.keys())
    tps_medians = [median(grouped[o]) for o in offsets]

    os.makedirs(output_dir, exist_ok=True)

    suffix = f"_{tag}" if tag else ""
    out_path = os.path.join(output_dir, f"offset_vs_tps{suffix}.png")

    plt.figure()
    plt.plot(offsets, tps_medians, marker="o")
    plt.title(f"OFFSET vs TPS (http_reqs.rate, median of runs){(' - ' + tag) if tag else ''}")
    plt.xlabel("OFFSET")
    plt.ylabel("TPS (req/s)")
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(out_path, dpi=150)
    plt.close()

    print(f"Saved: {out_path}")


if __name__ == "__main__":
    main()
