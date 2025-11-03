package dagsp;

import graph.DirectedGraph;
import metrics.PerformanceTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;


class DagShortestPathTest {

    private DirectedGraph dag;
    private List<Integer> topOrder;

    @BeforeEach
    void setUp() {

        dag = new DirectedGraph(6);
        dag.addEdge(0, 1, 5);
        dag.addEdge(0, 2, 3);
        dag.addEdge(1, 2, 2);
        dag.addEdge(1, 3, 6);
        dag.addEdge(2, 3, 7);
        dag.addEdge(2, 4, 4);
        dag.addEdge(2, 5, 2);
        dag.addEdge(3, 4, -1);
        dag.addEdge(3, 5, 1);


        topOrder = Arrays.asList(0, 1, 2, 3, 4, 5);
    }


    @Test
    void testFindShortestPaths() {
        DagShortestPath dagSp = new DagShortestPath();
        int source = 0;
        Map<Integer, Integer> distances = dagSp.findShortestPaths(dag, topOrder, source, new PerformanceTracker());



        assertEquals(0, distances.get(0));
        assertEquals(5, distances.get(1));
        assertEquals(3, distances.get(2));
        assertEquals(10, distances.get(3));
        assertEquals(7, distances.get(4));
        assertEquals(5, distances.get(5));
    }


    @Test
    void testFindLongestPaths() {
        DagShortestPath dagSp = new DagShortestPath();
        Map<Integer, Integer> distances = dagSp.findLongestPaths(dag, topOrder, new PerformanceTracker());



        assertEquals(0, distances.get(0));
        assertEquals(5, distances.get(1));
        assertEquals(7, distances.get(2));
        assertEquals(14, distances.get(3));
        assertEquals(13, distances.get(4));
        assertEquals(15, distances.get(5));
    }
}
