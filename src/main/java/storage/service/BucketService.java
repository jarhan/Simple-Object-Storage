package storage.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import storage.model.Bucket;

import java.util.Map;

public interface BucketService {

    String createInternalName();

    Map<String, Object> createResponse(Bucket bucket);

    Map<String, Object> createResponseList(Bucket bucket, Map<String, Object> response);

    Iterable<Bucket> findAll();

    boolean bucketExist(String name);

    @ResponseBody
    ResponseEntity<?> createBucket(String name);

    @ResponseBody
    ResponseEntity<?> deleteBucket(String name);

    @ResponseBody
    ResponseEntity<?> listObjects(String name);

    boolean createBucketDirectory(Bucket bucket);

    void saveBucket(Bucket bucket);

    void deleteBucket(Bucket bucket);

    Bucket findBucketByName(String name);

}
