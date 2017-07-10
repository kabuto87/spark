package uk.gov.ros.post;

import uk.gov.ros.core.JsonTransformer;
import static spark.Spark.*;

public class PostResourse {

    private final PostService postService;

    public PostResourse(PostService postService) {
        this.postService = postService;
        setupEndpoints();
    }

    private void setupEndpoints() {
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
