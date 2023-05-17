package Crawler;
import DBController.DB_Controller;
import com.mongodb.DB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainCrawler {

    public static void main(String[] args) throws InterruptedException, MalformedURLException, IOException {
        ArrayList<String> StartingLinks = readfromfile("seed.txt");

        int no_of_threads = Integer.parseInt(args[1]);
        int limit = Integer.parseInt(args[0]);
        if (no_of_threads < 0 || (limit < 100)) {
            System.out.println("Number of threads must be greater than 0 and limit must be greater than 100");
            return;
        }
        StaticArray.setLimit(limit);

        System.out.println(no_of_threads);
        Thread[] CrawlerThreadList = new Thread[Integer.parseInt(args[1])];
        DB_Controller.UpdateCrawlerLinksTrigger();
        if (StaticArray.check_Limit()) {
            DB_Controller.DropCollection("crawler_links");
            DB_Controller.DropCollection("crawler_relations");

            for (String startingLink : StartingLinks) {
                try {
                    StaticArray.add_seed_to_array(startingLink);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (!DB_Controller.CrawlerCollectionExists())
        {
            for (String startingLink : StartingLinks) {
                try {
                    System.out.println("ana hna");
                    StaticArray.add_seed_to_array(startingLink);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }




        // initializing

        for (int i = 0; i < no_of_threads; i++) {
            CrawlerThreadList[i] = new Thread(new CrawlerThread());
            //System.out.println(StartingLinks.get(i));
            CrawlerThreadList[i].setName(String.valueOf(i));
            CrawlerThreadList[i].start();
        }

        //initialize
        long startTime = System.nanoTime();

        for (int i = 0; i < no_of_threads; i++) {
            CrawlerThreadList[i].join();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000000 to get seconds.
        System.out.println("Crawler Uploaded ListSize = " + DB_Controller.crawler_links.countDocuments());
        System.out.println("Crawler Execution time " + duration / 1000000000 + " seconds");
    }

    public static ArrayList<String> readfromfile(String path) {
        ArrayList<String> returnedarray = new ArrayList<String>();
        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                returnedarray.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return returnedarray;
    }


}









