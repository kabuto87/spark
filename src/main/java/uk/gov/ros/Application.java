package uk.gov.ros;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ros.service.StorageService;

import static spark.Spark.*;
import static uk.gov.ros.config.Config.*;

public class Application {

    private Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

        StorageService storageService = new StorageService();
        Gson gson = new Gson();

        ipAddress(SERVER_IP_ADDRESS);
        port(SERVER_PORT);
        threadPool(
                SERVICE_MAX_THREADS,
                SERVICE_MIN_THREADS,
                SERVICE_TIMEOUT_MILLISECONDS
        );

        before("/*", storageService::authenticate);
        before("/*", storageService::validate);
        get("/health", storageService::healthCheck);
        get("/items", storageService::find);
        get("/item/:id", storageService::findOne);
        post("/item", "multipart/form-data", storageService::store);
        put("/item/:id", "application/json", storageService::update);
        patch("/item/:id", "application/json", storageService::updatePart);
        delete("/item/:id", storageService::delete);
        exception(Exception.class, storageService::handleException);
        notFound("{ \"error\": \"404 Not Found\" }" );
        internalServerError("{ \"error\": \"Internal Server Error\" }");
        after((request, response) -> response.header("Content-Encoding", "gzip") );
    }
}