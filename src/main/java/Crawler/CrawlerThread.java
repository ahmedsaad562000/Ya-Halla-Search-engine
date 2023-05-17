package Crawler;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;


class CrawlerThread implements Runnable {


    public void run()  {
        Document document;
        String html_link;
        Connection updated_connection;
        int i = 0;



        while (true) {
            updated_connection = Jsoup.newSession();

            try {


                if (StaticArray.checkindex(i)) {
                    html_link = StaticArray.getByindex(i);
                    document = updated_connection.url(html_link)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                            .referrer("http://www.google.com")
                            .timeout(80000)
                            .ignoreHttpErrors(true).get();

                } else {
                    i++;
                    continue;
                }

                //Get links from document object.

                Elements links = document.select("a[href]");
                if (StaticArray.check_Limit()) {
                    break;
                }
                //Iterate links and print link attributes.


                HashSet<String> asd = getRobotText(html_link);
                for (Element link : links) {
                    html_link = link.attr("href");
                    if (asd.contains(html_link)) {
                       // System.out.println("found dup");
                        continue;
                    }
                    //if (check_content_type(html_link)) {
                    //System.out.println("added");
                    StaticArray.add_to_array(i, html_link);
                    //}
                }

                //System.out.println("#from Thread number + " + Thread.currentThread().getName()+ " + Size = " + Crawler.StaticArray.getSize());

                if (i == StaticArray.getSize() - 1) {
                    break;
                }
                i++;

            } catch (Exception e) {
                continue;
            }


        }

    }

    public static HashSet<String> getRobotText(String url) {
        //array of links
        StringBuilder regex = new StringBuilder();
        try {
            //fetch the url.txt
            HttpURLConnection robotFile = (HttpURLConnection) (new URL("https://" + new URL(url).getHost() + "/robots.txt")).openConnection();
            robotFile.addRequestProperty("User-Agent", "Mozilla/4.0");
            //read robot.txt line by line
            Scanner robotScanner = new Scanner(robotFile.getInputStream());
            while (robotScanner.hasNextLine()) {
                //search for user-agent
                String line = robotScanner.nextLine();
                if (line.equals("User-agent: *")) {
                    //scan till end of file or another User-agent
                    while (robotScanner.hasNextLine()) {
                        line = robotScanner.nextLine();
                        //search for disallow
                        if (line.contains("Disallow:"))
                            //get all the disallowed links
                            regex.append(line.replaceAll("Disallow:", ""));
                            //return if u found User-agent
                        else if (line.contains("User-agent:")) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //nothing
        }
        //return after finishing the file
        HashSet<String> arr = new HashSet<String>();
        if (regex.toString().equals(""))
            return arr;
        regex.deleteCharAt(regex.length() - 1);
        arr.addAll(Arrays.asList(regex.toString().split(" ")));
        return arr;
    }
}