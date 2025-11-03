package dagsp;

import graph.DirectedGraph;
import metrics.PerformanceTracker;

import java.util.*;

public class DagShortestPath {

    public Map<Integer, Integer> findShortestPaths(DirectedGraph dag, List<Integer> topOrder, int source, PerformanceTracker tracker) {
        tracker.start();
        int V = dag.getNumVertices();
        Map<Integer, Integer> distances = new HashMap<>();
        for (int i = 0; i < V; i++) {
            distances.put(i, Integer.MAX_VALUE);
        }
        distances.put(source, 0);


        for (int u : topOrder) {
            if (distances.get(u) != Integer.MAX_VALUE) {
                for (int[] edge : dag.getAdj(u)) {
                    int v = edge[0];
                    int weight = edge[1];

                    if (distances.get(u) + weight < distances.get(v)) {
                        distances.put(v, distances.get(u) + weight);
                        tracker.incrementOperation("relaxations");
                    }
                }
            }
        }

        tracker.stop();
        return distances;
    }


    public Map<Integer, Integer> findLongestPaths(DirectedGraph dag, List<Integer> topOrder, PerformanceTracker tracker) {
        tracker.start();
        int V = dag.getNumVertices();
        Map<Integer, Integer> distances = new HashMap<>();
        for (int i = 0; i < V; i++) {
            distances.put(i, Integer.MIN_VALUE);
        }


        int[] inDegree = new int[V];
        for (int i = 0; i < V; i++) for (int[] edge : dag.getAdj(i)) inDegree[edge[0]]++;
        for(int i = 0; i < V; i++) if(inDegree[i] == 0) distances.put(i, 0);


        for (int u : topOrder) {
            if (distances.get(u) != Integer.MIN_VALUE) {
                for (int[] edge : dag.getAdj(u)) {
                    int v = edge[0];
                    int weight = edge[1];

                    if (distances.get(u) + weight > distances.get(v)) {
                        distances.put(v, distances.get(u) + weight);
                        tracker.incrementOperation("relaxations_longest");
                    }
                }
            }
        }

        tracker.stop();
        return distances;
    }
}