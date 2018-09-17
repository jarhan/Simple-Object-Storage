package storage.service;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import storage.model.Bucket;
import storage.repository.BucketRepository;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BucketServiceImpl implements BucketService {
    @Autowired
    private BucketRepository bucketRepository;

    @Override
    public Iterable<Bucket> findAll() {
        return bucketRepository.findAll();
    }

    @Override
    public String createInternalName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Map<String, Object> createResponse(Bucket bucket) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", bucket.getName());
        response.put("created", bucket.getCreated());
        response.put("modified", bucket.getModified());
        return response;
    }

    @Override
    public Map<String, Object> createResponseList(Map<String, Object> response, Bucket bucket) {
        response.put("objects", new ArrayList<String>());
        return response;
    }

    @Override
    public boolean bucketExist(String name) {
        Bucket bucket = this.findBucketByName(name);
        return bucket != null;
    }

    @Override
    public ResponseEntity<?> createBucket(String name) {
        if (!this.bucketExist(name)) {
            long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
            String uuid = this.createInternalName();
            Bucket b = new Bucket(name, timestamp, timestamp, uuid);
            try {
                this.createBucketDirectory(b);
                this.saveBucket(b);
                Map<String, Object> response = this.createResponse(b);
                return ResponseEntity.ok().body(response);
            }
            catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> deleteBucket(String name) {
        Bucket bucket_to_delete = this.findBucketByName(name);
        if (bucket_to_delete != null) {
            try {
                FileUtils.deleteDirectory(new File("data/" + name));
                this.deleteBucket(bucket_to_delete);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception ex) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> listObjects(String name) {
        Bucket bucket = this.findBucketByName(name);
        System.out.println(bucket);
        if (bucket != null) {
            System.out.println("bucket exists");
            Map<String, Object> response = this.createResponse(bucket);
            Map<String, Object> response_with_objects = this.createResponseList(response, bucket);
            return ResponseEntity.ok().body(response_with_objects);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public boolean createBucketDirectory(Bucket bucket) {
        File newDirectory = new File("data/" + bucket.getName());
        return newDirectory.mkdirs();
    }

    @Override
    public void saveBucket(Bucket bucket) {
        bucketRepository.save(bucket);
    }

    @Override
    public void deleteBucket(Bucket bucket) {
        bucketRepository.delete(bucket);
    }

    @Override
    public Bucket findBucketByName(String name) {
        return bucketRepository.findByName(name);
    }
}
