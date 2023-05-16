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

        int arraylength = StartingLinks.size();

        Thread[] CrawlerThreadList = new Thread[arraylength];

        if (StaticArray.check_Limit()) {
            DB_Controller.UpdateCrawlerLinksTrigger();
            DB_Controller.DropCollection("crawler_links");
            DB_Controller.DropCollection("crawler_relations");
        }
        else
        {
            DB_Controller.UpdateCrawlerLinksTrigger();
        }

        // initiaizing

        for (int i = 0; i < arraylength; i++) {
            CrawlerThreadList[i] = new Thread(new CrawlerThread(StartingLinks.get(i)));
            //System.out.println(StartingLinks.get(i));
            CrawlerThreadList[i].setName(String.valueOf(i));
            CrawlerThreadList[i].start();
        }

        //initialize
        long startTime = System.nanoTime();

        for (int i = 0; i < arraylength; i++) {
            CrawlerThreadList[i].join();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000000 to get seconds.
        System.out.println("Crawler Uploaded ListSize = " + DB_Controller.crawler_links.countDocuments());
        System.out.println("Crawler Execution time " + duration / 1000000000 + " seconds");
    }

    public static ArrayList<String> readfromfile(String path) {
        ArrayList<String> returnedarray = new ArrayList<String>();
        String tempdata;
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









