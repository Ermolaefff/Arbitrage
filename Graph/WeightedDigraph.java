package MONEYpackage.Graph;

import MONEYpackage.Enums.TradingMode;

import java.util.LinkedList;

public class WeightedDigraph {
    private final int numberOfVertices;
    private int numberOfEdges;
    private final LinkedList<DirectedEdge>[] adjacentEdges;

    public WeightedDigraph(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        this.numberOfEdges = 0;
        adjacentEdges = (LinkedList<DirectedEdge>[]) new LinkedList[numberOfVertices];
        for (int v = 0; v < numberOfVertices; v++)
            adjacentEdges[v] = new LinkedList<>();
    }

    public int numberOfVertices() {
        return numberOfVertices;
    }

    public int numberOfEdges() {
        return numberOfEdges;
    }

    public Iterable<DirectedEdge> adjacentEdges(int vertex) {
        return adjacentEdges[vertex];
    }

    public double weight(int from, int to) {
        for (DirectedEdge e : adjacentEdges[from])
            if (e.to() == to)
                return e.weight();
        return -1;
    }

    public boolean areConnected(int vertex1, int vertex2) {
        for (DirectedEdge e : adjacentEdges[vertex1])
            if (e.to() == vertex2)
                return true;
        return false;
    }

    public TradingMode sellMode(int from, int to) {
        for (DirectedEdge e : adjacentEdges[from])
            if (e.to() == to)
                return e.sellMode();
        throw new RuntimeException("Vertexes arent connected");
    }

    public void addEdge(DirectedEdge edge) {
        adjacentEdges[edge.from()].add(edge);
        numberOfEdges++;
    }

    public Iterable<DirectedEdge> edges() {
        LinkedList<DirectedEdge> bag = new LinkedList<DirectedEdge>();
        for (int v = 0; v < numberOfVertices; v++)
            for (DirectedEdge e : adjacentEdges(v))
                bag.add(e);
        return bag;
    }

    public static void main(String[] args) {

    }
}
