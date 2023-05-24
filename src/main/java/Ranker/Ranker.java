package Ranker;

import DBController.DB_Controller;
import Logger_custom.Logger_custom;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;


public class Ranker {

    private static final Logger_custom logger = new Logger_custom(Ranker.class.getPackageName(), null);
    /**
     * The Page graph.
     */
    Graph<String> pageGraph;
    /**
     * The Page ranks.
     */
    Map<String, Double> pageRanks = new HashMap<>();
    /**
     * The Contribution vector.
     */
    Map<String, Double> contributionVector = new HashMap<>();
    /**
     * The Adj list.
     */
    Map<String, List<String>> adjList;
    /**
     * The Pr settings.
     */
    PageRank_Settings PR_settings;
    /**
     * The Tfidf settings.
     */
    TFIDF_Settings TFIDF_settings;

    /**
     * Instantiates a new Ranker.
     */
    public Ranker() {

    }

    /**
     * Calculate harmonic mean.
     *
     * @param v1 First value
     * @param v2 Second value
     * @return Harmonic mean
     */
    private double calculateHarmonicMean(double v1, double v2) {
        return (v1 * v2) / (v1 + v2);
    }

    /**
     * Combine scores from both TF-IDF & PageRank.
     *
     * @param tfidf    Hashmap of URLs with key being the URL and value being the TF-IDF score
     * @param pagerank Hashmap of URLs with key being the URL and value being the PageRank score
     * @return Hashmap of URLs with key being the URL and value being the combined score between TF-IDF & PageRank
     */
    private HashMap<String, Double> combineScores(HashMap<String, Double> tfidf, HashMap<String, Double> pagerank) {
        HashMap<String, Double> combined_rank = new HashMap<>();
        for (String key : tfidf.keySet()) {
            double tfidf_score = tfidf.get(key) != null ? tfidf.get(key) : 0;
            double pagerank_score = pagerank.get(key) != null ? pagerank.get(key) : 0;
            double combined_score = (TFIDF_settings.final_weight) * tfidf_score + (PR_settings.final_weight * pagerank_score);
//            double combined_score = calculateHarmonicMean(tfidf_score, pagerank_score);
            combined_rank.put(key, combined_score);
        }
        return combined_rank;
    }

    /**
     * @param query          the query to cache
     * @param relevant_pages the relevant pages to cache
     */
    private void cacheQueryResult(String[] query, HashMap<String, Double> relevant_pages) {
        DB_Controller.cacheQueryResult(query, relevant_pages);
    }

    /**
     * Sets page rank settings.
     *
     * @param PR_settings the pr settings
     */
    public void setPageRankSettings(PageRank_Settings PR_settings) {
        this.PR_settings = PR_settings;
    }

    /**
     * Sets tfidf settings.
     *
     * @param TFIDF_settings the tfidf settings
     */
    public void setTFIDFSettings(TFIDF_Settings TFIDF_settings) {
        this.TFIDF_settings = TFIDF_settings;
    }

