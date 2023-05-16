package Crawler;

import DBController.DB_Controller;

import java.net.MalformedURLException;

public class StaticArray {

   /* static ArrayList<String> hash_Set = new ArrayList<String>();
    static ArrayList<Boolean> bool_hash_set = new ArrayList<Boolean>();*/
    static String pdfregex = "^(https?|ftp)://.*(\\.pdf)$";
    static int Limit = 6000;

    public synchronized static void add_to_array( int srcid , String Link) throws MalformedURLException
    {
        long destid = 0;
        if (Link.startsWith("http") && !Link.matches(pdfregex)) {
            Link = NormalizeURL.normalize(Link);
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
                destid = DB_Controller.UploadDocument(Link);
                DB_Controller.UploadCrawlerRelation(srcid , (int)destid);
            }
        }
    }

    public synchronized static void add_seed_to_array(String Link) throws MalformedURLException
    {
        if (Link.startsWith("https") && !Link.matches(pdfregex)) {

            Link = NormalizeURL.normalize(Link);
            Link = NormalizeURL.linkCleaner(Link);
            DB_Controller.UploadDocument(Link);
        }
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
