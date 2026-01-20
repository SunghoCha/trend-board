import argparse
import csv
import os
from typing import List, Tuple, Optional

import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter


def read_csv(path: str) -> Tuple[List[int], List[float]]:
    xs: List[int] = []
    ys: List[float] = []
    with open(path, newline="", encoding="utf-8") as f:
        r = csv.DictReader(f)
        for row in r:
            xs.append(int(row["offset"]))
            ys.append(float(row["ms"]))
    return xs, ys


def format_offset(x: float, _pos: int) -> str:
    # 0 / 1k / 50k / 1M / 9M 같은 식으로 표시
    if x >= 1_000_000:
        text = f"{x / 1_000_000:.1f}".rstrip("0").rstrip(".")
        return f"{text}M"
    if x >= 1_000:
        return f"{x / 1_000:.0f}k"
    return str(int(x))


def format_ms_as_s(y: float, _pos: int) -> str:
    # y축 라벨을 초 단위로 보여주고 싶으면 이렇게.
    # 예: 10000ms -> 10s
    if y >= 1000:
        sec = y / 1000.0
        if sec < 10:
            text = f"{sec:.1f}".rstrip("0").rstrip(".")
            return f"{text}s"
        return f"{sec:.0f}s"
    return f"{int(y)}ms"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, help="CSV path (columns: offset, ms)")
    parser.add_argument("--output", required=True, help="Output png path")
    parser.add_argument("--title", default="OFFSET vs elapsed time")
    parser.add_argument("--color", default=None, help="Line color (e.g. orange). If omitted, matplotlib default is used.")
    args = parser.parse_args()

    xs, ys = read_csv(args.input)
    if not xs:
        raise SystemExit("CSV is empty")

    os.makedirs(os.path.dirname(args.output) or ".", exist_ok=True)

    plt.figure()
    plt.plot(xs, ys, marker="o", color=args.color)
    plt.title(args.title)
    plt.xlabel("OFFSET")
    plt.ylabel("Elapsed time")

    ax = plt.gca()
    ax.xaxis.set_major_formatter(FuncFormatter(format_offset))
    ax.yaxis.set_major_formatter(FuncFormatter(format_ms_as_s))

    plt.grid(True)
    plt.tight_layout()
    plt.savefig(args.output, dpi=200)
    plt.close()

    print(f"Saved: {args.output}")


if __name__ == "__main__":
    main()
