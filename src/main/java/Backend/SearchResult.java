package Backend;


import javax.persistence.Entity;
import javax.persistence.Id;


public class SearchResult {
    private String Url;

    private String Occurence;

    private String Title;



    public SearchResult(String url, String occurence , String title) {
        Title = title;
        Url = url;
        Occurence = occurence;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getOccurence() {
        return Occurence;
    }

    public void setOccurence(String occurence) {
        Occurence = occurence;
    }


}