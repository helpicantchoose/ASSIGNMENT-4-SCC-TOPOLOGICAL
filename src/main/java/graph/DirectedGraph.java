package graph;

import java.util.ArrayList;
import java.util.List;


public class DirectedGraph {
    private final int V;
    private List<List<int[]>> adj;


    public DirectedGraph(int V) {
        this.V = V;
        adj = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }
    }


    public void addEdge(int u, int v, int weight) {
        adj.get(u).add(new int[]{v, weight});
    }

    public int getNumVertices() {
        return V;
    }

    public List<int[]> getAdj(int v) {
        return adj.get(v);
    }


    public DirectedGraph getTranspose() {
        DirectedGraph g = new DirectedGraph(V);
        for (int v = 0; v < V; v++) {
            for (int[] edge : adj.get(v)) {
                g.addEdge(edge[0], v, edge[1]);
            }
        }
        return g;
    }
}
