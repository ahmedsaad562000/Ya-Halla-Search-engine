package Indexer_Threading;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.porterStemmer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static DBController.DB_Controller.db;

public class Indexer_Threading {
    /* Global Variable that will be used inside main and Utilities Functions */
    public static List<String> LinksOfCrawler = new ArrayList<>();
    public static ConcurrentLinkedDeque<Document> Documents = new ConcurrentLinkedDeque<>();
    public static ConcurrentHashMap<String, WordDocuement> DB = new ConcurrentHashMap<>();
    public static List<String> StopWords;

    /**************************************************************************/
    /****************************** Utilities Function ************************/
    public static List<String> splitWords(String Lines) {
        List<String> words = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(Lines);
        while (match.find()) {
            String Word = match.group();
            if (!Word.matches("[0-9]+") && Word.length() <= 20) words.add(Word);
        }
        return words;
    }

    public static void ReadStopWords() throws IOException {
        BufferedReader InputFile = new BufferedReader(new FileReader("stopwords.txt"));
        StopWords = new ArrayList<String>();
        String word = null;
        while ((word = InputFile.readLine()) != null) {
            StopWords.add(word);
        }
    }

    public static void RemoveStopWordsFromList(List<String> Words) {
        Words.removeAll(StopWords);
    }

    public static void WordStemming(List<String> Words) {
        porterStemmer stemmer = new porterStemmer();
        for (int i = 0; i < Words.size(); ++i) {
            stemmer.setCurrent(Words.get(i));
            stemmer.stem();
            Words.set(i, stemmer.getCurrent());
        }
    }

    public static int countOccurrences(String word, List<String> Words) {
        int count = 0;
        for (String tempWord : Words) {
            if (tempWord.equals(word)) ++count;
        }
        return count;
    }

