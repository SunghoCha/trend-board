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


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input1", required=True)
    parser.add_argument("--label1", default="index")
    parser.add_argument("--input2", required=True)
    parser.add_argument("--label2", default="no_index")
    parser.add_argument("--output", required=True)
    parser.add_argument("--title", default="OFFSET vs time (compare)")

    # ✅ zoom options
    parser.add_argument("--xmin", type=float, default=None)
    parser.add_argument("--xmax", type=float, default=None)
    parser.add_argument("--ymin", type=float, default=None)
    parser.add_argument("--ymax", type=float, default=None)

    args = parser.parse_args()

    xs1, ys1 = read_csv(args.input1)
    xs2, ys2 = read_csv(args.input2)

    os.makedirs(os.path.dirname(args.output) or ".", exist_ok=True)

    plt.figure()
    plt.plot(xs1, ys1, marker="o", label=args.label1)
    plt.plot(xs2, ys2, marker="o", label=args.label2)

    plt.title(args.title)
    plt.xlabel("OFFSET")
    plt.ylabel("Elapsed time")

    ax = plt.gca()
    ax.xaxis.set_major_formatter(FuncFormatter(format_offset))
    ax.yaxis.set_major_formatter(FuncFormatter(format_ms_as_s))

    # ✅ apply zoom if provided
    if args.xmin is not None or args.xmax is not None:
        ax.set_xlim(left=args.xmin, right=args.xmax)
    if args.ymin is not None or args.ymax is not None:
        ax.set_ylim(bottom=args.ymin, top=args.ymax)

    plt.grid(True)
    plt.legend()
    plt.tight_layout()
    plt.savefig(args.output, dpi=200)
    plt.close()

    print(f"Saved: {args.output}")


if __name__ == "__main__":
    main()
