import org.tartarus.snowball.ext.porterStemmer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryProcessing
{
    /******************* Variables ********************/
    public static List<String> StopWords;
    public static List<String> SearchWords;
    /************************************************/

    /********************** Utilities Functions **************************/
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
        SearchWords.removeAll(StopWords);
    }
    public static void WordStemming()
    {
        porterStemmer stemmer = new porterStemmer();
        for(int i = 0 ; i < SearchWords.size() ; ++i)
        {
            stemmer.setCurrent(SearchWords.get(i));
            stemmer.stem();
            SearchWords.set(i , stemmer.getCurrent());
        }
    }

    /*********************************************************************/
    public static void main(String [] args) throws IOException {
        ReadStopWords();
        String QuerySearch = "i love football";
        SearchWords = splitWords(QuerySearch);
        RemoveStopWordsFromList();
        WordStemming();
    }
}
