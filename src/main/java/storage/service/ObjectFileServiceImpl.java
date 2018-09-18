package storage.service;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import storage.model.Bucket;
import storage.model.ObjectFile;
import storage.repository.BucketRepository;
import storage.repository.ObjectFileRepository;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class ObjectFileServiceImpl implements ObjectFileService {
    @Autowired
    private ObjectFileRepository objectFileRepository;

    @Autowired
    private BucketRepository bucketRepository;

    @Override
    public String createInternalName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Iterable<ObjectFile> findAll() {
        return objectFileRepository.findAll();
    }

    private Bucket getBucket(String name) {
        return bucketRepository.findByName(name);
    }

    private long getTimestamp(){
        return new Timestamp(System.currentTimeMillis()).getTime();
    }

    private boolean createObjectFileDirectory(Bucket bucket, ObjectFile objectFile) {
        File newDirectory = new File("data/" + bucket.getName() + "/" +objectFile.getName());
        return newDirectory.mkdirs();
    }

    private boolean objectExist(ArrayList<ObjectFile> objects, String object_name) {
        boolean isExist = false;
        for (ObjectFile object: objects) {
            isExist = isExist || object.getName().equals(object_name);
        }
        return isExist;
    }

    private ObjectFile getObjectByName(ArrayList<ObjectFile> objects, String object_name) {
        for (ObjectFile object: objects) {
            if (object.getName().equals(object_name)) {
                return object;
            }
        }
        return null;
    }

    @Override
    public ResponseEntity<?> createObjectFile(String bucket_name, String object_name) {
        Bucket bucket = this.getBucket(bucket_name);
        if (bucket != null) {
            long timestamp = this.getTimestamp();
            String uuid = this.createInternalName();
            ObjectFile objectFile = new ObjectFile(object_name, timestamp, timestamp, uuid);

            ArrayList<ObjectFile> objects = bucket.getObjects();
            
            if (!this.objectExist(objects, object_name)) {
                try {
                    objects.add(objectFile);
                    bucket.setObjects(objects);
                    bucket.setModified(timestamp);

                    this.createObjectFileDirectory(bucket, objectFile);
                    bucketRepository.save(bucket);

                    return new ResponseEntity<>(HttpStatus.OK);
                } catch (Exception ex) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> deleteObjectFile(String bucket_name, String object_name) {
        System.out.println("delete object");
        try {
            Bucket bucket = this.getBucket(bucket_name);
            ArrayList<ObjectFile> objects = bucket.getObjects();
            System.out.println("objects in bucket " + bucket_name + ":" + objects);
            ObjectFile object_to_delete = this.getObjectByName(objects, object_name);
            System.out.println("object will be deleted: " + object_to_delete);
            if (bucket != null && object_to_delete != null) {
                System.out.println("object to delete is valid!");
                try {
                    objects.remove(object_to_delete);
                    bucket.setObjects(objects);
                    System.out.println("object is deleted:" + objects);

                    FileUtils.deleteDirectory(new File("data/" + bucket_name + "/" + object_name));
                    bucketRepository.save(bucket);

                    return new ResponseEntity<>(HttpStatus.OK);
                } catch (Exception ex) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
