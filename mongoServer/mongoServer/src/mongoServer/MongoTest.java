package mongoServer;

import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoTest {
	String speech;
	String sysdate;
	
	public MongoTest(String speech, String sysdate) {
		super();
		this.speech = speech;
		this.sysdate = sysdate;
		
		String MongoDB_IP="127.0.0.1";
		int MongoDB_PORT=27017;
		String DB_NAME="testDB";
		
		//Connect to MongoDB
		MongoClient mongoClient=new MongoClient(new ServerAddress(MongoDB_IP, MongoDB_PORT));
		
		//View Database List
		List<String> databases=mongoClient.getDatabaseNames();
		
		System.out.println("====Database List======");
		int num=1;
		for(String dbName:databases) {
			System.out.println(num+". "+dbName);
			num++;
		}
		
		System.out.println();
		
		//Connect Database and Show Collection List in Database
		DB db=mongoClient.getDB(DB_NAME);
		Set<String> collections=db.getCollectionNames();
		
		System.out.println("Database: "+DB_NAME);
		for(String colName:collections) {
			System.out.println("+Collection: "+colName);
		}
		
		MongoDatabase database=mongoClient.getDatabase(DB_NAME);
		MongoCollection<Document> collection=database.getCollection("speechData");
		
		Document doc=new Document("speech",speech).append("sysdata", sysdate);
		collection.insertOne(doc);
	}
	
	
	
	
}
