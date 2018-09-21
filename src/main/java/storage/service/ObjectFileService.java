package storage.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import storage.model.ObjectFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ObjectFileService {

    String createInternalName();

    Iterable<ObjectFile> findAll();

    long castContentLength(String part_size);

    Integer castPartNumber(String part_number);

    @ResponseBody
    ResponseEntity<?> createObjectFile(String bucket_name, String object_name);

    @ResponseBody
    ResponseEntity<?> deleteObjectFile(String bucket_name, String object_name);

    @ResponseBody
    ResponseEntity<?> uploadObjectPart(String bucket_name, String object_name, Integer part_number, long part_size, String part_md5, HttpServletRequest request_body);

    @ResponseBody
    ResponseEntity<?> deleteObjectPart(String bucket_name, String object_name, Integer part_number);

    @ResponseBody
    ResponseEntity<?> completeObjectUpload(String bucket_name, String object_name);

    @ResponseBody
    ResponseEntity<?> downloadObjectWithRange(String bucket_name, String object_name, String range, HttpServletResponse response);

    @ResponseBody
    ResponseEntity<?> downloadObjectFullRange(String bucket_name, String object_name, HttpServletResponse response);

    @ResponseBody
    ResponseEntity<?> updateObjectMetadata(String bucket_name, String object_name, String metadata_key, String metadata_value);

    @ResponseBody
    ResponseEntity<?> deleteObjectMetadata(String bucket_name, String object_name, String metadata_key);

    @ResponseBody
    ResponseEntity<?> getObjectMetadata(String bucket_name, String object_name, String metadata_key);

    @ResponseBody
    ResponseEntity<?> getAllObjectMetadata(String bucket_name, String object_name);
}
