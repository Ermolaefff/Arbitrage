package MONEYpackage.Algorithms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class StringST<Data> { //String Search Tree
    private Node<Data> root;
    private int size = 0;
    private static final int breadth = 256;
    private static class Node<Data> {
        private Data val = null;
        private final Node<Data>[] next = new Node[breadth];
    }

    public StringST() {
        root = null;
    }

    public Data get(Object key) {
        Node<Data> x = get(root, key.toString(), 0);
        if (x == null) return null;
        return x.val;
    }

    public Data get(String key) {
        Node<Data> x = get(root, key.toString(), 0);
        if (x == null) return null;
        return x.val;
    }

    public void change(String key, Data value) {
        if (contains(key))
            root = put(root, key, value, 0);
    }

    public void put(String key, Data value) {
        if (!contains(key)) {
            size++;
            root = put(root, key, value, 0);
        }
    }

    public boolean contains(Object key) {
        return get(key.toString()) != null;
    }

    private Node<Data> get(Node<Data> currentNode, String key, int depth) {
        if (currentNode == null)
            return null;
        if (key.length() == depth)
            return currentNode;

        char symbol = key.charAt(depth);
        return get(currentNode.next[symbol], key, ++depth);
    }

    private Node<Data> put(Node<Data> currentNode, String key, Data val, int depth) {
        if (currentNode == null)
            currentNode = new Node<>();

        if (key.length() == depth) {
            currentNode.val = val;
            return currentNode;
        }

        char symbol = key.charAt(depth);
        currentNode.next[symbol] = put(currentNode.next[symbol], key, val, depth+1);
        return currentNode;
    }

    public int size() {
        return size;
    }

    public Iterable<String> keys() {
        return keysWithPrefix("");
    }

    public Iterable<String> keysWithPrefix(String prefix) {
        Queue<String> queue = new LinkedList<>();
        collect(get(root, prefix, 0), prefix, queue);
        return queue;
    }

    public Iterable<String> keysThatMatch(String sample) {
        Queue<String> queue = new LinkedList<>();
        collect(root, "", sample, queue);
        return queue;
    }

    public String longestPrefixOf(String string) {
        int length = search(root, string, 0, 0);
        return string.substring(0, length);
    }

    public void delete(String key) {
        root = delete(root, key, 0);
    }

    private void collect(Node<Data> startNode, String prefix, Queue<String> queue) {
        if (startNode == null) return;
        if (startNode.val != null)
            queue.add(prefix);
        for (char c = 0; c < breadth; c++)
            collect(startNode.next[c], prefix + c, queue);
    }

    private void collect(Node<Data> startNode, String prefix, String pattern, Queue<String> queue) {
        int depth = prefix.length();
        if (startNode == null)                                  return;
        if (depth == pattern.length() && startNode.val != null) queue.add(prefix);
        if (depth == pattern.length())                          return;

        char next = pattern.charAt(prefix.length());
        for (char c = 0; c < breadth; c++)
            if (next == '.' || next == c)
                collect(startNode.next[c], prefix+c, pattern, queue);
    }

    private int search(Node<Data> node, String key, int depth, int length) {
        if (node == null) return length;
        if (node.val != null) length = depth;
        if (depth == key.length()) return length;

        char symbol = key.charAt(depth);
        return search(node.next[symbol], key, depth+1, length);
    }

    private boolean isAnyNextKey(Node<Data> x) {
        boolean isAnyKey = false;
        for (int i = 0; i < breadth; i++)
            if (x.next[i] != null) {
                isAnyKey = true;
                break;
            }
        return isAnyKey;
    }

    private Node<Data> delete(Node<Data> x, String key, int depth) {
        if (x == null) return null;
        if (key.length() == depth)
            x.val = null;
        else {
            char c = key.charAt(depth);
            x.next[c] = delete(x.next[c], key, depth+1);
        }
        if (x.val == null && !isAnyNextKey(x)) return null;
        return x;
    }


    public static void main(String[] args) throws IOException {

    }


}


