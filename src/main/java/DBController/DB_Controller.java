package DBController;

import Logger_custom.Logger_custom;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;


public class DB_Controller {
    private static final String CRAWLER_RELATIONS_COLLECTION = "crawler_relations";
    private static final String CRAWLER_LINKS_COLLECTION = "crawler_links";
    private static final String TRIGGERS_COLLECTION = "triggers";
    static private final String DATABASE_NAME = "SearchEngine";
    static private final String QUERY_CACHE_COLLECTION = "QueryCache";
    static private final String INDEXER_LINKS_COLLECTION = "WordDocuments";
    static private final Logger_custom logger = new Logger_custom(DB_Controller.class.getPackageName(), null);
    static private final String LOCAL_DB_CONNECTION_STRING = "mongodb://localhost:27017";
    static private final String REMOTE_DB_CONNECTION_STRING = "";
    static MongoClient client = MongoClients.create(LOCAL_DB_CONNECTION_STRING);
    public static MongoDatabase db = client.getDatabase(DATABASE_NAME);
    public static MongoCollection<Document> crawler_links = db.getCollection(CRAWLER_LINKS_COLLECTION);
    public static MongoCollection<Document> crawler_relations = db.getCollection(CRAWLER_RELATIONS_COLLECTION);
    public static MongoCollection<Document> indexer_links = db.getCollection(INDEXER_LINKS_COLLECTION);
    public static MongoCollection<Document> QueryCache = db.getCollection(QUERY_CACHE_COLLECTION);
    public static MongoCollection<Document> triggers = db.getCollection(TRIGGERS_COLLECTION);

    public static void main(String[] args) {
        /*String[] q = {"css", "career"};
        Document[] d = getQueryInfo(q);
        for (Document document : d) {
            System.out.println(document.toJson());
        }*/
       /* String Link = "https://www.w3schools.com/";
        System.out.println(Link);
        Link = Link.endsWith("/") ? Link.substring(0, Link.length() - 1) : Link;
        System.out.println(Link);*/


        //System.out.println("Document size: " + size + " bytes");
    }


    public static synchronized long UploadDocument(String url) {
        long i = crawler_links.countDocuments();
        Document document = new Document();
        document.append("doc_id", i);
        document.append("url", url);
        document.append("is_visited", false);
        crawler_links.insertOne(document);
        return i;
        //System.out.println("Document inserted successfully");

    }

    public static synchronized void UploadCrawlerRelation(String srcdid, String destid) {
        Document document = new Document();
        document.append("src_id", srcdid);
        document.append("dest_id", destid);
        document.append("bid", false);
        crawler_relations.insertOne(document);
    }

    public static boolean is_present(String url) {
        Bson getAllWordsFromQuery = Filters.in("url", url);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        return (It.first() != null);
    }

    public static void is_relation_present(int src_id, int dest_id) {

        Document criteria = new Document();
        criteria.append("src_id", src_id);
        criteria.append("dest_id", dest_id);
        criteria.append("bid", false);
        crawler_relations.findOneAndUpdate(criteria, Updates.set("bid", true));
    }

    public static boolean GetDocStatus(int i) {
        Bson getAllWordsFromQuery = Filters.in("doc_id", i);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        return It.first().get("is_visited").equals(true);
    }

    public static String GetDoc(int i) {
        Bson getAllWordsFromQuery = Filters.in("doc_id", i);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        crawler_links.updateOne(getAllWordsFromQuery, Updates.set("is_visited", true));
        return It.first().get("url").toString();
    }

    public static int Getid(String Link) {
        Bson getAllWordsFromQuery = Filters.in("url", Link);
        FindIterable<Document> It = crawler_links.find(getAllWordsFromQuery);
        return Integer.parseInt(It.first().get("doc_id").toString());
    }

    public static void UpdateCrawlerLinksTrigger() {

        // new date object of current date


        UpdateResult ur = triggers.updateOne(Filters.eq("collection_name", CRAWLER_LINKS_COLLECTION), Updates.set("last_updated", new Date()));
       // System.out.println("hellloooooooooooo");
       // System.out.println(ur.getModifiedCount());
        if (ur.getModifiedCount() == 0) {
            Document document = new Document();
            document.append("collection_name", CRAWLER_LINKS_COLLECTION);
            document.append("last_updated", new Date());
            triggers.insertOne(document);
        }
    }

    public static void DropCollection(String collection_name) {
        db.getCollection(collection_name).drop();
    }

