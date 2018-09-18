package storage.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import storage.model.Bucket;
import storage.model.ObjectFile;

public interface ObjectFileService {

    String createInternalName();

    Iterable<ObjectFile> findAll();

    @ResponseBody
    ResponseEntity<?> createObjectFile(String bucket_name, String object_name);

    @ResponseBody
    ResponseEntity<?> deleteObjectFile(String bucket_name, String object_name);

}
