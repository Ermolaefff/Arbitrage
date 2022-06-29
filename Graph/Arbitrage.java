package MONEYpackage.Graph;

import MONEYpackage.Enums.TradingMode;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Arbitrage {
    private final double delta;
    private final DirectedEdge[] edgeTo;
    private final double[] distTo;
    private final boolean[] isInTheQueue;
    private Iterable<DirectedEdge> cycle;
    private final Queue<Integer> queue;
    private int numberOfIterations = 0;

    public Arbitrage(WeightedDigraph G, int startVertex, double delta) {
        this.delta = delta;
        edgeTo = new DirectedEdge[G.numberOfVertices()];
        distTo = new double[G.numberOfVertices()];
        isInTheQueue = new boolean[G.numberOfVertices()];
        queue = new LinkedList<>();

        distTo[startVertex] = 1;
        queue.add(startVertex);
        isInTheQueue[startVertex] = true;

        while (!queue.isEmpty() && !hasCycle()) {
            int vertex = queue.poll();
            isInTheQueue[vertex] = false;
            relax(G, vertex);
        }
    }

    private void relax(WeightedDigraph G, int from) {
        for (DirectedEdge edge : G.adjacentEdges(from)) {
            if (hasCycle()) return;
            int to = edge.to();
            double commission = edge.commission();

            double newDist = distTo[from] * edge.weight();
            if (edge.sellMode() != TradingMode.Transfer)   newDist *= (commission - delta);
            else                                           newDist -= commission;

            if (distTo[to] < newDist) {
                distTo[to] = newDist;
                edgeTo[to] = edge;
                if (!isInTheQueue[to]) {
                    queue.add(to);
                    isInTheQueue[to] = true;
                }
            }

            if (numberOfIterations++ % G.numberOfVertices() == 0) {
                findCycle();
                if (hasCycle()) return;
            }
        }
    }

    public double distTo(int v) {
        return distTo[v];
    }

    public boolean hasPathTo(int v) {
        return distTo[v] != 0;
    }

    public Iterable<DirectedEdge> pathTo(int v) {
        if (!hasPathTo(v))
            return null;
        Stack<DirectedEdge> path = new Stack<DirectedEdge>();
        for (DirectedEdge e = edgeTo[v]; e != null; e = edgeTo[e.from()])
            path.push(e);
        return path;
    }

    private void findCycle() {
        int numberOfVertices = edgeTo.length;
        WeightedDigraph G = new WeightedDigraph(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++)
            if (edgeTo[i] != null)
                G.addEdge(edgeTo[i]);
        CycleFinder cycleFinder = new CycleFinder(G);
        cycle = cycleFinder.cycle();
    }

    public Iterable<DirectedEdge> getCycle() {
        return cycle;
    }

    public boolean hasCycle() {
        return cycle != null;
    }

}