    public static boolean CrawlerCollectionExists() {
        for (String ASD : db.listCollectionNames()) {
            if (ASD.equals(CRAWLER_LINKS_COLLECTION)) {
                return true;
            }
        }
    return false;
    }

   public static String[] getFirstOcuurenceString(String Word , String Link) {
        Bson getFirstOcureence = Filters.in("word", Word);
        String[] returnedlist = new String[2];
       returnedlist[0] = "";
       returnedlist[1] = "";
        FindIterable<Document> It = db.getCollection("WordDocuments").find(getFirstOcureence);
        Object Temp2 = null;
        Object Temp = null;
        List<Document> Temp3 = new ArrayList<>();
        for (Document document : It) {
            Temp = document.get("URLS");
            if (Temp != null) {
                Temp3 = (List) Temp;
                for (Document object : Temp3) {
                    Temp2 = object;

                   if (((Document) Temp2).get("URL_Name").equals(Link)) {
                       returnedlist[0] = ((Document) Temp2).get("URL_Title").toString();
                       returnedlist[1] =  ((Document) Temp2).get("FirstOccurrence").toString();
                        return returnedlist;
                    }
                }

            }
        }

        return returnedlist;
    }


    /**
     * Get query info document [ ].
     *
     * @param query the query
     * @return the document [ ]
     */
    public static Document[] getQueryInfo(String[] query) {

        ArrayList<Document> query_info = new ArrayList<>();

        Bson getAllWordsFromQuery = Filters.in("word", query);
        FindIterable<Document> It = indexer_links.find(getAllWordsFromQuery);
//        logger.warning(It.toString());
        int i = 0;
        for (Document document : It) {
            query_info.add(document);
            i++;
        }
//        query_info.forEach(document -> logger.info(document.toJson()));
        return query_info.toArray(new Document[0]);
    }

    /**
     * Get page relations document [ ].
     *
     * @param links the links
     * @return the document [ ]
     */
    public static Document[] getPageRelations(String[] links) {

        ArrayList<Document> query_info = new ArrayList<>();

        Bson getSources = Filters.in("src_id", links);
        Bson getDestinations = Filters.in("dest_id", links);

        FindIterable<Document> It = crawler_relations.find(Filters.or(getSources, getDestinations));
//        logger.warning(It.toString());
        for (Document document : It) {
            query_info.add(document);
        }
//        query_info.forEach(document -> logger.warning(document.toJson()));
        return query_info.toArray(new Document[0]);
    }

    /**
     * Gets cached query result.
     *
     * @param query the query
     * @return the cached query result if found
     * @return null if not found
     */
    public static HashMap<String, Double> getCachedQueryResult(String[] query) {
        MongoDatabase db = client.getDatabase(DATABASE_NAME);
        MongoCollection col = db.getCollection(QUERY_CACHE_COLLECTION);
        Date last_crawl_date = triggers.find().first().get("last_updated" , Date.class);
        Document document = (Document) col.find(Filters.eq("query", Arrays.asList(query))).first();
        if (document != null) {
            // Check if the query add date is before the last crawl date,
            // if yes, delete it from cache since it is outdated
            if (document.get("date_saved", Date.class).before(last_crawl_date)) {
                logger.warning("Deleting outdated query from cache");
                col.deleteOne(Filters.eq("query", Arrays.asList(query)));
                return null;
            } else {
                ArrayList<Document> pages = document.get("pages", ArrayList.class);
                HashMap<String, Double> pageMap = new HashMap<>();
                for (Document page : pages) {
                    pageMap.put(page.getString("page"), page.getDouble("rank"));
                }
                return pageMap;
            }
        }
        return null;
    }

    /**
     * Cache query result in DB.
     *
     * @param query          the query
     * @param relevant_pages the relevant pages
     */
    public static void cacheQueryResult(String[] query, HashMap<String, Double> relevant_pages) {
//        setLastCrawlDate();
        Document cache_result = new Document().append("query", Arrays.asList(query)).append("pages", new ArrayList<>());

        for (Map.Entry<String, Double> entry : relevant_pages.entrySet()) {
            Document page = new Document();
            page.append("page", entry.getKey());
            page.append("rank", entry.getValue() * 100);
            cache_result.getList("pages", Document.class).add(page);
        }
        cache_result.append("date_saved", new Date());
//        logger.warning(cache_result.toJson());
        if (QueryCache.find(Filters.eq("query", Arrays.asList(query))).first() == null)
            QueryCache.insertOne(cache_result);
        else QueryCache.replaceOne(Filters.eq("query", Arrays.asList(query)), cache_result);
    }



}
