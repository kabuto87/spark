package uk.gov.ros.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;
import uk.gov.ros.config.Config;
import uk.gov.ros.repository.S3Repository;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static uk.gov.ros.config.Config.*;

public class StorageService {

    private MultipartConfigElement multipartConfigElement;
    private MongoClient mongoClient;
    private DB database;
    private DBCollection dbCollection;
//    private GridFS gridFS;
    private S3Repository s3Repository;
    private boolean serviceUp;
    private String bucketName;

    private final Gson gson = new Gson();
    private final JsonParser jsonParser = new JsonParser();
    private final Logger logger = LoggerFactory.getLogger(StorageService.class);

    public StorageService() throws UnknownHostException {
        this.multipartConfigElement = new MultipartConfigElement(
                MULTIPART_TEMP_FILE_LOCATION,
                MULTIPART_MAX_FILE_SIZE,
                MULTIPART_MAX_REQUEST_SIZE,
                MULTIPART_FILE_SIZE_THRESHOLD
        );
        this.mongoClient = new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT);
        this.database = mongoClient.getDB(Config.MONGO_DATABASE);
        this.dbCollection = database.getCollection(Config.MONGO_COLLECTION);
//        this.gridFS = new GridFS(database);


        // AWS S3 CONNECTION

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        AmazonS3 s3 = new AmazonS3Client(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);
        s3.setEndpoint("http://localhost:4572/");
        s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());

        this.bucketName = "my-first-s3-bucket"; //+ UUID.randomUUID();

        s3.createBucket(bucketName);

        this.s3Repository = new S3Repository(s3);
    }

    public boolean authenticate(Request request, Response response) {
        // authServer micro service
        return true;
    }

    public boolean validate(Request request, Response response) {
        // json schema micro service
        return true;
    }

    public JsonElement healthCheck(Request request, Response response) {
        this.serviceUp = true;
        JsonObject serviceStatus = new JsonObject();
        serviceStatus.add("mongo", integrationHealthCheck(!database.command("{ buildInfo: 1 }").equals("")));
        serviceStatus.add("validator", integrationHealthCheck(true));
        serviceStatus.add("auth", integrationHealthCheck(true));
        serviceStatus.add("service", integrationHealthCheck(serviceUp));
        return serviceStatus;
    }

    public List<DBObject> find(Request request, Response response) {
        return dbCollection.find().toArray();
    }

    public Object findOne(Request request, Response response) {
        if (!request.headers("Content-Disposition").equals("attachment")) {
            return dbCollection.findOne(new ObjectId(request.params(":id")));
        } else {
//            GridFSDBFile gridFSDBFile = gridFS.findOne(new ObjectId(request.params(":id")));

            S3Object s3Object = s3Repository.retrieve(bucketName, request.params(":id"));

            HttpServletResponse raw = response.raw();
            response.header("Content-Disposition", "attachment; filename=" + s3Object.getObjectMetadata().getContentDisposition());
            response.type(s3Object.getObjectMetadata().getContentType());
            try {
                raw.getOutputStream().write(IOUtils.toByteArray(s3Object.getObjectContent()));
                raw.getOutputStream().flush();
                raw.getOutputStream().close();
            } catch (Exception e) {

                e.printStackTrace();
            }
            return raw;
        }
    }

    public String store(Request request, Response response) {

        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

        HashMap<String, String> metadata = null;
        Part file = null;
        GridFSInputFile inputFile = null;
        String storageId = null;
        String key = null;
        try {

            metadata = gson.fromJson(
                    jsonParser.parse(IOUtils.toString(request.raw().getPart("metadata")
                            .getInputStream())), new TypeToken<HashMap<String, String>>(){}.getType());

            file = request.raw().getPart("file");

            key = "uk.gov.ros.deeds." + UUID.randomUUID();

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());
            objectMetadata.setUserMetadata(metadata);


            s3Repository.store(bucketName, key, file.getInputStream(), objectMetadata);

//            GRIDFS
//            inputFile = gridFS.createFile(file.getInputStream(), true);
//            storageId = inputFile.getId().toString();
//            inputFile.setFilename(file.getSubmittedFileName());
//            inputFile.setContentType(file.getContentType());
//            inputFile.setMetaData(new BasicDBObject(metadata));
//            inputFile.save();
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }

        return key;
    }

    public Response update(Request request, Response response) {
        WriteResult writeResult = dbCollection.update(
                new BasicDBObject("_id", new ObjectId(request.params(":id"))),
                new BasicDBObject("$set", new BasicDBObject("metadata", request.body()))
        );
        response.body(writeResult.toString());
        return response;
    }

    public Response updatePart(Request request, Response response) {

        JsonObject patch = jsonParser.parse(request.body()).getAsJsonObject();
        String operation = patch.get("operation").getAsString();
        String path = patch.get("path").getAsString();
        String from = patch.get("from").getAsString();
        String value = patch.get("value").getAsString();

        // operations: test, remove, add, replace, move, copy

        WriteResult writeResult = dbCollection.update(
                new BasicDBObject("_id", new ObjectId(request.params(":id"))),
                new BasicDBObject(operation, new BasicDBObject(path, value))
        );

        response.body(writeResult.toString());
        return response;
    }

    public WriteResult delete(Request request, Response response) {
        return dbCollection.remove(new BasicDBObject("_id", new ObjectId(request.params(":id"))));
    }

    public JsonElement handleException(Throwable throwable, Request request, Response response) {
        JsonObject exceptionMessage = new JsonObject();
        exceptionMessage.addProperty("error", throwable.getMessage());
        return exceptionMessage;
    }

    private JsonObject integrationHealthCheck(boolean result) {
        JsonObject jsonObject = new JsonObject();
        if (result) {
            jsonObject.addProperty("status", "UP");
            return jsonObject;
        }
        jsonObject.addProperty("status", "DOWN");
        serviceUp = false;
        return jsonObject;
    }
}
