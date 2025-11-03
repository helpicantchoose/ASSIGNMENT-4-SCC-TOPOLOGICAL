import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dagsp.DagShortestPath;
import graph.DirectedGraph;
import metrics.PerformanceTracker;
import scc.KosarajuAlgorithm;
import topo.KahnsAlgorithm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;

public class BatchRunner {


    static class GraphData {
        int vertices;
        List<List<Integer>> edges;
    }

    public static void main(String[] args) {
        File dataDir = new File("data");
        File[] jsonFiles = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("No .json files found in the /data directory.");
            return;
        }

        System.out.println("Found " + jsonFiles.length + " dataset(s). Starting batch processing...");
        List<Map<String, Object>> allResults = new ArrayList<>();

        for (File jsonFile : jsonFiles) {
            System.out.println("Processing: " + jsonFile.getName());
            try {
                Map<String, Object> fileResult = processGraphFile(jsonFile);
                allResults.add(fileResult);
            } catch (IOException e) {
                System.err.println("Error processing file " + jsonFile.getName() + ": " + e.getMessage());
            }
        }

        try {
            writeToCsv(allResults, "results.csv");
            System.out.println("\nBatch processing complete. Results saved to results.csv");
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }


    private static Map<String, Object> processGraphFile(File jsonFile) throws IOException {
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("Filename", jsonFile.getName());
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(jsonFile)) {
            Type graphType = new TypeToken<GraphData>() {}.getType();
            GraphData data = gson.fromJson(reader, graphType);

            results.put("Vertices", data.vertices);
            results.put("Edges", data.edges.size());

            DirectedGraph graph = new DirectedGraph(data.vertices);
            for (List<Integer> edge : data.edges) {
                graph.addEdge(edge.get(0), edge.get(1), edge.get(2));
            }


            KosarajuAlgorithm kosaraju = new KosarajuAlgorithm();
            PerformanceTracker sccTracker = new PerformanceTracker();
            List<List<Integer>> sccs = kosaraju.findSCCs(graph, sccTracker);
            results.put("Num_SCCs", sccs.size());
            results.put("SCC_Time_ns", sccTracker.getExecutionTimeNanos());
            results.put("SCC_DFS_Visits", sccTracker.getOperations().getOrDefault("dfs_visits", 0L));


            DirectedGraph condensationGraph = buildCondensationGraph(graph, sccs);
            KahnsAlgorithm kahns = new KahnsAlgorithm();
            PerformanceTracker topoTracker = new PerformanceTracker();
            List<Integer> topOrder = kahns.topologicalSort(condensationGraph, topoTracker);

            if (topOrder != null) {
                long queueOps = topoTracker.getOperations().getOrDefault("queue_pushes", 0L) +
                        topoTracker.getOperations().getOrDefault("queue_pops", 0L);
                results.put("Topo_Sort_Time_ns", topoTracker.getExecutionTimeNanos());
                results.put("Topo_Sort_Queue_Ops", queueOps);


                DagShortestPath dagSp = new DagShortestPath();
                int sourceComponent = topOrder.isEmpty() ? 0 : topOrder.get(0);

                PerformanceTracker spTracker = new PerformanceTracker();
                dagSp.findShortestPaths(condensationGraph, topOrder, sourceComponent, spTracker);
                results.put("Shortest_Path_Time_ns", spTracker.getExecutionTimeNanos());
                results.put("Shortest_Path_Relaxations", spTracker.getOperations().getOrDefault("relaxations", 0L));

                PerformanceTracker lpTracker = new PerformanceTracker();
                Map<Integer, Integer> longestPaths = dagSp.findLongestPaths(condensationGraph, topOrder, lpTracker);
                int criticalPathLength = 0;
                for(int dist : longestPaths.values()) if(dist > criticalPathLength) criticalPathLength = dist;
                results.put("Critical_Path_Length", criticalPathLength);
                results.put("Longest_Path_Time_ns", lpTracker.getExecutionTimeNanos());
                results.put("Longest_Path_Relaxations", lpTracker.getOperations().getOrDefault("relaxations_longest", 0L));
            }
        }
        return results;
    }

    private static void writeToCsv(List<Map<String, Object>> allResults, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {

            String[] headers = {"Filename", "Vertices", "Edges", "Num_SCCs", "SCC_Time_ns", "SCC_DFS_Visits",
                    "Topo_Sort_Time_ns", "Topo_Sort_Queue_Ops", "Shortest_Path_Time_ns",
                    "Shortest_Path_Relaxations", "Critical_Path_Length", "Longest_Path_Time_ns",
                    "Longest_Path_Relaxations"};
            writer.println(String.join(",", headers));


            for (Map<String, Object> result : allResults) {
                List<String> row = new ArrayList<>();
                for (String header : headers) {
                    row.add(result.getOrDefault(header, "N/A").toString());
                }
                writer.println(String.join(",", row));
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
