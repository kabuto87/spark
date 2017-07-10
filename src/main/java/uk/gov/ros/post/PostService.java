package uk.gov.ros.post;

import com.google.gson.Gson;
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class PostService {

    private final DB db;
    private final DBCollection collection;

    public PostService(DB db) {
        this.db = db;
        this.collection = db.getCollection("posts");
    }

    public List<Post> find() {
        List<Post> posts = new ArrayList<>();
        DBCursor dbObjects = collection.find();
        while (dbObjects.hasNext()) {
            DBObject dbObject = dbObjects.next();
            posts.add(new Post((BasicDBObject) dbObject));
        }
        return posts;
    }

    public Post find(String id) {
        return new Post((BasicDBObject) collection.findOne(new BasicDBObject("_id", new ObjectId(id))));
    }

    public void create(String body) {
        Post post = new Gson().fromJson(body, Post.class);
        collection.insert(new BasicDBObject("name", post.getName()).append("message", post.getMessage()));
    }

    public Post update(String id, String body) {
        Post post = new Gson().fromJson(body, Post.class);
        collection.update(new BasicDBObject("_id", new ObjectId(id)), new BasicDBObject("$set", new BasicDBObject("message", post.getMessage())));
        return this.find(id);
    }

    public void delete(String id) {
        collection.remove(new BasicDBObject("_id", new ObjectId(id)));
    }
}