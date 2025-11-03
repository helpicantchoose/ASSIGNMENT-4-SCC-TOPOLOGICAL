# Assignment 4: Smart City Task Scheduling Analysis

This report details the implementation and analysis of a scheduling system for a "Smart City" scenario. The system processes tasks with complex dependencies by identifying cyclic task groups (Strongly Connected Components), creating a logical execution plan (Topological Sort), and determining optimal execution paths (Shortest and Longest Path in a DAG).

The implementation uses the following algorithms:
*   **Strongly Connected Components:** Kosaraju's Algorithm
*   **Topological Ordering:** Kahn's Algorithm
*   **Shortest/Longest Path:** DAG relaxation based on topological order.

## 1. Data Summary

The algorithms were tested against 9 custom-generated datasets, stored in the `/data` directory. These datasets represent a variety of scheduling scenarios, including purely sequential tasks (DAGs) and tasks with circular dependencies (cyclic graphs).

For this project, the **edge weights** model was chosen, representing the cost or duration to transition from one task to another.

The table below summarizes the structure of each test dataset.

| Dataset Name | Vertices | Edges | Declared Structure | Actual Structure Found |
| :--- | :--- | :--- | :--- | :--- |
| `small_dag_1.json` | 6 | 8 | DAG | DAG (6 SCCs) |
| `small_dense.json` | 8 | 20 | DAG | DAG (8 SCCs) |
| `small_cyclic_1.json` | 6 | 7 | Cyclic | Single Large SCC |
| `medium_dag_1.json` | 15 | 18 | DAG | DAG (15 SCCs) |
| `medium_cyclic_1.json` | 12 | 15 | Cyclic | 4 distinct SCCs |
| `medium_cyclic_dense.json`| 20 | 80 | Cyclic | Single Large SCC |
| `large_dag_1.json` | 30 | 30 | DAG | DAG (30 SCCs) |
| `large_cyclic_1.json` | 40 | 51 | Cyclic | 8 distinct SCCs |
| `large_cyclic_dense.json`| Cyclic | 35 | 150 | **DAG (35 SCCs)** |

***Note on `large_cyclic_dense.json`:*** *The implementation correctly identified this graph as a DAG, revealing a mismatch between the file's content and its name. This demonstrates the robustness of the SCC algorithm as a validation tool.*

## 2. Algorithm Results Summary

The following table presents the key performance metrics for each algorithm across all datasets. Time is reported in milliseconds (ms) for readability.

| Dataset | Vertices | Edges | SCCs Found | SCC Time (ms) | SCC DFS Visits | Topo Sort Time (ms) | Topo Queue Ops | Critical Path Length |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `small_dag_1.json` | 6 | 8 | 6 | 0.017 | 12 | 0.010 | 12 | 18 |
| `small_dense.json` | 8 | 20 | 8 | 0.020 | 16 | 0.013 | 16 | 53 |
| `small_cyclic_1.json` | 6 | 7 | 1 | 0.019 | 12 | 0.005 | 2 | 0 |
| `medium_dag_1.json` | 15 | 18 | 15 | 0.034 | 30 | 0.020 | 30 | 35 |
| `medium_cyclic_1.json` | 12 | 15 | 4 | 0.032 | 24 | 0.012 | 8 | 30 |
| `medium_cyclic_dense.json`| 20 | 80 | 1 | 0.048 | 40 | 0.006 | 2 | 0 |
| `large_dag_1.json` | 30 | 30 | 30 | 0.062 | 60 | 0.047 | 60 | 55 |
| `large_cyclic_1.json` | 40 | 51 | 8 | 0.494 | 80 | 0.417 | 16 | 70 |
| `large_cyclic_dense.json`| 35 | 150 | 35 | 0.239 | 70 | 0.096 | 70 | 725 |

## 3. Performance Analysis

### a. Kosaraju's Algorithm (SCC)

Kosaraju's algorithm operates with a time complexity of **O(V + E)** because it is fundamentally based on two full passes of Depth-First Search (DFS) over the graph.
*   **Bottleneck:** The primary bottleneck is the traversal of every vertex and every edge twice. This is reflected in the `SCC_DFS_Visits` metric, which is consistently `2 * V`.
*   **Effect of Structure:** As seen when comparing `large_dag_1` (30 edges) to `large_cyclic_dense.json` (150 edges), the execution time increases with the number of edges, even for a similar number of vertices. This confirms that performance is linear in both V and E.

### b. Kahn's Algorithm (Topological Sort)

Kahn's algorithm also runs in **O(V + E)** time, where V and E are the vertices and edges of the graph it is run on.
*   **Bottleneck:** The main work involves calculating initial in-degrees and then processing each vertex and its outgoing edges once. The `Topo_Sort_Queue_Ops` metric (`pushes` + `pops`) directly corresponds to the number of vertices and edges in the processed graph.
*   **Effect of Structure:** This is where the analysis is most interesting. The algorithm runs on the **condensation graph**, not the original graph.
    *   In `medium_cyclic_dense.json`, the entire 20-node graph collapsed into a **single SCC**. Kahn's algorithm then ran on a trivial 1-node graph, finishing extremely quickly (0.006 ms).
    *   In contrast, for the DAGs like `large_dag_1`, the condensation graph has the same size (30 nodes) as the original, resulting in significantly more work for the topological sort.

### c. DAG Shortest/Longest Path

This algorithm's efficiency is its greatest strength. It runs in **O(V + E)** time on the graph it receives (the condensation graph).
*   **Bottleneck:** The work is dominated by the number of **edge relaxations**, which happens exactly once for every edge in the condensation graph.
*   **Effect of Structure:** Performance is entirely dependent on the size and density of the condensation graph. When a large, complex graph like `medium_cyclic_dense.json` condenses to a single node, the pathfinding algorithm becomes trivial and completes almost instantly, as there are no edges to relax. Conversely, a large, sparse DAG like `large_dag_1` results in more work for this stage. The extremely high critical path length in `large_cyclic_dense.json` (which is a DAG) shows how a dense graph can create very long dependency chains, even without cycles.

## 4. Conclusions and Recommendations

The implemented pipeline provides a robust and efficient solution for scheduling complex, interdependent tasks.

1.  **When to Use Each Method:**
    *   **Kosaraju's Algorithm** is essential as the first step for any graph where cyclic dependencies are possible. It correctly identifies which tasks are mutually dependent and must be treated as a single unit.
    *   **Kahn's Algorithm** is the natural next step, providing a valid execution order for the task units (SCCs). Its ability to detect cycles also makes it a good validation tool.
    *   **DAG Pathfinding** is the final, critical step for optimization. It should always be run on a topologically sorted graph to guarantee linear-time performance.

2.  **Practical Recommendations for "Smart City" Scheduling:**
    *   **Identify Task Groups First:** Before attempting to schedule individual tasks, always run an SCC algorithm. This will group tasks like "inspect road, repair pothole, repaint lines" on the same street segment into a single, inseparable unit.
    *   **Build a High-Level Plan:** The condensation graph represents the true, high-level project plan. Applying topological sort to this graph gives the city planners a clear, valid sequence of operations for the task groups.
    *   **Find the Bottleneck:** The **longest path (critical path)** calculation is the most important output for project management. It tells planners the absolute minimum time the entire set of tasks will take and highlights the specific sequence of tasks that cannot be delayed without delaying the entire project. This is crucial for allocating resources efficiently.

This modular, multi-stage approach is highly practical for real-world scheduling problems, providing both correctness in the face of cycles and optimal efficiency for planning.

