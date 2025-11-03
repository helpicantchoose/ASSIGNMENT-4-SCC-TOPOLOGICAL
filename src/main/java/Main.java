import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dagsp.DagShortestPath;
import graph.DirectedGraph;
import metrics.PerformanceTracker;
import scc.KosarajuAlgorithm;
import topo.KahnsAlgorithm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Main {


    static class GraphData {
        int vertices;
        List<List<Integer>> edges;
    }

    public static void main(String[] args) {

        File dataDir = new File("data");
        File[] jsonFiles = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("No .json files found in the /data directory. Aborting.");
            return;
        }

        System.out.println("Found " + jsonFiles.length + " dataset(s). Starting analysis...\n");
        Gson gson = new Gson();


        for (File jsonFile : jsonFiles) {
            System.out.println("========================================================");
            System.out.println("--- Processing file: " + jsonFile.getName() + " ---");
            System.out.println("========================================================\n");

            try (FileReader reader = new FileReader(jsonFile)) {
                Type graphType = new TypeToken<GraphData>() {}.getType();
                GraphData data = gson.fromJson(reader, graphType);


                DirectedGraph graph = new DirectedGraph(data.vertices);
                for (List<Integer> edge : data.edges) {
                    graph.addEdge(edge.get(0), edge.get(1), edge.get(2));
                }


                System.out.println("1. Running Kosaraju's Algorithm to find SCCs...");
                KosarajuAlgorithm kosaraju = new KosarajuAlgorithm();
                PerformanceTracker sccTracker = new PerformanceTracker();
                List<List<Integer>> sccs = kosaraju.findSCCs(graph, sccTracker);

                System.out.println("   Found " + sccs.size() + " Strongly Connected Components:");
                for (List<Integer> scc : sccs) {
                    System.out.println("   - Component (size " + scc.size() + "): " + scc);
                }
                System.out.println("   Performance: " + sccTracker.getOperations() + ", Time: " + sccTracker.getExecutionTimeNanos() + " ns\n");


                System.out.println("2. Building the Condensation Graph (DAG)...");
                DirectedGraph condensationGraph = buildCondensationGraph(graph, sccs);
                System.out.println("   Condensation Graph has " + condensationGraph.getNumVertices() + " vertices (nodes).\n");


                System.out.println("3. Running Kahn's Algorithm for Topological Sort...");
                KahnsAlgorithm kahns = new KahnsAlgorithm();
                PerformanceTracker topoTracker = new PerformanceTracker();
                List<Integer> topOrder = kahns.topologicalSort(condensationGraph, topoTracker);

                if (topOrder != null) {
                    System.out.println("   Topological order of components: " + topOrder);
                    System.out.println("   Performance: " + topoTracker.getOperations() + ", Time: " + topoTracker.getExecutionTimeNanos() + " ns\n");


                    System.out.println("4. Calculating Shortest and Longest Paths on the DAG...");
                    DagShortestPath dagSp = new DagShortestPath();

                    int sourceComponent = topOrder.isEmpty() ? 0 : topOrder.get(0);

                    PerformanceTracker spTracker = new PerformanceTracker();
                    Map<Integer, Integer> shortestPaths = dagSp.findShortestPaths(condensationGraph, topOrder, sourceComponent, spTracker);
                    System.out.println("   Shortest path distances from component " + sourceComponent + ": " + shortestPaths);
                    System.out.println("   Performance: " + spTracker.getOperations() + ", Time: " + spTracker.getExecutionTimeNanos() + " ns\n");

                    PerformanceTracker lpTracker = new PerformanceTracker();
                    Map<Integer, Integer> longestPaths = dagSp.findLongestPaths(condensationGraph, topOrder, lpTracker);
                    int criticalPathLength = 0;
                    for(int dist : longestPaths.values()) if(dist > criticalPathLength) criticalPathLength = dist;
                    System.out.println("   Longest path (critical path) length: " + criticalPathLength);
                    System.out.println("   Performance: " + lpTracker.getOperations() + ", Time: " + lpTracker.getExecutionTimeNanos() + " ns");
                } else {
                    System.out.println("   Could not compute topological sort (this indicates an issue in the condensation graph).");
                }
                System.out.println("\n");

            } catch (IOException e) {
                System.err.println("Error processing file " + jsonFile.getName() + ": " + e.getMessage() + "\n");
            }
        }
    }

    private static DirectedGraph buildCondensationGraph(DirectedGraph originalGraph, List<List<Integer>> sccs) {
        int numSccs = sccs.size();
        DirectedGraph condensationGraph = new DirectedGraph(numSccs);
        int[] vertexToSccId = new int[originalGraph.getNumVertices()];
        for (int i = 0; i < numSccs; i++) {
            for (int vertex : sccs.get(i)) {
                vertexToSccId[vertex] = i;
            }
        }
        Set<String> addedEdges = new HashSet<>();
        for (int u = 0; u < originalGraph.getNumVertices(); u++) {
            for (int[] edge : originalGraph.getAdj(u)) {
                int v = edge[0];
                int weight = edge[1];
                int sccU = vertexToSccId[u];
                int sccV = vertexToSccId[v];
                if (sccU != sccV) {
                    String edgeKey = sccU + "->" + sccV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensationGraph.addEdge(sccU, sccV, weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }
        return condensationGraph;
    }
}
