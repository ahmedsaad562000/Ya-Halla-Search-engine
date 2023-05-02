import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.porterStemmer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class Indexer {
    /* Global Variable that will be used inside main and Utilities Functions */
    public static List<Document> Documents = new ArrayList<>();
    public static HashMap<String , WordDocuement> DB = new HashMap<>();
    public static List<String> StopWords;
    public static List<Element> ElementsWithoutChild;
    public static List<String> Words = new ArrayList<>();
    public static HashMap<String , Integer> WordPriority = new HashMap<>();
    /**************************************************************************/
    /****************************** Utilities Function ************************/
    public static List<String> splitWords(String Lines) {
        List<String> words = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(Lines);
        while (match.find()) {
            String Word = match.group();
            if (!Word.matches("[0-9]+") && Word.length() <= 20)
                words.add(Word);
        }
        return words;
    }
    public static void ReadStopWords() throws IOException {
        BufferedReader InputFile = new BufferedReader(new FileReader("stopwords.txt"));
        StopWords = new ArrayList<String>();
        String word = null;
        while((word = InputFile.readLine()) != null)
        {
            StopWords.add(word);
        }
    }
    public static void RemoveStopWordsFromList()
    {
        Words.removeAll(StopWords);
    }
    public static void WordStemming()
    {
        porterStemmer stemmer = new porterStemmer();
        for(int i = 0 ; i < Words.size() ; ++i)
        {
            stemmer.setCurrent(Words.get(i));
            stemmer.stem();
            Words.set(i , stemmer.getCurrent());
        }
    }
    public static int countOccurrences(String word)
    {
        int count = 0;
        for(String tempWord : Words)
        {
            if(tempWord.equals(word))
                ++count;
        }
        return count;
    }
    public static String stemmingOnlyWord(String Word)
    {
        porterStemmer stemmer = new porterStemmer();
        stemmer.setCurrent(Word);
        stemmer.stem();
        return stemmer.getCurrent();
    }
    public static Integer getPriorityOfWord(String tagName)
    {
        Integer Priority = 0;
        if(tagName.equals("h1"))
            Priority = 7;
        else if(tagName.equals("h2"))
            Priority = 6;
        else if(tagName.equals("h3"))
            Priority = 5;
        else if(tagName.equals("h4"))
            Priority = 4;
        else if(tagName.equals("h5"))
            Priority = 3;
        else if(tagName.equals("h6"))
            Priority = 2;
        else if(tagName.equals("title"))
            Priority = 8;
        else
            Priority = 1;
        return Priority;
    }
    public static void InsertIntoHashMap(String URL_Name)
    {
        for(String Word : Words)
        {
            if(!DB.containsKey(Word))
            {
                WordDocuement wordDocuement = new WordDocuement();
                URLDocument urlDocument = new URLDocument();
                wordDocuement.Word = Word;
                wordDocuement.DocumentFrequency++;
                int totalNumOfDocs = DB.size();
                wordDocuement.IDF = Math.log(totalNumOfDocs / wordDocuement.DocumentFrequency);
                urlDocument.TermFrequency++;
                urlDocument.URL_name = URL_Name;
                urlDocument.Priority = WordPriority.get(Word);
                wordDocuement.URLS.add(urlDocument);
                DB.put(Word , wordDocuement);
            }
            else
            {
                boolean urlFound = false;
                WordDocuement wordDocuement = DB.get(Word);
                List<URLDocument> urlDocument = wordDocuement.URLS;
                for(URLDocument urlDocument1 : urlDocument)
                {
                    if (urlDocument1.URL_name.equals(URL_Name)) {
                        urlFound = true;
                        urlDocument1.TermFrequency++;
                        urlDocument1.Priority = WordPriority.get(Word);
                        wordDocuement.URLS = urlDocument;
                        DB.put(Word , wordDocuement);
                        break;
                    }
                }
                if(!urlFound)
                {
                    URLDocument newURL = new URLDocument();
                    wordDocuement.DocumentFrequency++;
                    newURL.TermFrequency++;
                    newURL.URL_name = URL_Name;
                    newURL.Priority = WordPriority.get(Word);
                    wordDocuement.URLS.add(newURL);
                    DB.put(Word , wordDocuement);
                }
            }
        }
    }
    public static void convertHashMapToDocument()
    {
        for(Map.Entry<String, WordDocuement> entry: DB.entrySet())
        {
            String key = entry.getKey();
            WordDocuement document = entry.getValue();
            List<URLDocument> URLS_DOC = new ArrayList<>();
            URLS_DOC = document.URLS;
            URLDocument URL = new URLDocument();
            Document documentEntry = new Document();
            documentEntry.put("word" , document.Word);
            documentEntry.put("DF" , document.DocumentFrequency);
            documentEntry.put("IDF" , document.IDF);
            List<List<String>> URLS = new ArrayList<>();
            for(URLDocument urlDocument : URLS_DOC)
            {
                String urlName = urlDocument.URL_name;
                String termFrequency = Integer.toString(urlDocument.TermFrequency);
                Integer Priority = urlDocument.Priority;
                List<String> urlDetails = new ArrayList<>();
                urlDetails.add(urlName); urlDetails.add(termFrequency); urlDetails.add(String.valueOf(Priority));
                URLS.add(urlDetails);
            }
            documentEntry.put("URLS" , URLS);
            Documents.add(documentEntry);
        }
    }
    /********************************************************************/
    public static void main(String[] args) throws IOException {
        MongoClient client = MongoClients.create("mongodb+srv://testUser:mahmoudKK11@cluster0.eptn6lz.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase db = client.getDatabase("SampleDB");
        MongoCollection col = db.getCollection("SampleCollection");
        ReadStopWords();
        /* Create a Buffer reader to read URL Links */
        BufferedReader fileReader = new BufferedReader(new FileReader("seedsets.txt"));
        String fileName = null;
        while((fileName = fileReader.readLine()) != null)
        {
            try {
                org.jsoup.nodes.Document doc = Jsoup.connect(fileName).get();
                /* This array will contain only HTML text with Elements without Child */
                ElementsWithoutChild = new ArrayList<Element>();
                /* Select all HTML Tags from downloaded Document*/
                Elements elements = doc.select("*");
                /* This loop check if Element has child ignore it --> it only Focus on Elements without child */
                for(Element elements1 : elements)
                {
                    if(elements1.children().isEmpty())
                        ElementsWithoutChild.add(elements1);
                }
                /* List which will contains all words without any duplication */
                for(Element element: ElementsWithoutChild)
                {
                    Integer Priority = getPriorityOfWord(element.tagName());
                    String tempStr = element.text();
                    List<String> tempList = splitWords(tempStr);
                    for(String str: tempList)
                    {
                        String LowerCaseWord = str.toLowerCase();
                        if(!StopWords.contains(LowerCaseWord))
                        {
                            String stemmingWord = stemmingOnlyWord(LowerCaseWord);
                            if(!WordPriority.containsKey(stemmingWord))
                            {
                                WordPriority.put(stemmingWord , Priority);
                            }
                            else
                            {
                                Integer P = WordPriority.get(stemmingWord);
                                if(P > Priority)
                                {
                                    Priority = P;
                                    WordPriority.put(stemmingWord , Priority);
                                }
                            }
                        }
                        Words.add(str.toLowerCase());
                    }
                };
                RemoveStopWordsFromList();
                WordStemming();
                InsertIntoHashMap(fileName);
                convertHashMapToDocument();
                for(Document document : Documents)
                {
                    String wordName = document.get("word").toString();
                    int DocumentFrequency = (int) document.get("DF");
                    double IDF = (double) document.get("IDF");
                    List<List<String>> URLS_Details = (List<List<String>>) document.get("URLS");
                    Bson FilterQuery = Filters.eq("word" , wordName);
                    long count = col.countDocuments(FilterQuery);
                    boolean found = count > 0;
                    if(found) {
                        Bson filter = Filters.eq("word" , wordName);
                        Bson updateOperation = Updates.combine(
                                Updates.set("DF" , DocumentFrequency) ,
                                Updates.set("IDF" , IDF) ,
                                Updates.set("URLS" , URLS_Details)
                        );
                        col.updateOne(filter , updateOperation);
                    }
                    else
                    {
                        col.insertOne(document);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Words.clear();
            ElementsWithoutChild.clear();
            Documents.clear();
            WordPriority.clear();
        }
    }
}

