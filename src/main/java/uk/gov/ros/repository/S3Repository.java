package uk.gov.ros.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import java.io.InputStream;

public class S3Repository implements StorageRepository {

    private AmazonS3 s3;

    public S3Repository(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public void store(String bucketName, String key, InputStream input, ObjectMetadata metadata) {
        s3.putObject(new PutObjectRequest(bucketName, key, input, metadata));
    }

    @Override
    public S3Object retrieve(String bucketName, String key) {
        return s3.getObject(new GetObjectRequest(bucketName, key));
    }

    @Override
    public void delete(String bucketName, String key) {
        s3.deleteObject(bucketName, key);
    }

    @Override
    public ObjectListing list(String bucketName, String prefix) {
        return  s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(prefix));
    }
}