    /**
     * Gets page ranks.
     *
     * @param query the query
     * @return the page ranks
     */
    public HashMap<String, Double> getPageRanks(String[] query) {

        // Check if the query is already cached in DB
        HashMap<String, Double> query_result = DB_Controller.getCachedQueryResult(query);
        if (query_result != null) {
            logger.info("Query is already cached");
            return query_result;
        } else {
            logger.info("Query is not cached");
            long start_tfidf = System.currentTimeMillis();
            LinkedHashMap<String, Double> relevant_pages = startRelevanceRank(query);
            long end_tfidf = System.currentTimeMillis();
            logger.config("TF-IDF time: " + (end_tfidf - start_tfidf) + "ms");
            logger.info("Relevant pages: " + relevant_pages);
            logger.info("PageRank started");
            long start_pr = System.currentTimeMillis();
            startPageRank(relevant_pages.keySet().toArray(new String[0]));
            long end_pr = System.currentTimeMillis();
            logger.config("TF-IDF time: " + (end_pr - start_pr) + "ms");
            logger.info("PageRank finished");
            logger.info("Page ranks results: " + pageRanks.toString());
            logger.info("TF-IDF results: " + relevant_pages);
            long start_combine = System.currentTimeMillis();
            HashMap<String, Double> combined_rank = combineScores(relevant_pages, (HashMap<String, Double>) pageRanks);
            long end_combine = System.currentTimeMillis();
            logger.config("Combine time: " + (end_combine - start_combine) + "ms");
            logger.info("Combined results: (PR weight = " + PR_settings.final_weight + ", TFIDF weight = " + TFIDF_settings.final_weight + ")\n" + combined_rank);

            cacheQueryResult(query, combined_rank);
//            /////////////////
//            pageRanks.clear();
//            Document[] docs = db_controller.getQueryInfo(query);
//            ArrayList<String> pages = new ArrayList<>();
//            for (Document doc : docs) {
//                List<List> urls = doc.get("URLS", List.class);
//                for (List url : urls) {
//                    pages.add(url.get(0).toString());
//                }
//            }
//            startPageRank(pages.toArray(new String[0]));
//            pageRanks.forEach((k, v) -> logger.info(k + ": " + v + "\n"));
//            /////////////

            return combined_rank;
        }
    }

    /**
     * Initializes the page graph
     *
     * @param pages the page URL array
     */
    private void initializePageRankGraph(String[] pages) {
        pageGraph = new Graph<String>();
        Document[] documents = DB_Controller.getPageRelations(pages);
        for (Document document : documents) {
            pageGraph.addEdge(document.get("src_id", String.class), document.get("dest_id", String.class), false);
        }
    }

    /**
     * Initializes the page rank parameters and graph
     */
    private void initializePageRank() {
        adjList = pageGraph.getAdjList();
        initializePageRanks(1.0 / pageGraph.getVertexCount());
        initializeContribution();
        PR_settings.offset = (1 - PR_settings.dampingFactor) / pageGraph.getVertexCount();
    }

    /**
     * Initializes the page ranks
     *
     * @param initialValue the initial value to initialize the page ranks before iterating
     */
    private void initializePageRanks(double initialValue) {
        for (String vertex : adjList.keySet()) {
            pageRanks.put(vertex, initialValue);
        }
    }

    /**
     * Initializes the contribution vector of each vertex(page)
     */
    private void initializeContribution() {

        for (String vertex : adjList.keySet()) {
            contributionVector.put(vertex, (double) adjList.get(vertex).size());
        }
    }

    /**
     * Calculates the page rank for each page and returns a map with rank of each page.
     */
    private void calculatePageRank() {
        double page_rank;
        Map<String, Double> lastPageRank = new HashMap<>();
        Map<String, Double> currentPageRankMap;
        boolean stop = false;

        for (int k = 0; !stop && (k < PR_settings.iterations); k++) {
            currentPageRankMap = new HashMap<>();
            for (String page : adjList.keySet()) {
                page_rank = 0;
                for (String edge : adjList.keySet()) {
                    if (pageGraph.hasEdge(edge, page))
                        page_rank += (pageRanks.get(edge) / contributionVector.get(edge));
                }
                currentPageRankMap.put(page, PR_settings.offset + PR_settings.dampingFactor * page_rank);
            }
            // Normalize the magnitudes of all ranks
            double sum = currentPageRankMap.values().stream().reduce(0.0, Double::sum);
            double inverseMagnitude = 1.0 / sum;

            // Check for convergence
            for (Map.Entry<String, Double> entry : currentPageRankMap.entrySet()) {
                //Check entry not null
                if (entry.getValue() != null && lastPageRank.get(entry.getKey()) != null) {
                    if (Math.abs(entry.getValue() - lastPageRank.get(entry.getKey())) < PR_settings.convergenceThreshold) {
                        stop = true;
                        logger.info("Converged after " + k + " iterations");
                        break;
                    }
                }
            }

            // Update the page ranks
            currentPageRankMap.replaceAll((p, v) -> v * inverseMagnitude);
            lastPageRank = pageRanks;
            pageRanks = currentPageRankMap;
        }
        // Sort page ranks in descending order
        pageRanks = pageRanks.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        logger.info(pageRanks.toString());
        if (!stop) logger.finer("Iterations reached maximum");
    }

