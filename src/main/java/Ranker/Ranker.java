package Ranker;
import DBController.DB_Controller;
import java.util.*;
import java.util.stream.Collectors;

import org.bson.Document;

public class Ranker<T> {
    Graph<T> pageGraph;
    Map<T, Double> pageRanks = new HashMap<T, Double>();
    Map<T, Double> contributionVector = new HashMap<T, Double>();
    double dampingFactor;
    double offset;
    int iterations = 25;
    Map<T, List<T>> adjList;

    public Ranker(Graph<T> pages, double dampingFactor) {
        pageGraph = pages;
        adjList = pageGraph.getAdjList();
        initializePageRanks(1.0 / pageGraph.getVertexCount());
        initializeContribution();
        this.dampingFactor = dampingFactor;
        offset = (1 - dampingFactor) / pageGraph.getVertexCount();
    }

    private void initializePageRanks(double initialValue) {
        for (T vertex : adjList.keySet()) {
            pageRanks.put(vertex, initialValue);
        }
    }

    public void initializeContribution() {

        for (T vertex : adjList.keySet()) {
            contributionVector.put(vertex, (double) adjList.get(vertex).size());
        }
    }

    public void calculatePageRank() {
        double page_rank = 0;
        Map<T, Double> lastPageRank = new HashMap<T, Double>();

        Map<T, Double> currentPageRankMap = null;
        for (int k = 0; k < iterations; k++) {
            currentPageRankMap = new HashMap<T, Double>();

            for (T page : adjList.keySet()) {
                page_rank = 0;
                for (T edge : adjList.keySet()) {
                    if (pageGraph.hasEdge(edge, page))
                        page_rank += (pageRanks.get(edge) / contributionVector.get(edge));
                }
                currentPageRankMap.put(page, offset + dampingFactor * page_rank);
            }
            // Normalize the magnitudes of all ranks
            double sum = currentPageRankMap.values().stream().reduce(0.0, Double::sum);
            double inverseMagnitude = 1.0 / sum;
            for (T p : currentPageRankMap.keySet()) {
                currentPageRankMap.put(p, currentPageRankMap.get(p) * inverseMagnitude);
            }
            lastPageRank = pageRanks;
            pageRanks = currentPageRankMap;
        }

        pageRanks.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEach(System.out::println);
        return;
    }

    private double calculateSingleDocumentRelevance(Set<String> query, HashMap<String, Double> query_vector, HashMap<String, Double> document_vector) {
        double document_word_score = 0d;
        double dot_product = 0d;
        double magnitude = 0d;

        for (String word : query) {
            document_word_score = 0d;
            // Calculate the dot product of both query and document vectors
            // Ex. query_vector = {a:1, b:2, c:3}, document_vector = {a:4, b:2}
            // Dot product = (1 * 4) + (2 * 2) = 8
            // Note that c:3 did not contribute to the dot product because they are not in the document vector
            if (document_vector.containsKey(word)) {
                document_word_score = document_vector.get(word);
                double query_word_score = query_vector.get(word);
                dot_product += (query_word_score * document_word_score);
            }
        }
        // Calculate magnitude of query and document vectors
        double qv_magnitude = Math.sqrt(query_vector.values().stream().mapToDouble(value -> value * value).sum());
        double dv_magnitude = Math.sqrt(document_vector.values().stream().mapToDouble(value -> value * value).sum());
        // Calculate magnitude of the dot product
        magnitude = qv_magnitude * dv_magnitude;
        if (magnitude == 0) {
            return 0d;
        } else {
            return (double) (dot_product / magnitude);
        }
    }

    private HashMap<String, Double> getQueryVector(String[] query) {
        HashMap<String, Double> query_vector = new HashMap<String, Double>();
        // Calculate the count of each word in the query
        for (String word : query) {
            query_vector.put(word, 0d);
        }
        return query_vector;
    }

