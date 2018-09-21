package storage.service;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import storage.model.Bucket;
import storage.model.ObjectFile;
import storage.repository.BucketRepository;
import storage.repository.ObjectFileRepository;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.*;
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

    private Map<String, Object> createResponse(String part_md5, long part_size, Integer part_number) {
        Map<String, Object> response = new HashMap<>();
        response.put("md5", part_md5);
        response.put("length", part_size);
        response.put("partNumber", part_number);
        return response;
    }

    private Map<String, Object> completeResponse(String object_name, String eTag, long length) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", object_name);
        response.put("length", length);
        response.put("eTag", eTag);
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
        try {
            Bucket bucket = bucketRepository.findByName(name);
            if (bucket == null) {
                throw new IllegalArgumentException("InvalidBucket");
            }
            return bucket;
        } catch (Exception ex) {
            System.out.println(ex);
            throw new IllegalArgumentException("InvalidBucket");
        }
    }

    @Override
    public long castContentLength(String part_size) {
        try {
            long casted_size = Long.parseLong(part_size);
            return casted_size;
        } catch (Exception ex) {
            throw new IllegalArgumentException("LengthMismatched");
        }
    }

    @Override
    public Integer castPartNumber(String part_number) {
        try {
            Integer casted_part_number = Integer.valueOf(part_number);
            return casted_part_number;
        } catch (Exception ex) {
            throw new IllegalArgumentException("InvalidPartNumber");
        }
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
        File newDirectory = new File("data/" + bucket.getUuid() + "/" +objectFile.getUuid());
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
        try {
            Bucket bucket = this.getBucket(bucket_name);
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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> deleteObjectFile(String bucket_name, String object_name) {
        try {
            Bucket bucket = this.getBucket(bucket_name);
            ArrayList<ObjectFile> objects = bucket.getObjects();
            Pair<Integer, ObjectFile> pair = this.getObjectByName(objects, object_name);
            ObjectFile object_to_delete = pair.getValue();

            if (bucket != null && object_to_delete != null) {
                try {
                    objects.remove(object_to_delete);
                    bucket.setObjects(objects);
                    long timestamp = this.getTimestamp();
                    bucket.setModified(timestamp);
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

    private String digestAsHex(String file_path) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file_path);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot digest");
        }
    }

    private boolean isMD5Valid(String file_path, String given_md5) {
        try {
            String data_md5 = digestAsHex(file_path);
            return given_md5.equals(data_md5);
        } catch (Exception ex) {
            return true;
        }
    }

    private boolean isContentLengthValid(Integer given_size, Integer data_size) {
        return given_size.equals(data_size);
    }

    private boolean isRequestBodyValid(String file_path, String given_md5) {
        if (!isMD5Valid(file_path, given_md5)) {
            throw new IllegalArgumentException("MD5Mismatched");
        }
        return true;
    }

    @Override
    public ResponseEntity<?> uploadObjectPart(String bucket_name,
                                              String object_name,
                                              Integer part_number,
                                              long part_size,
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

            String filepath = "data/" + bucket.getUuid() + "/" + object.getUuid() + "/" + part_number.toString();
            FileOutputStream fout = new FileOutputStream(filepath);
            ServletInputStream inputStream = request_body.getInputStream();
            IOUtils.copy(inputStream, fout);

            if (isRequestBodyValid(filepath, part_md5)) {
                long timestamp = getTimestamp();

                object.addFilePart(part_number, part_md5, part_size);
                object.setModified(timestamp);

                if (object_index != -1) {
                    ArrayList<ObjectFile> objects = bucket.getObjects();
                    objects.set(object_index, object);
                    bucket.setObjects(objects);
                    bucket.setModified(timestamp);
                    bucketRepository.save(bucket);
                    return ResponseEntity.ok().body(response);
                }
            }
        } catch (Exception ex) {
            Map<String, Object> response_with_error = createResponseWithError(response, ex.getMessage());
            System.out.println("error: "+ex.getMessage());
            return ResponseEntity.badRequest().body(response_with_error);
        }
        Map<String, Object> response_with_error = createResponseWithError(response, "UndefinedError");
        return ResponseEntity.badRequest().body(response_with_error);
    }

    @Override
    public ResponseEntity<?> deleteObjectPart(String bucket_name, String object_name, Integer part_number) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();
            Integer object_index = pair.getKey();
            if (!object.isTicketFlagged() && object.containsFilePart(part_number)) {

                long timestamp = getTimestamp();
                ArrayList<ObjectFile> objects = bucket.getObjects();
                object.removeFilePart(part_number);
                object.setModified(timestamp);
                objects.set(object_index, object);
                bucket.setObjects(objects);
                bucketRepository.save(bucket);

                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private static String calculateChecksumForMultipartUpload(String total_md5) {
        String hex =  total_md5;
        byte raw[] = BaseEncoding.base16().decode(hex.toUpperCase());
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putBytes(raw);
        String digest = hasher.hash().toString();

        return digest;
    }

    @Override
    public ResponseEntity<?> completeObjectUpload(String bucket_name, String object_name) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();
            Integer object_index = pair.getKey();
            Map<Integer, ArrayList<Object>> files_part_data = object.getFile_parts();

            SortedSet<Integer> keys = new TreeSet<>(files_part_data.keySet());
            long total_length = 0;
            StringBuilder total_md5 = new StringBuilder();
            for (Integer key : keys) {
                ArrayList<Object> data = files_part_data.get(key);
                String part_md5 = (String) data.get(0);
                long part_size = (long) data.get(1);

                total_length = total_length + part_size;
                total_md5.append(part_md5);
            }
            String total_md5_string = total_md5.toString();
            String checksum = calculateChecksumForMultipartUpload(total_md5_string);
            String eTag = checksum + "-" + files_part_data.size();

            object.flagTicket();
            object.setFile_length(total_length);

            ArrayList<ObjectFile> objects = bucket.getObjects();
            objects.set(object_index, object);
            bucket.setObjects(objects);
            bucketRepository.save(bucket);

            Map<String, Object> response = completeResponse(object_name, eTag, total_length);
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            Map<String, Object> response = completeResponse(object_name, "", 0);
            Map<String, Object> response_with_error = createResponseWithError(response, ex.getMessage());
            return ResponseEntity.badRequest().body(response_with_error);
        }
    }

    @Override
    public ResponseEntity<?> downloadObjectWithRange(String bucket_name, String object_name, String range, HttpServletResponse response) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();

            if (!object.isTicketFlagged()) {
                throw new IllegalArgumentException("UploadUncomplete");
            }

            System.out.println("range: "+range);
            String[] only_range = range.split("bytes=");
            String[] split_range = only_range[1].split("-");
            long from_byte = Long.parseLong(split_range[0]);
            long to_byte = object.getFile_length();

            if (split_range.length > 1) {
                to_byte = Long.parseLong(split_range[1]);
            }

            System.out.println("to_byte" + to_byte);
            if (from_byte > to_byte) {
                throw new IllegalArgumentException("RangeInvalid");
            }

            String filepath = "data/" + bucket.getUuid() + "/" + object.getUuid();
            System.out.println(filepath);
            response.setHeader("Content-Length", Long.toString(object.getFile_length()));

            Set<Integer> file_parts_key = object.getFile_parts().keySet();
            SortedSet<Integer> keys = new TreeSet<>(file_parts_key);

            List<InputStream> in_list = new ArrayList<>();

            for (Integer key: keys) {
                in_list.add(new FileInputStream(filepath + "/" + key));
            }

            SequenceInputStream in_stream = new SequenceInputStream(Collections.enumeration(in_list));
            OutputStream out = new BufferedOutputStream(response.getOutputStream());

            in_stream.skip(from_byte);
            for (long i = 0L; i < to_byte-from_byte; i++) {
                out.write(in_stream.read());
            }

            in_stream.close();
            out.close();


            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            System.out.println(ex);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> downloadObjectFullRange(String bucket_name, String object_name, HttpServletResponse response) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();

            String range = "bytes=0-" + Long.toString(object.getFile_length() - 1);

            return downloadObjectWithRange(bucket_name, object_name, range, response);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> updateObjectMetadata(String bucket_name, String object_name, String metadata_key, String metadata_value) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();
            Integer object_index = pair.getKey();

            ArrayList<ObjectFile> objects = bucket.getObjects();
            object.updateMetadata(metadata_key, metadata_value);
            objects.set(object_index, object);
            bucket.setObjects(objects);
            bucketRepository.save(bucket);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> deleteObjectMetadata(String bucket_name, String object_name, String metadata_key) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();
            Integer object_index = pair.getKey();

            if (object.containMetatdataKey(metadata_key)) {
                ArrayList<ObjectFile> objects = bucket.getObjects();
                object.removeMetadata(metadata_key);
                objects.set(object_index, object);
                bucket.setObjects(objects);
                bucketRepository.save(bucket);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> getObjectMetadata(String bucket_name, String object_name, String metadata_key) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();
            Map<String, String> response = new HashMap<>();

            if (object.containMetatdataKey(metadata_key)) {
                String metadata_value = object.getMetadataWithKey(metadata_key);
                response.put(metadata_key, metadata_value);
            }
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> getAllObjectMetadata(String bucket_name, String object_name) {
        try {
            Bucket bucket = getBucket(bucket_name);
            Pair<Integer, ObjectFile> pair = getObjectFile(bucket, object_name);
            ObjectFile object = pair.getValue();

            return ResponseEntity.ok().body(object.getAllMetadata());
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
