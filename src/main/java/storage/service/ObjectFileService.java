package storage.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import storage.model.ObjectFile;

import javax.servlet.http.HttpServletRequest;

public interface ObjectFileService {

    String createInternalName();

    Iterable<ObjectFile> findAll();

    @ResponseBody
    ResponseEntity<?> createObjectFile(String bucket_name, String object_name);

    @ResponseBody
    ResponseEntity<?> deleteObjectFile(String bucket_name, String object_name);

    @ResponseBody
    ResponseEntity<?> uploadObjectPart(String bucket_name, String object_name, Integer part_number, Integer part_size, String part_md5, HttpServletRequest request_body);

}