    /**
     * Starts the page rank algorithm.
     *
     * @param pages the pages
     * @return
     */
    public Map<String, Double> startPageRank(String[] pages) {
        initializePageRankGraph(pages);
        initializePageRank();
        calculatePageRank();
        return pageRanks;
    }

    /**
     * Calculate single document relevance based on the IDF values of each word.
     *
     * @param query           the query string array
     * @param query_vector    the query vector
     * @param document_vector the document vector
     * @return the relevance score between the query and document
     */
    private double calculateSingleDocumentRelevance(Set<String> query, HashMap<String, Double> query_vector, HashMap<String, Double> document_vector) {
        double document_word_score;
        double dot_product = 0d;
        double magnitude;

        for (String word : query) {
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
            return dot_product / magnitude;
        }
    }

    /**
     * Creates the query vector from query strings
     *
     * @param query the query string array
     * @return the query vector with key as word and value as 0, all words are unique
     */
    private HashMap<String, Double> getQueryVector(String[] query) {
        HashMap<String, Double> query_vector = new HashMap<>();
        // Initialize the query vector with 0
        for (String word : query) {
            query_vector.put(word, 0d);
        }
        return query_vector;
    }

    /**
     * Sorts a hashmap in descending order based on the values
     *
     * @param sortedMap the map to be sorted
     * @return the sorted map in descending order
     */
    public LinkedHashMap<String, Double> getSortedHashMap(LinkedHashMap<String, Double> sortedMap) {
        if (TFIDF_settings.topK > 0) {
            // Return the first k entries from the sorted Map
            LinkedHashMap<String, Double> firstKEntries = new LinkedHashMap<>();
            Iterator<Map.Entry<String, Double>> iterator = sortedMap.entrySet().iterator();
            for (int i = 0; i < TFIDF_settings.topK && iterator.hasNext(); i++) {
                Map.Entry<String, Double> entry = iterator.next();
                firstKEntries.put(entry.getKey(), entry.getValue());
            }
            return firstKEntries;
        } else {
            return sortedMap;
        }
    }

