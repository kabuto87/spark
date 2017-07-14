package uk.gov.ros.repository;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.InputStream;

public interface StorageRepository {

    /*
     *
     */
    void store(String bucketName, String key, InputStream file, ObjectMetadata metadata);

    /*
     *
     */
    S3Object retrieve(String bucketName, String key);

    /*
     *
     */
    void delete(String bucketName, String key);

    /*
     *
     */
    ObjectListing list(String bucketName, String key);
}
