package Ranker;

import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {

        Graph<Character> graph = new Graph<Character>();
        graph.addEdge('A', 'D', false);
        graph.addEdge('A', 'E', false);
        graph.addEdge('A', 'F', false);

        graph.addEdge('B', 'A', false);
        graph.addEdge('B', 'E', false);
        graph.addEdge('B', 'C', false);

        graph.addEdge('E', 'C', false);

        graph.addEdge('F', 'D', false);


        System.out.println(graph);
        Ranker r = new Ranker(graph, 0.85);
        r.calculatePageRank();
        String[] q = {"css", "onlin", "trademark", "healthi", "lol"};
        LinkedHashMap<String, Double> x = r.calculateRelevance(q, 0);
        System.out.println(x);

    }
}