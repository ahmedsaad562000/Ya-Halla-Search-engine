package PhraseSearcher;

import DBController.DB_Controller;
import Ranker.Ranker;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PhraseSearcher {

    @Deprecated
    public static URLData getFilteredURLContent(String URL, String word, String[] phrase_query) throws IOException {
        URLData URLContent = new URLData();
        try {
            // Get page
            org.jsoup.nodes.Document document = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0").referrer("http://www.google.com").timeout(10000).ignoreHttpErrors(true).get();

            // Select all elements from the document
            Elements elements = document.select("*");

            // Loop on all elements and get the text which contains the word in lowercase
            ExecutorService executor = Executors.newFixedThreadPool(16);
            Future<?>[] futures = new Future[elements.size()];
            ConcurrentLinkedDeque<String> temp = new ConcurrentLinkedDeque<>();
            int total_occurrences = 0;
            for (Element element : elements) {
                executor.submit(() -> {
                    int occurrences = 0;
                    String text = element.text().strip().toLowerCase();
                    if (element.children().isEmpty() && text.contains(word)) {
                        int new_occurrences = countPhraseOccurrences(phrase_query, text.split(" "));
                        if (new_occurrences > 0) {
                            temp.add(element.text());
                            occurrences += (new_occurrences);
                        }
                    }
                    System.out.println(occurrences);
                    return occurrences;
                });
            }
            for (Future<?> future : futures) {
                if (future != null) {
                    total_occurrences += (int) future.get();
                    ;
                }
            }
            if (total_occurrences == 0) return null;
            URLContent.text = (ArrayList<String>) temp.stream().toList();
            URLContent.occurrences = total_occurrences;
            URLContent.URL = URL;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return URLContent;
    }

    public static void main(String[] args) throws IOException {
        String[] phrase_query = {"to", "hoda"};
        String URL_data = "hello x world abcd i am not the phrase you are looking for hello fdasd hello world" + " world hello hello hello hello , world 213asd hlelo hello world world hello world";
        System.out.println(countPhraseOccurrences(phrase_query, URL_data.toLowerCase().split(" ")));

        searchPhrase(phrase_query);
    }

    @Deprecated
    public static int countPhraseOccurrences(String[] phrase_query, String[] URL_data) {
        int count = 0;

        for (int i = 0; i < URL_data.length; i++) {

            int words_found = 0;
            int j = i;
            int k = 0;

            while (k < phrase_query.length) {
                // Skip words with special characters
                if (j < phrase_query.length && URL_data[j].matches("[^a-zA-Z0-9]")) {
                    ++j;
                }
                if (j == URL_data.length) break;
                if (phrase_query[k].equals(URL_data[j])) {
                    ++words_found;
                    ++j;
                } else {
                    break;
                }
                k++;
            }
            if (words_found == phrase_query.length) ++count;
        }
        return count;
    }

    public static HashMap<String, Document> getWordData(String[] phrase_query) {
        Document[] documents = DB_Controller.getQueryInfo(phrase_query);
        HashMap<String, Document> words = new HashMap<>();

        for (Document document : documents) {
            words.put(document.get("word").toString(), document);
        }
        System.out.println(words);
        return words;
    }

    public static PhraseSearchData searchPhrase(String[] phrase_query) throws IOException {

        HashMap<String, Document> wordData = getWordData(phrase_query);
        HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
        HashMap<String, ArrayList> URLOccurrenceText = new HashMap<>();
        //TODO get intersecting links for optimization
//        HashMap<String, ArrayList> intersecting_links = new HashMap<>();
//        // Get every link in the first word
//        for (int i = 0; i < phrase_query.length - 1; i++) {
//            ArrayList<Document> first_word_URLs = wordData.get(phrase_query[i]).get("URLS", ArrayList.class);
//            for (int j = i + 1; j < phrase_query.length; j++) {
//                ArrayList<Document> next_word_URLs = wordData.get(phrase_query[j]).get("URLS", ArrayList.class);
//                for (Document first_word_doc : first_word_URLs) {
//                    for (Document next_word_doc : next_word_URLs) {
//                        String url_1 = first_word_doc.get("URL_Name").toString();
//                        String url_2 = next_word_doc.get("URL_Name").toString();
//                        if (url_1.equals(url_2)) {
//
//                        }
//                    }
//                }
//            }
//        }
        // K: URL, V: occurrences of the phrase in the page
        HashMap<String, Integer> phraseURLs = new HashMap<String, Integer>();
        ArrayList<Document> first_word_URLs = wordData.get(phrase_query[0]).get("URLS", ArrayList.class);

        do {
            // Special case
            if (phrase_query.length == 1) {
                for (Document doc : first_word_URLs) {
                    occurrences.putIfAbsent(doc.get("URL_Name").toString(), 1);
                    URLOccurrenceText.putIfAbsent(doc.get("URL_Name").toString(), new ArrayList<>());
                    URLOccurrenceText.get(doc.get("URL_Name").toString()).add(doc.get("FirstOccurrence").toString());
                }
                break;
            }
            // Compare one word's URLs with the next word
            for (int j = 1; j < phrase_query.length; j++) {
                ArrayList<Document> next_word_URLs = wordData.get(phrase_query[j]).get("URLS", ArrayList.class);
                // Find intersecting links
                for (Document first_word_doc : first_word_URLs) {
                    String first_doc_url = first_word_doc.get("URL_Name").toString();
                    for (Document next_word_doc : next_word_URLs) {
                        String second_word_url = next_word_doc.get("URL_Name").toString();
                        if (Objects.equals(second_word_url, first_doc_url)) {
                            ArrayList<Integer> first_positions = first_word_doc.get("Positions", ArrayList.class);
                            ArrayList<Integer> next_positions = next_word_doc.get("Positions", ArrayList.class);
                            int local_count = 0;
                            // Compare consecutive positions
                            for (int first_pos : first_positions) {
                                for (int next_pos : next_positions) {
                                    if (first_pos + j == next_pos) {
                                        local_count = 1;
                                        URLOccurrenceText.putIfAbsent(first_doc_url, new ArrayList<>());
                                        // Add the text of both words
                                        URLOccurrenceText.get(first_doc_url).add(first_word_doc.get("FirstOccurrence"));
                                        URLOccurrenceText.get(second_word_url).add(first_word_doc.get("FirstOccurrence"));
                                        break;
                                    }
                                }
                            }
                            if (local_count > 0) {
                                int value = phraseURLs.get(first_doc_url) == null ? 0 : phraseURLs.get(first_doc_url);
                                phraseURLs.put(first_doc_url, value + 1);
                            }
                        }
                    }
                }
            }

            for (String URL : phraseURLs.keySet()) {
                if (phraseURLs.get(URL) == phrase_query.length - 1) occurrences.put(URL, phraseURLs.get(URL));
            }
        } while (false);

        System.out.println(occurrences.toString());
        System.out.println("size" + occurrences.size());
        System.out.println("URL occurrence texts" + URLOccurrenceText.toString());
        PhraseSearchData data = new PhraseSearchData();
        data.URL_OccurrenceText = URLOccurrenceText;
        // Set page rank algorithm settings
        Ranker.PageRank_Settings settings = new Ranker.PageRank_Settings();
        settings.iterations = 100;
        settings.dampingFactor = 0.85;
        settings.convergenceThreshold = 0.001;
        settings.final_weight = 0.3f;

        Ranker r = new Ranker();
        r.setPageRankSettings(settings);
        data.ranked_links = r.startPageRank(occurrences.keySet().toArray(new String[0]));
        System.out.println("Before ranking = " + phraseURLs.toString());
        System.out.println("Ranked links = " + data.ranked_links.toString());
        return data;
    }


    public static class PhraseSearchData {
        public HashMap<String, ArrayList> URL_OccurrenceText = new HashMap<>();
        public Map<String, Double> ranked_links = new HashMap<>();
    }

    public static class URLData {
        public String URL;
        public ArrayList<String> text = new ArrayList<>();
        public int occurrences;
    }
}
