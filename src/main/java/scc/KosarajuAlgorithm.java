package scc;

import graph.DirectedGraph;
import metrics.PerformanceTracker;

import java.util.*;

public class KosarajuAlgorithm {


    public List<List<Integer>> findSCCs(DirectedGraph graph, PerformanceTracker tracker) {
        tracker.start();
        int V = graph.getNumVertices();
        Stack<Integer> stack = new Stack<>();
        boolean[] visited = new boolean[V];

        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                fillOrder(i, visited, stack, graph, tracker);
            }
        }


        DirectedGraph transposedGraph = graph.getTranspose();


        Arrays.fill(visited, false);
        List<List<Integer>> sccs = new ArrayList<>();
        while (!stack.empty()) {
            int v = stack.pop();
            if (!visited[v]) {
                List<Integer> component = new ArrayList<>();
                collectSCC(v, visited, component, transposedGraph, tracker);
                sccs.add(component);
            }
        }

        tracker.stop();
        return sccs;
    }


    private void fillOrder(int v, boolean[] visited, Stack<Integer> stack, DirectedGraph graph, PerformanceTracker tracker) {
        visited[v] = true;
        tracker.incrementOperation("dfs_visits");
        for (int[] edge : graph.getAdj(v)) {
            int neighbor = edge[0];
            if (!visited[neighbor]) {
                fillOrder(neighbor, visited, stack, graph, tracker);
            }
        }
        stack.push(v);
    }


    private void collectSCC(int v, boolean[] visited, List<Integer> component, DirectedGraph graph, PerformanceTracker tracker) {
        visited[v] = true;
        component.add(v);
        tracker.incrementOperation("dfs_visits");
        for (int[] edge : graph.getAdj(v)) {
            int neighbor = edge[0];
            if (!visited[neighbor]) {
                collectSCC(neighbor, visited, component, graph, tracker);
            }
        }
    }
}
