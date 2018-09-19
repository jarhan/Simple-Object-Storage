package storage.service;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import storage.model.Bucket;
import storage.model.ObjectFile;
import storage.repository.BucketRepository;
import storage.repository.ObjectFileRepository;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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

    private Map<String, Object> createResponse(String part_md5, Integer part_size, Integer part_number) {
        Map<String, Object> response = new HashMap<>();
        response.put("md5", part_md5);
        response.put("length", part_size);
        response.put("partNumber", part_number);
        return response;
    }

    private Map<String, Object> createResponseWithError(Map<String, Object> response, String error) {
        response.put("error", error);
        return response;
    }

    @Override
    public Iterable<ObjectFile> findAll() {
        return objectFileRepository.findAll();
    }

    private Bucket getBucket(String name) {
        Bucket bucket = bucketRepository.findByName(name);
        if (bucket == null) {
            throw new IllegalArgumentException("InvalidBucket");
        }
        return bucket;
    }

    private Pair<Integer, ObjectFile> getObjectByName(ArrayList<ObjectFile> objects, String object_name) {
        for (int i = 0; i < objects.size(); i++) {
            ObjectFile object = objects.get(i);
            if (object.getName().equals(object_name)) {
                return new Pair<>(i, object);
            }
        }
        return new Pair<>(-1, null);
    }

    private Pair<Integer, ObjectFile> getObjectFile(Bucket bucket, String name) {
        ArrayList<ObjectFile> objects = bucket.getObjects();
        Pair<Integer, ObjectFile> pair = getObjectByName(objects, name);
        ObjectFile object = pair.getValue();
        if (object == null) {
            throw new IllegalArgumentException("InvalidObjectName");
        }
        return pair;
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
            isExist = isExist || object.getName().equalsIgnoreCase(object_name);
        }
        return isExist;
    }

    @Override
    public ResponseEntity<?> createObjectFile(String bucket_name, String object_name) {
        Bucket bucket = this.getBucket(bucket_name);
        if (bucket != null) {
            long timestamp = this.getTimestamp();
            String uuid = this.createInternalName();
            ObjectFile objectFile = new ObjectFile(object_name, timestamp, timestamp, uuid);

            ArrayList<ObjectFile> objects = bucket.getObjects();
            
            if (!this.objectExist(objects, object_name) && isObjectNameValid(object_name)) {
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
            Pair<Integer, ObjectFile> pair = this.getObjectByName(objects, object_name);
            ObjectFile object_to_delete = pair.getValue();
            System.out.println("object will be deleted: " + object_to_delete);
            if (bucket != null && object_to_delete != null) {
                System.out.println("object to delete is valid!");
                try {
                    objects.remove(object_to_delete);
                    bucket.setObjects(objects);
                    long timestamp = this.getTimestamp();
                    bucket.setModified(timestamp);
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

    private boolean isObjectNameValid(String object_name) {
        String pattern = "^(?![.])(?!.*[-_.]$).+";
        return Pattern.matches(pattern, object_name);
    }

    private boolean isMD5Valid(byte[] data, String given_md5) {
        try {
            String data_md5 = DigestUtils.md5DigestAsHex(data);
            return given_md5.equals(data_md5);
        } catch (Exception ex) {
            return true;
        }
    }

    private boolean isContentLengthValid(Integer given_size, Integer data_size) {
        return given_size.equals(data_size);
    }

    private boolean isRequestBodyValid(byte[] data, String given_md5, Integer given_size, Integer data_size) {
        if (!isMD5Valid(data, given_md5)) {
            throw new IllegalArgumentException("MD5Mismatched");
        }
        if (!isContentLengthValid(given_size, data_size)) {
            throw new IllegalArgumentException("LengthMismatched");
        }
        return true;
    }

    @Override
    public ResponseEntity<?> uploadObjectPart(String bucket_name,
                                              String object_name,
                                              Integer part_number,
                                              Integer part_size,
                                              String part_md5,
                                              HttpServletRequest request_body) {
        
        Map<String, Object> response = createResponse(part_md5, part_size, part_number);
        
        try {
           // Check Validation of bucket and object
           Bucket bucket = getBucket(bucket_name);
           Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
           ObjectFile object = pair.getValue();
           Integer object_index = pair.getKey();
           if (!object.isPartNumberValidToAdd(part_number)) {
               throw new IllegalArgumentException("InvalidPartNumber");
           }
           if (object.isTicketFlagged()) {
               throw new IllegalArgumentException("Object is flagged as completed");
           }

           String filepath = "data/" + bucket_name + "/" + object_name + "/" + part_number.toString();
           FileOutputStream fout = new FileOutputStream(filepath);
           ServletInputStream inputStream = request_body.getInputStream();
           byte[] bytes = IOUtils.toByteArray(inputStream);
           Integer data_size = bytes.length;

            if (isRequestBodyValid(bytes, part_md5, part_size, data_size)) {
               IOUtils.copy(inputStream, fout);
               object.addFilePart(part_number);
               ArrayList<ObjectFile> objects = bucket.getObjects();
               if (object_index != -1) {
                   objects.set(object_index, object);
                   bucket.setObjects(objects);
                   bucketRepository.save(bucket);
                   return ResponseEntity.ok().body(response);
               }
           }
       } catch (Exception ex) {
           Map<String, Object> response_with_error = createResponseWithError(response, ex.getMessage());
           return ResponseEntity.badRequest().body(response_with_error);
       }
        Map<String, Object> response_with_error = createResponseWithError(response, "UndefinedError");
        return ResponseEntity.badRequest().body(response_with_error);
    }
}
