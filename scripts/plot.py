#!/usr/bin/env python3
"""Draws all charts of the report from results/*.csv into results/plots/*.png.

Usage (from the project root):
    python3 scripts/plot.py [results_dir]

Requires: pandas, matplotlib.
"""
import sys
from pathlib import Path

import matplotlib

matplotlib.use("Agg")  # no display needed
import matplotlib.pyplot as plt
import pandas as pd

ROOT = Path(__file__).resolve().parent.parent
RES = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "results"
OUT = RES / "plots"
OUT.mkdir(parents=True, exist_ok=True)

COLORS = {"DynamicArray": "tab:blue", "MyLinkedList": "tab:orange", "MinHeap": "tab:green"}
MARKERS = {"DynamicArray": "o", "MyLinkedList": "s", "MinHeap": "^"}
OPS = [("steps", "tab:blue", "o", "-"), ("moves", "tab:red", "s", "--"), ("comparisons", "tab:green", "^", ":")]
NS_LABEL = "Input size n (number of elements)"


def save(fig, name):
    fig.tight_layout()
    fig.savefig(OUT / name, dpi=150)
    plt.close(fig)
    print("saved", OUT / name)


def time_chart(df, workload, title, name, variants=None):
    """Time vs n, both structures on the same chart (one panel per variant for W3)."""
    sub = df[df.workload == workload]
    variants = variants or ["-"]
    fig, axes = plt.subplots(1, len(variants), figsize=(6.2 * len(variants), 4.4), squeeze=False)
    for ax, variant in zip(axes[0], variants):
        for structure, g in sub[sub.variant == variant].groupby("structure"):
            g = g.sort_values("n")
            ax.plot(g.n, g.time_ms, marker=MARKERS[structure], color=COLORS[structure], label=structure)
        ax.set_xscale("log")
        ax.set_yscale("log")
        ax.set_xlabel(NS_LABEL)
        ax.set_ylabel("Median time (ms)")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend()
        if variant != "-":
            ax.set_title(f"{title} - {variant}")
        else:
            ax.set_title(title)
    save(fig, name)


def ops_chart(df, workload, title, name, variants=None):
    """Steps / moves / comparisons vs n; one panel per structure (and per variant)."""
    sub = df[df.workload == workload]
    variants = variants or ["-"]
    structures = sorted(sub.structure.unique())
    fig, axes = plt.subplots(len(variants), len(structures),
                             figsize=(6.2 * len(structures), 4.2 * len(variants)), squeeze=False)
    for r, variant in enumerate(variants):
        for c, structure in enumerate(structures):
            ax = axes[r][c]
            g = sub[(sub.variant == variant) & (sub.structure == structure)].sort_values("n")
            for col, color, marker, ls in OPS:
                ax.plot(g.n, g[col], marker=marker, color=color, linestyle=ls, label=col)
            ax.set_xscale("log")
            ax.set_yscale("symlog", linthresh=1)  # symlog so that zero counters stay visible
            top = max(float(g[[c for c, *_ in OPS]].max().max()), 1.0)
            ax.set_ylim(bottom=0, top=top * 5)  # head-room so the top line is not clipped
            ax.set_xlabel(NS_LABEL)
            ax.set_ylabel("Counted operations (count)")
            suffix = f" - {variant}" if variant != "-" else ""
            ax.set_title(f"{title}: {structure}{suffix}")
            ax.grid(True, which="both", alpha=0.3)
            ax.legend()
    save(fig, name)


def buildheap_chart(bh):
    fig, axes = plt.subplots(1, 3, figsize=(17, 4.6))
    styles = {
        ("BONUS_B", "insert"): ("tab:orange", "o", "-", "n x insert, random data"),
        ("BONUS_B", "buildHeap"): ("tab:blue", "s", "-", "buildHeap, random data"),
        ("BONUS_B_DESC", "insert"): ("tab:red", "o", "--", "n x insert, descending data"),
        ("BONUS_B_DESC", "buildHeap"): ("tab:green", "s", "--", "buildHeap, descending data"),
    }
    for ax, col, ylabel in zip(axes, ["time_ms", "comparisons", "moves"],
                               ["Median time (ms)", "Comparisons (count)", "Moves (count)"]):
        for (wl, variant), (color, marker, ls, label) in styles.items():
            g = bh[(bh.workload == wl) & (bh.variant == variant)].sort_values("n")
            ax.plot(g.n, g[col], marker=marker, color=color, linestyle=ls, label=label)
        ax.set_xscale("log")
        ax.set_yscale("log")
        ax.set_xlabel(NS_LABEL)
        ax.set_ylabel(ylabel)
        ax.set_title(ylabel.split(" (")[0] + ": insert vs buildHeap")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend(fontsize=8)
    save(fig, "bonus_buildheap.png")


def memory_chart(mem):
    method = ", ".join(sorted(mem.method.unique()))
    fig, axes = plt.subplots(1, 2, figsize=(12.5, 4.6))
    for structure, g in mem.groupby("structure"):
        g = g.sort_values("n")
        # DynamicArray and MinHeap have the same layout, so draw the heap dashed to keep both visible
        ls = "--" if structure == "MinHeap" else "-"
        axes[0].plot(g.n, g.mb, marker=MARKERS[structure], color=COLORS[structure], linestyle=ls, label=structure)
        axes[1].plot(g.n, g.bytes / g.n, marker=MARKERS[structure], color=COLORS[structure], linestyle=ls,
                     label=structure)
    axes[0].set_xscale("log")
    axes[0].set_yscale("log")
    axes[0].set_xlabel(NS_LABEL)
    axes[0].set_ylabel("Memory (MB)")
    axes[0].set_title(f"Memory vs n (measured with: {method})")
    axes[1].set_xscale("log")
    axes[1].set_xlabel(NS_LABEL)
    axes[1].set_ylabel("Bytes per stored element")
    axes[1].set_title("Memory overhead per element")
    axes[1].set_ylim(bottom=0)
    for ax in axes:
        ax.grid(True, which="both", alpha=0.3)
        ax.legend()
    save(fig, "bonus_memory.png")


def main():
    df = pd.read_csv(RES / "results.csv")
    time_chart(df, "W1", "W1 Random access (10 000 x get)", "w1_time.png")
    ops_chart(df, "W1", "W1 Random access", "w1_ops.png")
    time_chart(df, "W2", "W2 Search (1 000 x contains)", "w2_time.png")
    ops_chart(df, "W2", "W2 Search", "w2_ops.png")
    time_chart(df, "W3", "W3 Insert & remove (1 000 + 1 000)", "w3_time.png", ["head", "middle"])
    ops_chart(df, "W3", "W3 Insert & remove", "w3_ops.png", ["head", "middle"])
    time_chart(df, "W4", "W4 Priority processing (n insert + n extractMin)", "w4_time.png")
    ops_chart(df, "W4", "W4 Priority processing", "w4_ops.png")

    bh = RES / "buildheap.csv"
    if bh.exists():
        buildheap_chart(pd.read_csv(bh))
    mem = RES / "memory.csv"
    if mem.exists():
        memory_chart(pd.read_csv(mem))


if __name__ == "__main__":
    main()
