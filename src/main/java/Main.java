import com.mongodb.client.*;
import org.bson.Document;

public class Main {
    public static void main(String[] args)
    {
        MongoClient client = MongoClients.create("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase db = client.getDatabase("SampleDB");
        MongoCollection col = db.getCollection("SampleCollection");
//        Document sampleDoc2 = new Document("_id" , "533").append("name" , "Hazem H");
//        col.insertOne(sampleDoc2);
        Document Query = new Document("name" , "Hazem H");
        FindIterable<Document> It = col.find(Query);
        for (Document document : It) {
            // do something with the document
            String name = document.getString("name");
            Integer ID = Integer.parseInt(document.getString("_id"));
            if(ID == 533)
                System.out.println("True ID");
            System.out.println(name);
            System.out.println(document.toJson());
        }
    }
}