    public static String stemmingOnlyWord(String Word) {
        porterStemmer stemmer = new porterStemmer();
        stemmer.setCurrent(Word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public static Integer getPriorityOfWord(String tagName) {
        Integer Priority = 0;
        if (tagName.equals("h1")) Priority = 7;
        else if (tagName.equals("h2")) Priority = 6;
        else if (tagName.equals("h3")) Priority = 5;
        else if (tagName.equals("h4")) Priority = 4;
        else if (tagName.equals("h5")) Priority = 3;
        else if (tagName.equals("h6")) Priority = 2;
        else if (tagName.equals("title")) Priority = 8;
        else Priority = 1;
        return Priority;
    }

    public static void InsertIntoHashMap(String URL_Name, String title , HashMap<String, Integer> WordPriority, List<String> Words, HashMap<String, String> firstOccurrenceOfWord) {
        Integer index = 0;
        for (String Word : Words) {
            if (!DB.containsKey(Word)) {
                WordDocuement wordDocuement = new WordDocuement();
                URLDocument urlDocument = new URLDocument();
                wordDocuement.Word = Word;
                wordDocuement.DocumentFrequency++;
                int totalNumOfDocs = DB.size();
                wordDocuement.IDF = Math.log(totalNumOfDocs / wordDocuement.DocumentFrequency);
                urlDocument.TermFrequency++;
                urlDocument.URL_name = URL_Name;
                urlDocument.Priority = WordPriority.get(Word);
                // Added //
                urlDocument.WordPosition.add(index);
                urlDocument.firstParagraph = firstOccurrenceOfWord.get(Word);
                urlDocument.title = title;
                //*******//
                wordDocuement.URLS.add(urlDocument);
                DB.put(Word, wordDocuement);
            } else {
                boolean urlFound = false;
                WordDocuement wordDocuement = DB.get(Word);
                List<URLDocument> urlDocument = wordDocuement.URLS;
                for (URLDocument urlDocument1 : urlDocument) {
                    if (urlDocument1.URL_name.equals(URL_Name)) {
                        urlFound = true;
                        urlDocument1.TermFrequency++;
                        urlDocument1.Priority = WordPriority.get(Word);
                        urlDocument1.WordPosition.add(index);
                        wordDocuement.URLS = urlDocument;
                        DB.put(Word, wordDocuement);
                        break;
                    }
                }
                if (!urlFound) {
                    URLDocument newURL = new URLDocument();
                    wordDocuement.DocumentFrequency++;
                    newURL.TermFrequency++;
                    newURL.URL_name = URL_Name;
                    newURL.Priority = WordPriority.get(Word);
                    newURL.WordPosition.add(index);
                    newURL.firstParagraph = firstOccurrenceOfWord.get(Word);
                    newURL.title = title;
                    wordDocuement.URLS.add(newURL);
                    DB.put(Word, wordDocuement);
                }
            }
            ++index;
        }
    }

    public static void convertHashMapToDocument() {

        for (Map.Entry<String, WordDocuement> entry : DB.entrySet()) {
            String key = entry.getKey();
            WordDocuement document = entry.getValue();
            List<URLDocument> URLS_DOC = new ArrayList<>();
            URLS_DOC = document.URLS;
            URLDocument URL = new URLDocument();
            Document documentEntry = new Document();
            List<Document> All_URL_Documents = new ArrayList<>();
            // Check if document has null values
            if (document.URLS == null ||
                    document.URLS.size() == 0 ||
                    document.Word == null ||
                    document.Word.length() == 0 ||
                    document.DocumentFrequency == 0 ||
                    document.IDF == 0) {
                continue;
            }
            documentEntry.put("word", document.Word);
            documentEntry.put("DF", document.DocumentFrequency);
            documentEntry.put("IDF", document.IDF);
            List<List<String>> URLS = new ArrayList<>();
            for (URLDocument urlDocument : URLS_DOC) {
                Document URL_Document = new Document();
                if (urlDocument.URL_name == null ||
                        urlDocument.Priority == null ||
                        urlDocument.WordPosition == null ||
                        urlDocument.firstParagraph == null ||
                        urlDocument.TermFrequency == 0) {
                    continue;
                }
                List<Integer> WordPositions = urlDocument.WordPosition;
                URL_Document.put("URL_Name", urlDocument.URL_name);
                URL_Document.put("URL_Title", urlDocument.title);
                URL_Document.put("TF", urlDocument.TermFrequency);
                URL_Document.put("Priority", urlDocument.Priority);
                URL_Document.put("Positions", urlDocument.WordPosition);
                URL_Document.put("FirstOccurrence", urlDocument.firstParagraph);
                All_URL_Documents.add(URL_Document);
            }
            documentEntry.put("URLS", All_URL_Documents);
            BsonDocument bsonDocument = BsonDocumentWrapper.asBsonDocument(documentEntry, db.getCollection("WordDocuments").getCodecRegistry());
            RawBsonDocument rawBsonDocument = RawBsonDocument.parse(bsonDocument.toJson() );

            int bsonSize = rawBsonDocument.getByteBuffer().remaining();
            if (bsonSize > 16000000) {
                System.out.println("Document is too large");
                continue;
            }
            Documents.add(documentEntry);
        }
    }

    public static void InsertFirstOccurrenceOfString(String paragraph, List<String> word, HashMap<String, String> firstOccurrenceOfWord) {

        if (!paragraph.isEmpty())
        {
            paragraph = paragraph.substring(0,  (paragraph.length() >300) ? 300  :paragraph.length() - 1);
        }

        word.removeAll(StopWords);
        for (String str : word) {
            String tempStr = stemmingOnlyWord(str);
            if (!firstOccurrenceOfWord.containsKey(tempStr.toLowerCase())) {
                firstOccurrenceOfWord.put(tempStr.toLowerCase(), paragraph);
            }
        }
    }

    /********************************************************************/
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("SearchEngine");
        MongoCollection col = db.getCollection("WordDocuments");
        MongoCollection<Document> crawlerLinks = db.getCollection("crawler_links");
        FindIterable<Document> LinksDocuments = crawlerLinks.find();
        try (MongoCursor<Document> cursor = LinksDocuments.iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String url = (String) document.get("url");
                System.out.println(url);
                LinksOfCrawler.add(url);
            }
        }
        ReadStopWords();

        /* Create a Buffer reader to read URL Links */
        BufferedReader fileReader = new BufferedReader(new FileReader("seedsets.txt"));
        String fileName = null;
        int numThreads = 10; // Get the number of available processors
        int chunkSize = LinksOfCrawler.size() / numThreads; // Calculate the chunk size for each thread

        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int startIndex = i * chunkSize;
            final int endIndex = (i == numThreads - 1) ? LinksOfCrawler.size() : (i + 1) * chunkSize;

            threads[i] = new Thread(() -> {

                    for (int k = startIndex; k < endIndex; k++) {

                            String link = LinksOfCrawler.get(k);
                            System.out.println("Thread " + Thread.currentThread().getName());
                            System.out.println("HashMap Size : " + DB.size() + " Link#" + k);
                        org.jsoup.nodes.Document doc = null;
                        try {
                            doc = Jsoup.connect(link).get();
                        } catch (IOException e) {
                            System.out.println("Error :----------------------------------------------- ");
                            continue;
                        }
                        String title = doc.title();
                            /* This array will contain only HTML text with Elements without Child */
                            ArrayList<Element> ElementsWithoutChild = new ArrayList<Element>();
                            List<String> Words = new ArrayList<>();
                            HashMap<String, Integer> WordPriority = new HashMap<>();
                            HashMap<String, String> firstOccurrenceOfWord = new HashMap<>();
//                            List<Document> Documents = new ArrayList<>();

                            /* Select all HTML Tags from downloaded Document*/
                            Elements elements = doc.select("*");
                            /* This loop check if Element has child ignore it --> it only Focus on Elements without child */
                            for (Element elements1 : elements) {
                                String tagName = elements1.tagName();
                                if (tagName.equals("h1") || tagName.equals("h2") || tagName.equals("h3") || tagName.equals("h4")
                                        || tagName.equals("h5") || tagName.equals("h6") || tagName.equals("p")
                                        || tagName.equals("span") || tagName.equals("title") || tagName.equals("li")
                                        || tagName.equals("td") || tagName.equals("th"))
                                    ElementsWithoutChild.add(elements1);
                            }

                            /* List which will contains all words without any duplication */
                            for (Element element : ElementsWithoutChild) {//
                                Integer Priority = getPriorityOfWord(element.tagName());//
                                String tempStr = element.text();//
                                List<String> tempList = splitWords(tempStr);//
                                // Added NOW //
                                InsertFirstOccurrenceOfString(tempStr, tempList, firstOccurrenceOfWord);//
                                for (String str : tempList) {
                                    String LowerCaseWord = str.toLowerCase();//
                                    if (!StopWords.contains(LowerCaseWord)) {
                                        String stemmingWord = stemmingOnlyWord(LowerCaseWord);//
                                        if (!WordPriority.containsKey(stemmingWord)) {
                                            WordPriority.put(stemmingWord, Priority);//
                                        } else {
                                            Integer P = WordPriority.get(stemmingWord);
                                            if (P > Priority) {
                                                Priority = P;
                                                WordPriority.put(stemmingWord, Priority);
                                            }
                                        }
                                    }
                                    Words.add(str.toLowerCase());
                                }
                            }
                            RemoveStopWordsFromList(Words);
                            WordStemming(Words);
                            InsertIntoHashMap(link, title ,WordPriority, Words, firstOccurrenceOfWord);


                    }
                System.out.println("Thread " + Thread.currentThread().getName() + " Finished");
            });
            threads[i].start();

        }
        System.out.println("Main loop finished");
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("join interrupted");
            }
        }
        System.out.println("Finished threads");
        convertHashMapToDocument();
        System.out.println("Finished converting map to document");
        //delete all documents from collection
        col.deleteMany( Filters.eq("word", ""));
        System.out.println("Starting to Fetch to Database");
        col.insertMany(Arrays.asList(Documents.toArray()));

    }
}