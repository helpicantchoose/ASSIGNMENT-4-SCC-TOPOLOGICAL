package scc;

import graph.DirectedGraph;
import metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KosarajuAlgorithmTest {
    @Test
    void testFindSCCs() {
        DirectedGraph g = new DirectedGraph(5);
        g.addEdge(1, 0, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, 1);
        g.addEdge(0, 3, 1);
        g.addEdge(3, 4, 1);

        KosarajuAlgorithm kosaraju = new KosarajuAlgorithm();
        List<List<Integer>> sccs = kosaraju.findSCCs(g, new PerformanceTracker());

        assertEquals(3, sccs.size());
    }
}
