# Assignment 4: SCC|TOPOLOGICAL ORDER|SHORTEST PATH

This report details the implementation and analysis of a SCC,Topological Ordering and Shortest Path finding algorithms. 

This algorithms have been chosen:
*   **Strongly Connected Components:** Kosaraju's Algorithm
*   **Topological Ordering:** Kahn's Algorithm
*   **Shortest/Longest Path:** DAG relaxation based on topological order.(Weights)

## 1. Data Summary

The algorithms were tested with 9 datasets, stored in the `/data` directory. These datasets represent a variety of scenarios, including Directed Acyclic Graphs and tasks with cycles.

For this assignment, the **edge weights** model was chosen, representing the cost or duration to transition from one task to another.

The table below summarizes the structure of each test dataset.

| Graph Name               | Vertices | Edges | Graph Type | Acyclic/Cyclic               |
|--------------------------|----------|-------|------------|------------------------------|
| large_cyclic_1.json      | 40       | 56    | Sparse     | Cyclic                       |
| large_dag_1.json         | 30       | 29    | Sparse     | Directed Acyclic Graph (DAG) |
| large_dag_dense.json     | 35       | 112   | Dense      | Directed Acyclic Graph (DAG) |
| medium_cyclic_1.json     | 12       | 15    | Dense      | Cyclic                       |
| medium_cyclic_dense.json | 20       | 59    | Dense      | Cyclic                       |
| medium_dag_1.json        | 15       | 18    | Sparse     | Directed Acyclic Graph (DAG) |
| small_cyclic_1.json      | 6        | 7     | Dense      | Cyclic                       |
| small_dag_1.json         | 6        | 8     | Dense      | Directed Acyclic Graph (DAG) |
| small_dense.json         | 8        | 20    | Dense      | Directed Acyclic Graph (DAG) |



## 2. Algorithm Results Summary

The following table presents the key performance metrics for each algorithm across all datasets. Notice that time is in (NS).
However you may also see detailed results in [pdf file](https://github.com/helpicantchoose/ASSIGNMENT-4-SCC-TOPOLOGICAL/blob/main/AlgorithmResults.pdf)(Order of datasets may mismatch with table!!)

|Filename                |Vertices|Edges|Num_SCCs|SCC_Time_ns|SCC_DFS_Visits|Topo_Sort_Time_ns|Topo_Sort_Queue_Ops|Shortest_Path_Time_ns|Shortest_Path_Relaxations|Critical_Path_Length|Longest_Path_Time_ns|Longest_Path_Relaxations|
|------------------------|--------|-----|--------|-----------|--------------|-----------------|-------------------|---------------------|-------------------------|--------------------|--------------------|------------------------|
|large_cyclic_1.json     |40      |47   |8       |800800     |80            |374700           |16                 |29600                |7                        |70                  |23700               |7                       |
|large_dag_1.json        |30      |30   |30      |143400     |60            |95800            |60                 |107000               |29                       |55                  |47800               |30                      |
|large_dag_dense.json    |35      |126  |35      |185900     |70            |91800            |70                 |88600                |62                       |725                 |127500              |110                     |
|medium_cyclic_1.json    |12      |15   |4       |39800      |24            |13600            |8                  |12400                |3                        |30                  |5800                |3                       |
|medium_cyclic_dense.json|20      |68   |1       |98300      |40            |8200             |2                  |2100                 |0                        |0                   |1400                |0                       |
|medium_dag_1.json       |15      |18   |15      |33300      |30            |21200            |30                 |23400                |17                       |35                  |17300               |18                      |
|small_cyclic_1.json     |6       |7    |1       |18100      |12            |5500             |2                  |2300                 |0                        |0                   |2900                |0                       |
|small_dag_1.json        |6       |8    |6       |24600      |12            |14100            |12                 |11800                |7                        |18                  |11100               |6                       |
|small_dense.json        |8       |20   |8       |20800      |16            |12600            |16                 |10500                |10                       |53                  |14300               |14                      |

## 3. Performance Analysis

### a. Kosaraju's Algorithm (SCC)

Kosaraju's algorithm operates with a time complexity of **O(V + E)** because it is fundamentally based on two full passes of Depth-First Search (DFS) over the graph.
*   **Bottleneck:** The primary bottleneck is the traversal of every vertex and every edge twice. This is reflected in the `SCC_DFS_Visits` metric, which is consistently `2 * V`.
*   **Effect of Structure:** As seen when comparing `large_dag_1` (30 edges) to `large_dag_dense.json` (126 edges), the execution time increases with the number of edges, even for a similar number of vertices. This confirms that performance is linear in both V and E.

### b. Kahn's Algorithm (Topological Sort)

Kahn's algorithm also runs in **O(V + E)** time, where V and E are the vertices and edges of the graph it is run on.
*   **Bottleneck:** The main work involves calculating initial in-degrees and then processing each vertex and its outgoing edges once. The `Topo_Sort_Queue_Ops` metric (`pushes` + `pops`) directly corresponds to the number of vertices and edges in the processed graph.
*   **Effect of Structure:** This is where the analysis is most interesting. The algorithm runs on the **condensation graph**, not the original graph.
    *   In `medium_cyclic_dense.json`, the entire 20-node graph folded into a **single SCC**. Kahn's algorithm then ran on a trivial 1-node graph, finishing  quickly . Maybe Graph was too interesting ^D.
    *   In contrast, for the DAGs like `large_dag_1`, the condensation graph has the same size (30 nodes) as the original, resulting in significantly more work for the topological sort.

### c. DAG Shortest/Longest Path

This algorithms efficiency is its greatest strength. It runs in **O(V + E)** time on the graph it receives (the condensation graph).
*   **Bottleneck:** The work is dominated by the number of **edge relaxations**, which happens exactly once for every edge in the condensation graph.
*   **Effect of Structure:** Performance is entirely dependent on the size and density of the condensation graph. When a large, complex graph like `medium_cyclic_dense.json` condenses to a single node, the pathfinding algorithm becomes trivial and completes almost instantly, as there are no edges to relax. Conversely, a large, sparse DAG like `large_dag_1` results in more work for this stage. The extremely high critical path length in `large_cyclic_dense.json` (which is a DAG) shows how a dense graph can create very long dependency chains, even without cycles.

## 4. Conclusions and Recommendations

I really wish you reading this! 
1.  **When to Use Each Method:**
    *   **Kosaraju's Algorithm** is a great introduction for finding SCC by being easier to implement but lacking speed in comparing to Tarjans(By some sources). Very consistent amon different languages and easier to debug.
    *   **Kahn's Algorithm** easier to implement and great for finding Topological Order. And also able to find cycles! Safer for large graphs (no recursion stack overflow).
    *   **DAG Pathfinding** It should always be run on a topologically sorted graph to guarantee linear-time performance, for tasks requiring finding distance between objects edge weights model prefered.

2.  **Practical Recommendations :**
    * **Kosarajus Algorithm:** Works best on moderate-sized graphs where building the transpose isn’t too expensive, If your project already has graph.getTranspose() it's okay to leave kosarajus use, but with large graphs Tarjan's algorithm dominating
    * **Kahn's Algorithm:** Solid choice for large graphs, and goes well for next algorithms like pathfinding.
    * **DAG PATHFINDING:** Requires a topological order, Works for both edge-weighted (transition cost) and node-weighted (task duration) models, For cyclic graphs, this approach does not work

Edge relaxed, so we have to relax too :)