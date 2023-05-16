package DBController;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;



public class DB_Controller {
    static MongoClient client = MongoClients.create("mongodb+srv://ahmedhussein00:9thNQZQc7hANflRt@sw-backend.ktfxxtz.mongodb.net/?retryWrites=true&w=majority");
    static MongoDatabase db = client.getDatabase("SearchEngine");
    static MongoCollection col = db.getCollection("Indexer");
    public static MongoCollection crawler_links = db.getCollection("crawler_links");
    public static MongoCollection crawler_relations = db.getCollection("crawler_relations");

    public static void main(String[] args) {
        /*String[] q = {"css", "career"};
        Document[] d = getQueryInfo(q);
        for (Document document : d) {
            System.out.println(document.toJson());
        }*/
        String Link = "https://www.w3schools.com/";
        System.out.println(Link);
        Link = Link.endsWith("/") ?  Link.substring(0 , Link.length() -1) : Link;
        System.out.println(Link);
    }

    public static Document[] getQueryInfo(String[] query) {
        ArrayList<Document> query_info = new ArrayList<>();

        Bson getAllWordsFromQuery = Filters.in("word", query);
        FindIterable<Document> It = col.find(getAllWordsFromQuery);
        System.out.println(It.toString());
        int i = 0;
        for (Document document : It) {
            query_info.add(document);
            i++;
        }

//        Arrays.stream(query_info).forEach(document -> System.out.println(document.toJson()));
        query_info.forEach(document -> System.out.println(document.toJson()));
        return query_info.toArray(new Document[0]);
    }
    public static synchronized long UploadDocument(String url)
    {
        long i = crawler_links.countDocuments();
        Document document = new Document();
        document.append("doc_id", i);
        document.append("url", url);
        document.append("is_visited", false);
        crawler_links.insertOne(document);
        return i;
        //System.out.println("Document inserted successfully");

    }

    public static synchronized void UploadCrawlerRelation(String srcdid , String destid)
    {
        Document document = new Document();
        document.append("src_id", srcdid);
        document.append("dest_id", destid);
        document.append("bid", false);
        crawler_relations.insertOne(document);
    }

    public static boolean is_present(String url)
    {
        Bson getAllWordsFromQuery = Filters.in("url", url);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        return  (It.first() != null);
    }

    public static void is_relation_present(int src_id , int dest_id)
    {

        Document criteria = new Document();
        criteria.append("src_id", src_id);
        criteria.append("dest_id", dest_id);
        criteria.append("bid" , false);
        crawler_relations.findOneAndUpdate(criteria , Updates.set("bid" , true));
    }

    public static boolean GetDocStatus(int i)
    {
        Bson getAllWordsFromQuery = Filters.in("doc_id", i);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        return  It.first().get("is_visited").equals(true);
    }
    public static String GetDoc(int i)
    {
        Bson getAllWordsFromQuery = Filters.in("doc_id", i);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        crawler_links.updateOne(getAllWordsFromQuery , Updates.set("is_visited", true));
        return  It.first().get("url").toString();
    }

    public static int Getid(String Link)
    {
        Bson getAllWordsFromQuery = Filters.in("url", Link);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        return  Integer.valueOf(It.first().get("doc_id").toString());
    }
}
