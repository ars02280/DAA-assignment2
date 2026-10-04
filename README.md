# DAA Assignment 2 - In-Memory Workload Engine

Own implementations of three data structures for primitive `int` values (no `java.util` collections, no boxing):

| Class | What it is | Package |
|---|---|---|
| `DynamicArray` | resizable array, grows 2x when full | `dataStr` |
| `MyLinkedList` | singly linked list with a tail pointer | `dataStr` |
| `MinHeap` | array-based binary min-heap (+ Floyd's `buildHeap`, bonus B) | `dataStr` |

`DynamicArray` and `MyLinkedList` implement the same interface `IntList`, so one benchmark runs both.
Every structure counts its own **steps / moves / comparisons** inside its methods (class `Metrics`).

## Requirements

* JDK 25 (as configured in `pom.xml`; change `maven.compiler.source/target` if you use an older JDK, the code is plain Java 17+)
* Maven 3.9+
* Python 3 with `pandas` and `matplotlib` (only for drawing the charts)

## Build, test, run

```bash
mvn test                      # JUnit 5 tests (correctness, edge cases, heap property, sorted output, counters)
mvn compile exec:java         # runs W1-W4 + both bonus tasks, writes results/*.csv (seed 42)
python3 scripts/plot.py       # draws results/plots/*.png from the CSV files
```

`mvn compile exec:java` takes about one to two minutes. An output directory can be passed with
`mvn compile exec:java -Dexec.args="my-results"` (default: `results`).

Without Maven (plain JDK): `javac -d out $(find src/main -name "*.java")` and `java -cp out org.example.Main`.

## Project layout

```
src/main/java/dataStr/      DynamicArray, MyLinkedList, MinHeap, IntList, Measurable, Metrics
src/main/java/benchmark/    Benchmark (W1-W4), BuildHeapBenchmark (bonus B), MemoryBenchmark (bonus A), CsvWriter, Result
src/main/java/org/example/  Main (entry point)
src/test/java/dataStr/      JUnit 5 tests
scripts/plot.py             charts
results/results.csv         workload,variant,structure,n,time_ms,steps,moves,comparisons
results/buildheap.csv       bonus B (same columns, workload BONUS_B / BONUS_B_DESC)
results/memory.csv          bonus A: structure,n,bytes,mb,method
results/plots/              PNG charts
REPORT.md                   complexity table, loop-invariant proofs, plots, discussion
```

## What is counted

| Counter | DynamicArray / MinHeap | MyLinkedList |
|---|---|---|
| **steps** | one read of an array cell | one move to the next node (`cur = cur.next`) |
| **moves** | one element shifted or stored in the array (growing copies every element: 1 step + 1 move each) | one link update (every assignment to a `next` pointer, `head` or `tail`) |
| **comparisons** | one comparison of two elements | one comparison of two elements |

Counters are incremented inside the operations (never estimated afterwards) and are reset after filling a
structure, so the CSV contains only the cost of the measured workload.

## Benchmark method

* Data: `new Random(42)`, generated once per workload and size, so both structures get identical input.
* Sizes n = 100, 1 000, 10 000, 100 000.
* A global warm-up pass runs first; then every case runs 3 warm-up runs (discarded) + 5 measured runs; the CSV holds the **median** time of the 5 measured runs.
* Filling the structure is not part of the timed region.
* W3: 1 000 insertions followed by 1 000 removals at the same index (`0` for `head`, `n / 2` for `middle`).
* `memory.csv` is measured with JOL when it can be loaded (the `method` column says `jol`), otherwise with a heap-before/after fallback (`heap-delta`).

## Git workflow

Branches `feature/metrics`, `feature/array`, `feature/list`, `feature/heap` were merged (no fast-forward) into `main`;
release tag `v1.0` is on `main`.
