
import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import org.bson.types.ObjectId;
import spark.Response;
import spark.ResponseTransformer;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.stream.Collectors;
import static spark.Spark.*;

public class Application {

    public static void main(String[] args) throws UnknownHostException {

        port(8081);

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB database = mongoClient.getDB("sparkTest");
        DBCollection collection = database.getCollection("posts");

        GridFS gridfs = new GridFS(database);

        before((request, response) -> {
            boolean authenticated = true;
            boolean valid = true;

            // ... call other micro services
            // ... check if authenticated - user info from headers
            // ... check if valid - schema from resources

            if (!authenticated) {
                halt(401, "User not authenticated");
            }

            if (!valid) {
                halt(400, "Request not valid");
            }
        });

        post("/post", "application/json", (request, response) -> {
            request
            Post post = new Gson().fromJson(request.body(), Post.class);
            collection.insert(new BasicDBObject("name", post.getName()).append("message", post.getMessage()));
            response.status(201);
            return response;
        }, new JsonTransformer());

        get("/post", "application/json", (request, response) -> {
            response.status(200);
            return collection.find().toArray().stream().map(Post::new).collect(Collectors.toList());
        }, new JsonTransformer());

        get("/post/:id", "application/json", (request, response) -> {
            response.status(200);
            return new Post(collection.findOne(new ObjectId(request.params(":id"))));
        }, new JsonTransformer());

        put("/post/:id", "application/json", (request, response) -> {
            response.status(200);
            return collection.update(new BasicDBObject("_id", new ObjectId(request.params(":id"))), new BasicDBObject("$set", new BasicDBObject("message", new Gson().fromJson(request.body(), Post.class))));
        }, new JsonTransformer());

        delete("/post/:id", "application/json", (request, response) -> {
            response.status(202);
            return collection.remove(new BasicDBObject("_id", new ObjectId(request.params(":id"))));
        }, new JsonTransformer());
    }

//    public Object handle(Request request, Response response) {
//        MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
//        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
//        Part file = request.raw().getPart("file"); //file is name of the upload form
//    }
}


class Post {

    private String id;
    private String name;
    private String message;

    public Post(DBObject dbObject) {
        this.id = ((ObjectId) dbObject.get("_id")).toString();
        this.name = dbObject.get("name").toString();
        this.message = dbObject.get("message").toString();
    }

    public String getName() { return name; }
    public String getMessage() {
        return message;
    }
}


class JsonTransformer implements ResponseTransformer {

    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        if (model instanceof Response) {
            return gson.toJson(new HashMap<>());
        }
        return gson.toJson(model);
    }

}