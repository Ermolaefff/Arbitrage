package MONEYpackage.Graph;

import java.util.LinkedList;

public class CycleFinder {

    private final boolean[] marked;
    private final boolean[] onStack;
    private final DirectedEdge[] edgeTo;
    private LinkedList<DirectedEdge> cyclePath;

    public CycleFinder(WeightedDigraph G) {
        marked = new boolean[G.numberOfVertices()];
        onStack = new boolean[G.numberOfVertices()];
        edgeTo = new DirectedEdge[G.numberOfVertices()];
        for (int vertex = 0; vertex < G.numberOfVertices(); vertex++)
            if (!marked[vertex]) dfs(G, vertex);
    }

    private void dfs(WeightedDigraph G, int from) {    //Deep-First Search
        marked[from] = true;
        onStack[from] = true;
        for (DirectedEdge edge : G.adjacentEdges(from)) {
            int to = edge.to();
            if (hasCycle())
                return;
            else if (!marked[to]) {
                edgeTo[to] = edge;
                dfs(G, to);
            }
            else if (onStack[to]) {
                cyclePath = new LinkedList<>();
                edgeTo[to] = null;
                cyclePath.push(edge);
                for (DirectedEdge x = edgeTo[from]; x != null; x = edgeTo[x.from()])
                    cyclePath.push(x);
                return;
            }
        }
        onStack[from] = false;
    }

    public boolean hasCycle() {
        return cyclePath != null;
    }

    public Iterable<DirectedEdge> cycle() {
        return cyclePath;
    }
    public static void main(String[] args) {

    }
}
