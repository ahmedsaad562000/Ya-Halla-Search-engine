package Backend;

import Ranker.Ranker;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.sun.source.tree.Tree;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import DBController.DB_Controller;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tartarus.snowball.ext.porterStemmer;

import javax.persistence.Tuple;

import static java.lang.Math.ceil;


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


    @GetMapping("/upload")
    public HashMap<String , Object> upload(
                         @RequestParam("search") String SearchQuery , @RequestParam("phrase") boolean phrase) {



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


        String[] splitted = SearchQuery.split(" ");

        porterStemmer stemmer = new porterStemmer();
        for (int i = 0; i < splitted.length; ++i) {
            stemmer.setCurrent(splitted[i]);
            stemmer.stem();
            splitted[i] = stemmer.getCurrent();
        }



        //Hashmap from ranker


        // Sort By Value

        //Get Keys(Links)
        //demo
        Set<String> keys = new HashSet<>();

        List<SearchResult> results = Get_Search_Results(keys , splitted);

        HashMap<String , Object>  returned_results = new HashMap<>();

        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        returned_results.put("time" , time);
        //map to list



        returned_results.put("results" , results);
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


    List<SearchResult> Get_Search_Results(Set<String> Links , String[] queryWords)
    {

        List<SearchResult> SearchResults = new ArrayList<>();

        for (String link : Links)
        {
            //getTitle from link;
            Document document;

            try {
                document = Jsoup.connect(link)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                        .referrer("http://www.google.com")
                        .get();

            } catch (IOException e) {
                Links.remove(link);
                continue;
            }
            String title = document.title();


            //get FirstOccurence of all words from link;
            String temp;
            StringBuilder occurence = new StringBuilder();
            for (String word : queryWords)
            {
                //getOccurence from word and link;
                 temp = DB_Controller.getFirstOcuurenceString(word, link);
                if (temp.equals(""))
                {
                    continue;
                }
                 occurence.append(temp);
                    occurence.append("\n");
            }


            SearchResults.add(new SearchResult( link ,occurence.toString() , title));

            //GetFirstOccurence
        }


        return SearchResults;
    }
    
    

    
}