    /**
     * Start relevance rank linked hash map.
     *
     * @param query the query
     * @return the linked hash map
     */
    private LinkedHashMap<String, Double> startRelevanceRank(String[] query) {
        // Documents with TF-IDF scores for each word in the query (if found in the
        // document)
        // Key: URL, Value: HashMap<word, TF-IDF score> (document vector)
        HashMap<String, HashMap<String, Double>> documents_vector = new HashMap<>();

        double IDF;
        int TF;
        int DF;
        int element_weight;
        String URL;

        // Get unique words form query
        HashMap<String, Double> query_vector = getQueryVector(query);

        // Get information about the query from the database
        Document[] query_documents = DB_Controller.getQueryInfo(query_vector.keySet().toArray(new String[0]));

        for (Document document : query_documents) {
            // Get word count in query
            int word_count_in_query = Collections.frequency(Arrays.stream(query).toList(), document.get("word", String.class));

            // Get IDF score for this word
            IDF = Double.parseDouble(document.get("IDF").toString());
            DF = Integer.parseInt(document.get("DF").toString());

            double document_word_tfidf;
            double query_word_tfidf = IDF * word_count_in_query;

            query_vector.put(document.get("word", String.class), query_word_tfidf);

            // Get URL list with TF in each page
            ArrayList<Document> urls = document.get("URLS", ArrayList.class);

            // Loop over all the documents where this word was found and calculate the
            // vector for the query in this document
            for (Document url_data : urls) {

                // URL of this page
                URL = url_data.get("URL_Name").toString();

                // Term frequency in this URL
                TF = url_data.get("TF", Integer.class);

                // Element weight where the word was found
                element_weight = url_data.get("Priority", Integer.class);

                // TODO Calculate the relevance score with element score
                // Calculate TF-IDF score for this document (URL)
                document_word_tfidf = (TF * IDF) + 0.1 * element_weight;
                // System.out.println("Word = " + document.get("word", String.class) + " TF = " + TF + " IDF = " + IDF + " element_weight = " + element_weight + " total_word_tf_idf = " + document_word_tfidf);

                // If this document does not exist in the documents vector, create it
                if (documents_vector.get(URL) == null) {
                    documents_vector.put(URL, new HashMap<>());
                    documents_vector.get(URL).put(document.get("word", String.class), document_word_tfidf);
                } else {
                    Double old_tfidf = 0d;
                    // Add the old TF-IDF score to the word for this document
                    if (documents_vector.get(URL).containsKey(document.get("word", String.class))) {
                        old_tfidf = documents_vector.get(URL).get(document.get("word", String.class));
                        //System.out.println("old tfidf = " + old_tfidf);
                    }
                    // TODO check if adding the TF-IDF of all occurrences of this word is correct
                    documents_vector.get(URL).put(document.get("word", String.class), old_tfidf + document_word_tfidf);
                }
            }
        }
        logger.info(query_vector.toString());
        logger.info(documents_vector.toString());

        // Calculate relevance score for each page
        Set<String> querySet = new HashSet<>(Arrays.asList(query));
        HashMap<String, Double> relevantDocuments = new HashMap<>();
        for (String doc : documents_vector.keySet()) {
            relevantDocuments.put(doc, calculateSingleDocumentRelevance(querySet, query_vector, documents_vector.get(doc)));
        }
        // Convert the HashMap to a List of Map.Entry objects
        List<Map.Entry<String, Double>> list = new ArrayList<>(relevantDocuments.entrySet());

        // Sort the List of Map.Entry objects using a Comparator that compares the values in descending order
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Create a new LinkedHashMap to store the sorted entries
        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

        // Loop through the sorted List of Map.Entry objects and put each entry into the new LinkedHashMap
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return getSortedHashMap(sortedMap);
    }

    /**
     * The enum Element weight.
     */
    public enum ElementWeight {
        /**
         * Paragraph element weight.
         */
        Paragraph(1),
        /**
         * Heading 6 element weight.
         */
        Heading6(2),
        /**
         * Heading 5 element weight.
         */
        Heading5(3),
        /**
         * Heading 4 element weight.
         */
        Heading4(4),
        /**
         * Heading 3 element weight.
         */
        Heading3(5),
        /**
         * Heading 2 element weight.
         */
        Heading2(6),
        /**
         * Heading 1 element weight.
         */
        Heading1(7),
        /**
         * Title element weight.
         */
        Title(8);
        private final int weight;

        ElementWeight(int id) {
            this.weight = id;
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public int getValue() {
            return weight;
        }
    }

    /**
     * The type Page rank settings.
     */
    public static class PageRank_Settings {
        /**
         * The Iterations.
         */
        public int iterations;
        /**
         * The Damping factor.
         */
        public double dampingFactor;
        /**
         * The Convergence threshold.
         */
        public double convergenceThreshold;
        /**
         * The Final weight.
         */
        public double final_weight;
        /**
         * The Offset.
         */
        public double offset;
    }

    /**
     * The type Tfidf settings.
     */
    public static class TFIDF_Settings {
        /**
         * The Top k.
         */
        public int topK;
        /**
         * The Final weight.
         */
        public double final_weight;
    }


}