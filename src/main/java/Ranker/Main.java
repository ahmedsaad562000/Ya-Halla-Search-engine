package Ranker;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        String[] q = {"iphone","buy","phone"};

        long start = System.currentTimeMillis();
        Ranker r = new Ranker();
        // Set page rank algorithm settings
        Ranker.PageRank_Settings settings = new Ranker.PageRank_Settings();
        settings.iterations = 100;
        settings.dampingFactor = 0.85;
        settings.convergenceThreshold = 0.00001;
        settings.final_weight = 0.1;

        // Set TF-IDF algorithm settings
        Ranker.TFIDF_Settings tfidf_settings = new Ranker.TFIDF_Settings();
        tfidf_settings.topK = 10;
        tfidf_settings.final_weight = 0.7;

        r.setTFIDFSettings(tfidf_settings);
        r.setPageRankSettings(settings);

        long start_ranking = System.currentTimeMillis();
        HashMap<String, Double> results = r.getPageRanks(q);
        long end_ranking = System.currentTimeMillis();

        results.forEach((k, v) -> System.out.println(k + " " + v));

        long end = System.currentTimeMillis();
        System.out.println("Ranker took " + (end - start) / 1000f + " seconds in total");
        System.out.println("Ranking took " + (end_ranking - start_ranking) / 1000f + " seconds net");

    }
}