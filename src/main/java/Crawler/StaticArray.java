package Crawler;
import DBController.DB_Controller;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;

public class StaticArray {

    /* static ArrayList<String> hash_Set = new ArrayList<String>();
     static ArrayList<Boolean> bool_hash_set = new ArrayList<Boolean>();*/
    static String pdfregex = "^(https?|ftp)://.*(\\.pdf)$";
    static int Limit = 6000;

    public synchronized static void add_to_array( int srcid , String Link)
    {

        String srcLink = "";
        if (Link.startsWith("https") && !Link.startsWith("http://www.foodnetworklatam") &&!Link.startsWith("http://www.foodnetworktv")  && !Link.startsWith("https://www.pinterest.com") &&!Link.startsWith("https://twitter") && !Link.startsWith("https://auth") &&  !Link.startsWith("https://www.linkedin") && !Link.matches(pdfregex)) {
            try {
                Link = NormalizeURL.normalize(Link);
            }
            catch (MalformedURLException e) {
                System.out.println("not accessible");
                return;
            }










            Link = NormalizeURL.linkCleaner(Link);
            /*if (!Crawler.NormalizeURL.isAccessable(Link , 60000 )) {
                System.out.println("not accessible");
                return;
            }*/
            if (DB_Controller.is_present(Link)) {
                //Nothing

            }
            else
            {
                srcLink = DB_Controller.GetDoc(srcid);
                DB_Controller.UploadDocument(Link);
                DB_Controller.UploadCrawlerRelation(srcLink , Link);
            }
        }
    }

    public synchronized static void add_seed_to_array(String Link) throws MalformedURLException
    {
        if (Link.startsWith("https") && !Link.startsWith("https://auth") && !Link.startsWith("https://www.linkedin") && !Link.matches(pdfregex)) {

            try {
                Link = NormalizeURL.normalize(Link);
            }
            catch (MalformedURLException e) {
                System.out.println("not accessible");
                return;
            }
            Link = NormalizeURL.linkCleaner(Link);
            DB_Controller.UploadDocument(Link);
        }
    }

    public static void setLimit(int limit) {
        Limit = limit;
    }

    public static boolean check_Limit()
    {
        return (DB_Controller.crawler_links.countDocuments() >= Limit);
    }

    /*public static ArrayList<String> getHash_Set() {
        return hash_Set;
    }*/

    public static long getSize()
    {
        return DB_Controller.crawler_links.countDocuments();
    }

    public static String getByindex(int i) {
        /*bool_hash_set.set(i , true);
        return hash_Set.get(i);*/
        return DB_Controller.GetDoc(i);
    }
    public static boolean checkindex(int i) {


        return !DB_Controller.GetDocStatus(i);


    }



}
