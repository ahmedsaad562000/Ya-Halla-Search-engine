package Ranker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Graph<T> {
    // Adjacency list
    protected Map<T, List<T>> adjList = new HashMap<>();

    public Map<T, List<T>> getAdjList() {
        return adjList;
    }

    public void addVertex(T v) {
        adjList.put(v, new LinkedList<T>());
    }

    public void addEdge(T src, T dest, boolean bidir) {
        // Add source vertex if it doesn't already exist
        if (!adjList.containsKey(src))
            addVertex(src);
        // Add destination vertex if it doesn't already exist
        if (!adjList.containsKey(dest))
            addVertex(dest);

        // Link source to destination
        adjList.get(src).add(dest);
        // If bidirectional edge, add edge from source to destination
        if (bidir)
            adjList.get(dest).add(src);
    }

    public int getVertexCount() {
        return adjList.keySet().size();
    }

    public int getEdgeCount(boolean bidir) {
        int count = 0;
        // Count the number of edges from each vertex
        for (T vertex : adjList.keySet()) {
            count += adjList.get(vertex).size();
        }
        // If the graph is bidirectional, we need to remove half the edges we counted
        if (bidir)
            count /= 2;
        return count;
    }

    public boolean hasVertex(T v) {
        return adjList.containsKey(v);
    }

    public boolean hasEdge(T src, T dest) {
        // Check if adjacency list of source contains an edge to destination vertex
        return adjList.get(src).contains(dest);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (T vertex : adjList.keySet()) {
            stringBuilder.append(vertex.toString() + ": [ ");
            for (T edge : adjList.get(vertex))
                stringBuilder.append(edge.toString()+" ");
            stringBuilder.append("]\n");
        }
        return stringBuilder.toString();
    }
}

