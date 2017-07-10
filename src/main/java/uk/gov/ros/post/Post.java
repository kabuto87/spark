package uk.gov.ros.post;

import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;

public class Post {

    private String id;
    private String name;
    private String message;

    public Post(BasicDBObject dbObject) {
        this.id = ((ObjectId) dbObject.get("_id")).toString();
        this.name = dbObject.getString("name");
        this.message = dbObject.getString("message");
    }

    public String getName() { return name; }
    public String getMessage() {
        return message;
    }
}
