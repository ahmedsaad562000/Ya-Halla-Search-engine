package Ranker;

import Logger_custom.Logger_custom;

import java.util.HashMap;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        String[] q = {"career","css","javascript"};

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
        tfidf_settings.topK = 10;
        tfidf_settings.final_weight = 0.7f;

        r.setTFIDFSettings(tfidf_settings);
        r.setPageRankSettings(settings);

        long start_ranking = System.currentTimeMillis();
        HashMap<String, Double> results = r.getPageRanks(q);

        long end_ranking = System.currentTimeMillis();

        results.forEach((k, v) -> System.out.println(k + " " + v));

        long end = System.currentTimeMillis();
        logger.config("Ranker took " + (end - start) / 1000f + " seconds in total");
        logger.config("Ranking took " + (end_ranking - start_ranking) / 1000f + " seconds net");

    }
}