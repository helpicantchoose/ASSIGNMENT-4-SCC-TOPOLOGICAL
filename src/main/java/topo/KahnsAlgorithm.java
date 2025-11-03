package topo;

import graph.DirectedGraph;
import metrics.PerformanceTracker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class KahnsAlgorithm {


    public List<Integer> topologicalSort(DirectedGraph dag, PerformanceTracker tracker) {
        tracker.start();
        int V = dag.getNumVertices();
        int[] inDegree = new int[V];


        for (int i = 0; i < V; i++) {
            for (int[] edge : dag.getAdj(i)) {
                inDegree[edge[0]]++;
            }
        }


        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < V; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
                tracker.incrementOperation("queue_pushes");
            }
        }

        List<Integer> topOrder = new ArrayList<>();
        int visitedCount = 0;


        while (!queue.isEmpty()) {
            int u = queue.poll();
            tracker.incrementOperation("queue_pops");
            topOrder.add(u);
            visitedCount++;


            for (int[] edge : dag.getAdj(u)) {
                int v = edge[0];
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                    tracker.incrementOperation("queue_pushes");
                }
            }
        }

        tracker.stop();


        if (visitedCount != V) {
            System.err.println("Error: Graph contains a cycle, topological sort not possible.");
            return null;
        }

        return topOrder;
    }
}
