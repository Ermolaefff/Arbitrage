package MONEYpackage.Algorithms;

public class Union {
    private final int[] id;
    private final int[] size;
    private final int numberOfElements;

    public Union(int numberOfElements) {
        this.numberOfElements = numberOfElements;
        id = new int[numberOfElements];
        size = new int[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            id[i] = i;
            size[i] = 1;
        }
    }

    public void unite(Integer p, Integer q) {
        if (p == null || q == null)
            return;

        int pRoot = root(p);
        int qRoot = root(q);
        if (pRoot == qRoot) return;

        if (size[pRoot] < size[qRoot]) {
            size[qRoot] += size[pRoot];
            id[pRoot] = qRoot;
        } else {
            size[pRoot] += size[qRoot];
            id[qRoot] = pRoot;
        }
    }

    public int root(int p) {
        while (p != id[p]) p = id[p];
        return p;
    }

    public boolean connected(int p, int q) {
        return root(p) == root(q);
    }

    public int numberOfElements() {
        return numberOfElements;
    }

    public static void main(String[] args) {

    }
}
