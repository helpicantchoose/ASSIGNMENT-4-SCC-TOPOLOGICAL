package topo;
import graph.DirectedGraph;
import metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class KahnsAlgorithmTest {

    @Test
    void testTopologicalSortOnDAG() {

        DirectedGraph dag = new DirectedGraph(6);
        dag.addEdge(5, 2, 1);
        dag.addEdge(5, 0, 1);
        dag.addEdge(4, 0, 1);
        dag.addEdge(4, 1, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(3, 1, 1);
        KahnsAlgorithm kahns = new KahnsAlgorithm();
        List<Integer> topOrder = kahns.topologicalSort(dag, new PerformanceTracker());

        assertNotNull(topOrder, "Topological sort should not be null for a DAG");
        assertEquals(6, topOrder.size(), "Topological sort should include all vertices");

        Map<Integer, Integer> vertexPositions = new HashMap<>();
        for (int i = 0; i < topOrder.size(); i++) {
            vertexPositions.put(topOrder.get(i), i);
        }

        for (int u = 0; u < dag.getNumVertices(); u++) {
            for (int[] edge : dag.getAdj(u)) {
                int v = edge[0];

                assertTrue(vertexPositions.get(u) < vertexPositions.get(v),
                        "For edge " + u + "->" + v + ", vertex " + u + " does not come before " + v);
            }
        }
    }

    @Test
    void testTopologicalSortOnCyclicGraph() {

        DirectedGraph cyclicGraph = new DirectedGraph(3);
        cyclicGraph.addEdge(0, 1, 1);
        cyclicGraph.addEdge(1, 2, 1);
        cyclicGraph.addEdge(2, 0, 1);
        KahnsAlgorithm kahns = new KahnsAlgorithm();
        List<Integer> topOrder = kahns.topologicalSort(cyclicGraph, new PerformanceTracker());
        assertNull(topOrder, "Topological sort should be null for a graph with a cycle");
    }
}