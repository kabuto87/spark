package uk.gov.ros.config;

public class Config {

    //SERVER
    public final static String SERVER_IP_ADDRESS = "0.0.0.0";
    public final static int SERVER_PORT = 8081;

    //JETTY
    public final static int SERVICE_MAX_THREADS = 8;
    public final static int SERVICE_MIN_THREADS = 2;
    public final static int SERVICE_TIMEOUT_MILLISECONDS = 30000;

    //SECURITY
    public final static String SECURITY_KEYSTORE_FILEPATH = "";
    public final static String SECURITY_KEYSTORE_PASSWORD = "";
    public final static String SECURITY_TRUSTSTORE_FILEPATH = "";
    public final static String SECURITY_TRUSTSTORE_PASSWORD = "";

    //MULTIPART
    public final static String MULTIPART_TEMP_FILE_LOCATION = "/tmp";
    public final static int MULTIPART_MAX_FILE_SIZE = 100000000;
    public final static int MULTIPART_MAX_REQUEST_SIZE = 100000000;
    public final static int MULTIPART_FILE_SIZE_THRESHOLD = 100000000;

    //MONGO
    public final static String MONGO_HOST = "localhost";
    public final static int MONGO_PORT = 27017;
    public final static String MONGO_DATABASE = "sparkTest";
    public final static String MONGO_COLLECTION = "fs.files";
}
