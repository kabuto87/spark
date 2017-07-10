package uk.gov.ros;

import com.mongodb.*;
import uk.gov.ros.post.PostResourse;
import uk.gov.ros.post.PostService;
import java.net.UnknownHostException;

public class Application {

    public static void main(String[] args) throws UnknownHostException {

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("sparkTest");

        new PostResourse(new PostService(db));
    }
}
