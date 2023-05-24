package Ranker;

import Logger_custom.Logger_custom;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        String[] q = {"stumbl", "reman","world"};

        Logger_custom logger = new Logger_custom(Ranker.class.getPackageName(), null);
        long start = System.currentTimeMillis();

        Ranker r = new Ranker();

        // Set page rank algorithm settings
        Ranker.PageRank_Settings settings = new Ranker.PageRank_Settings();
        settings.iterations = 100;
        settings.dampingFactor = 0.85;
        settings.convergenceThreshold = 0.001;
        settings.final_weight = 0.3f;

        // Set TF-IDF algorithm settings
        Ranker.TFIDF_Settings tfidf_settings = new Ranker.TFIDF_Settings();
        tfidf_settings.topK = 0;
        tfidf_settings.final_weight = 0.7f;


        r.setTFIDFSettings(tfidf_settings);
        r.setPageRankSettings(settings);

        long start_ranking = System.currentTimeMillis();
        HashMap<String, Double> results = r.getPageRanks(q);

        long end_ranking = System.currentTimeMillis();

        results.forEach((k, v) -> logger.warning(k + ": " + v));

        long end = System.currentTimeMillis();
        logger.config("Ranker took " + (end - start) / 1000f + " seconds in total");
        logger.config("Ranking took " + (end_ranking - start_ranking) / 1000f + " seconds net");
        logger.config("Ranker returned " + results.size() + " results");

    }

    public static HashMap<String, Double> getPageRanks(String[] q) {
        Ranker r = new Ranker();
        Logger_custom logger = new Logger_custom(Ranker.class.getPackageName(), null);

        // Set page rank algorithm settings
        Ranker.PageRank_Settings settings = new Ranker.PageRank_Settings();
        settings.iterations = 100;
        settings.dampingFactor = 0.85;
        settings.convergenceThreshold = 0.001;
        settings.final_weight = 0.3f;

        // Set TF-IDF algorithm settings
        Ranker.TFIDF_Settings tfidf_settings = new Ranker.TFIDF_Settings();
        tfidf_settings.topK = 0;
        tfidf_settings.final_weight = 0.7f;


        r.setTFIDFSettings(tfidf_settings);
        r.setPageRankSettings(settings);

        long start_ranking = System.currentTimeMillis();
        HashMap<String, Double> results = r.getPageRanks(q);
        long end_ranking = System.currentTimeMillis();
        logger.config("Ranking took " + (end_ranking - start_ranking) / 1000f + " seconds net");
        logger.config("Ranker returned " + results.size() + " results");
        // Convert the HashMap to a List of Map.Entry objects
        List<Map.Entry<String, Double>> list = new ArrayList<>(results.entrySet());

        // Sort the List of Map.Entry objects using a Comparator that compares the values in descending order
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Create a new LinkedHashMap to store the sorted entries
        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

        // Loop through the sorted List of Map.Entry objects and put each entry into the new LinkedHashMap
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

}