    /*
     * Calculate the relevance of each page based on TF-IDF
     */
    public LinkedHashMap<String, Double> calculateRelevance(String[] query, int topK) {

        // Documents with TF-IDF scores for each word in the query (if found in the
        // document)
        // Key: URL, Value: HashMap<word, TF-IDF score> (document vector)
        HashMap<String, HashMap<String, Double>> documents_vector = new HashMap<String, HashMap<String, Double>>();

        double IDF = 0d;
        double relevance_score = 0d;
        int TF = 0;
        int DF = 0;
        int element_weight = 0;
        String URL = "";

        // Get unique words form query
        HashMap<String, Double> query_vector = getQueryVector(query);

        // Get information about the query from the database
        Document[] query_documents = DB_Controller.getQueryInfo(query_vector.keySet().toArray(new String[0]));

        for (Document document : query_documents) {
            // Get IDF score for this word
            IDF = Double.parseDouble(document.get("IDF").toString());
            DF = Integer.parseInt(document.get("DF").toString());

            int word_count_in_query = Collections.frequency(Arrays.stream(query).toList(), document.get("word", String.class));
            query_vector.put(document.get("word", String.class), IDF * word_count_in_query);

            // Get URL list with TF in each page
            List<List> urls = document.get("URLS", List.class);
            Double query_word_tfidf = 0d;
            Double document_word_tfidf = 0d;

            // Loop over all the documents where this word was found and calculate the
            // vector for the query in this document
            for (List url_data : urls) {

                // URL of this page
                URL = url_data.get(0).toString();

                // Term frequency in this URL
                TF = Integer.parseInt(url_data.get(1).toString());

                // Element weight where the word was found
                element_weight = Integer.parseInt(url_data.get(2).toString());

                // TODO Calculate the relevance score with element score
                // Calculate TF-IDF score for this document (URL)
                document_word_tfidf = (TF * IDF);
                System.out.println("Word = " + document.get("word", String.class) + " TF = " + TF + " IDF = " + IDF + " element_weight = " + element_weight + " total_word_tf_idf = " + query_word_tfidf);

                // If this document does not exist in the documents vector, create it
                if (documents_vector.get(URL) == null) {
                    documents_vector.put(URL, new HashMap<String, Double>());
                    documents_vector.get(URL).put(document.get("word", String.class), document_word_tfidf);
                } else {
                    Double old_tfidf = 0d;
                    // Add the old TF-IDF score to the word for this document
                    if (documents_vector.get(URL).containsKey(document.get("word", String.class))) {
                        old_tfidf = documents_vector.get(URL).get(document.get("word", String.class));
                        System.out.println(old_tfidf);
                    }
                    // TODO check if adding the TF-IDF of all occurrences of this word
                    documents_vector.get(URL).put(document.get("word", String.class), old_tfidf + document_word_tfidf);
                }

            }

        }
        System.out.println(query_vector);
        System.out.println(documents_vector);

        // Calculate relevance score for each page

        HashMap<String, Double> relevantDocuments = new HashMap<String, Double>();
        for (String doc : documents_vector.keySet()) {
            relevantDocuments.put(doc, calculateSingleDocumentRelevance(Set.of(query), query_vector, documents_vector.get(doc)));
        }
        // Convert the HashMap to a List of Map.Entry objects
        List<Map.Entry<String, Double>> list = new ArrayList<>(relevantDocuments.entrySet());

        // Sort the List of Map.Entry objects using a Comparator that compares the values in descending order
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Create a new LinkedHashMap to store the sorted entries
        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

        // Loop through the sorted List of Map.Entry objects and put each entry into the new LinkedHashMap
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        if (topK > 0) {
            // Return the first k entries from the sorted Map
            LinkedHashMap<String, Double> firstKEntries = new LinkedHashMap<>();
            Iterator<Map.Entry<String, Double>> iterator = sortedMap.entrySet().iterator();
            for (int i = 0; i < topK && iterator.hasNext(); i++) {
                Map.Entry<String, Double> entry = iterator.next();
                firstKEntries.put(entry.getKey(), entry.getValue());
            }
            return firstKEntries;
        } else {
            return sortedMap;
        }
    }


}
