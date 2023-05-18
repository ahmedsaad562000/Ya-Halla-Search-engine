package Backend;

import PhraseSearcher.PhraseSearcher;
import Ranker.Main;
import DBController.DB_Controller;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.bind.annotation.*;
import org.tartarus.snowball.ext.porterStemmer;

import java.io.IOException;
import java.util.*;


@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public String getEmployeesById(@PathVariable String id) {
        return "ID: " + id;
    }


    // make a post request to /api/employees/{id}


    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/upload")
    public HashMap<String, Object> upload(
            @RequestParam("search") String SearchQuery, @RequestParam("phrase") boolean phrase , @RequestParam("page") int page ) {



        /*

        HashMap<Double, SearchResult> map = new HashMap<>();

        for (double i = 100; i >= 0; i-=0.5) {
            map.put(i ,new SearchResult("Link" + i  , "Paragraph" + i  , "Title" + i  ));
        }
        TreeMap<Double, SearchResult> asd22 = new TreeMap<>(map);
        double time = 0.125;

        HashMap<String , Object>  asd = new HashMap<>();
        asd.put("time" , time);
        //map to list

        List <SearchResult> list = new ArrayList<>();
        for (Map.Entry<Double, SearchResult> m : asd22.entrySet()) {
            list.add(m.getValue());
        }


        asd.put("results" , list);
        return asd;*/


        long start = System.currentTimeMillis();

        HashMap<String, Object> returned_results = new HashMap<>();
        String[] splitted = SearchQuery.split(" ");

        porterStemmer stemmer = new porterStemmer();
        for (int i = 0; i < splitted.length; ++i) {
            stemmer.setCurrent(splitted[i]);
            stemmer.stem();
            splitted[i] = stemmer.getCurrent();
           // System.out.println(splitted[i]);
        }
        Set<String> links;

        if (phrase) {
            PhraseSearcher.PhraseSearchData phraseSearch = new PhraseSearcher.PhraseSearchData();
            try {
                phraseSearch = PhraseSearcher.searchPhrase( splitted);
            } catch (IOException e) {
                System.out.println("error in reaading in phrase searching");
            }
            //System.out.println(phraseSearch.ranked_links.values());
            //System.out.println(phraseSearch.ranked_links.keySet());
             links = phraseSearch.ranked_links.keySet();
            List<SearchResult> results = new ArrayList<>();
            int i = 0;
            for    (String link : links) {

                if (!((i >= ((page-1)*10)  && i < (page*10)))) {
                    i++;
                    continue;
                }


                if (phraseSearch.URL_OccurrenceText.get(link) == null)
                {continue;}
                i++;
                results.add(new SearchResult( link, phraseSearch.URL_OccurrenceText.get(link).toString().replace("[" , "").replace("]" , ""), phraseSearch.link_title.get(link)));
            }
            long end = System.currentTimeMillis();
            double time = (end - start) / 1000.0;
            returned_results.put("number", links.size()/10);
            returned_results.put("time", time);
            returned_results.put("results", results);
        }
        else {
            //Hashmap from ranker
            HashMap<String, Double> ranker_map = Main.getPageRanks(splitted);

            // System.out.println(ranker_map);

            // Sort By Value


            //Get Keys(Links)
            //demo
            links = ranker_map.keySet();
            //keys.add("https://edition.cnn.com");
            //demo
            List<SearchResult> results = Get_Search_Results(links, splitted , page);



            long end = System.currentTimeMillis();
            double time = (end - start) / 1000.0;
            returned_results.put("time", time);
            //map to list
            returned_results.put("number", links.size()/10);

            returned_results.put("results", results);
        }
        System.out.println(returned_results);
        return returned_results;
    }




    /*@Autowired
    private SearchResultRepo SearchResultRepository;
    @GetMapping(value = "/blogPageable")
    Page blogPageable(Pageable pageable) {
        return SearchResultRepo.findAll(pageable);
    }*/
/*public List<Map<Double, SearchResult>> split(Map<Double, SearchResult> original) {
        int max = 10;
        int counter = 0;
        int lcounter = 0;
        List<Map<Double, SearchResult>> listOfSplitMaps = new ArrayList<> ();
        Map<Double, SearchResult> splitMap = new HashMap<> ();

        for (Map.Entry<Double, SearchResult> m : original.entrySet()) {
            if (counter < max) {
                splitMap.put(m.getKey(), m.getValue());
                counter++;
                lcounter++;

                if (counter == max || lcounter == original.size()) {
                    counter = 0;
                    listOfSplitMaps.add(splitMap);
                    splitMap = new HashMap<> ();
                }
            }
        }

        return listOfSplitMaps;
    }*/


    List<SearchResult> Get_Search_Results(Set<String> Links, String[] queryWords , int page) {

        List<SearchResult> SearchResults = new ArrayList<>();
        List <String> Links_List = new ArrayList<>();
        int i = 0;
        for (String link : Links) {



            /////////enhancement////////

            if (!((i >= ((page-1)*10)  && i < (page*10)))) {
                i++;
                continue;
            }

            /////////enhancement////////




            //getTitle from link;
            Document document;

            /*try {
                document = Jsoup.connect(link)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                        .referrer("http://www.google.com")
                        .get();

            } catch (IOException e) {
                //Links.remove(link);
                continue;
            }*/
            String title = "";


            //get FirstOccurence of all words from link;
            String[] temp = new String[2];
            HashSet occurence = new HashSet<>();
            for (String word : queryWords) {
                //getOccurence from word and link;
                temp = DB_Controller.getFirstOcuurenceString(word, link);
                if (temp[1].equals("")) {

                    continue;
                }

                if (!temp[0].equals(""))
                {
                    title = temp[0];
                }
                occurence.add(temp[1] + "\n");
            }

            String Occurenssce = occurence.toString().replace("[" , "").replace("]" , "");
            if (link.isEmpty() || Occurenssce.isEmpty() || title.isEmpty()) {
                continue;
            }
            i++;
            SearchResults.add(new SearchResult(link, occurence.toString().replace("[" , "").replace("]" , ""), title));
            //GetFirstOccurence
        }



        return SearchResults;
    }


}