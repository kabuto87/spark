package uk.gov.ros;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import uk.gov.ros.core.JsonTransformer;
import uk.gov.ros.post.PostService;
import java.net.UnknownHostException;

import static spark.Spark.*;

public class Application {

    private static final String MONGO_HOST = System.getenv("MONGO_HOST");
    private static final String MONGO_PORT = System.getenv("MONGO_PORT");
    private static final String MONGO_DATABASE = System.getenv("MONGO_DATABASE");

    public static void main(String[] args) throws UnknownHostException {
        final MongoClient mongoClient = new MongoClient(MONGO_HOST, Integer.parseInt(MONGO_PORT));
        final DB database = mongoClient.getDB(MONGO_DATABASE);
        final PostService postService = new PostService(database);

        post("/post", "application/json", (request, response) -> {
            postService.create(request.body());
            response.status(201);
            return response;
        });

        get("/post", "application/json", (request, response) ->
                postService.find(), new JsonTransformer()
        );

        get("/post/:id", "application/json", (request, response) ->
                postService.find(request.params(":id")), new JsonTransformer()
        );

        put("/post/:id", "application/json", (request, response) ->
                postService.update(request.params(":id"), request.body()), new JsonTransformer()
        );

        delete("/post/:id", "application/json", (request, response) -> {
            postService.delete(request.params(":id"));
            response.status(202);
            return response;
        });
    }
}
