package storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import storage.model.Bucket;
import storage.model.ObjectFile;
import storage.repository.BucketRepository;
import storage.service.ObjectFileServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
public class ObjectFileController {
    @Autowired
    private ObjectFileServiceImpl objectFileService;

    @Autowired
    private BucketRepository bucketRepository;

    @GetMapping(value = "/{bucket_name}/all")
    public Iterable<ObjectFile> getAll(@PathVariable String bucket_name){
        Bucket bucket = bucketRepository.findByName(bucket_name);
        return bucket.getObjects();
    }

    @PostMapping(value = "/{bucket_name}/{object_name}", params = "create")
    public @ResponseBody ResponseEntity<?> createObjectFile(@PathVariable String bucket_name,
                                                            @PathVariable String object_name) {
        return objectFileService.createObjectFile(bucket_name, object_name.toLowerCase());
    }

    @DeleteMapping(value = "/{bucket_name}/{object_name}", params = "delete")
    public @ResponseBody ResponseEntity<?> deleteBucket(@PathVariable String bucket_name,
                                                        @PathVariable String object_name) {
        return objectFileService.deleteObjectFile(bucket_name, object_name.toLowerCase());
    }

    @PutMapping(value = "/{bucket_name}/{object_name}")
    public @ResponseBody ResponseEntity<?> uploadPart(@RequestHeader(value="Content-Length") String part_size,
                                                      @RequestHeader(value="Content-MD5") String part_md5,
                                                      @RequestParam(value = "partNumber") String part_number,
                                                      @PathVariable String bucket_name,
                                                      @PathVariable String object_name,
                                                      HttpServletRequest request_body){
        try {
            long casted_part_size = objectFileService.castContentLength(part_size);
            Integer casted_part_number = objectFileService.castPartNumber(part_number);

            return objectFileService.uploadObjectPart(bucket_name, object_name.toLowerCase(), casted_part_number, casted_part_size, part_md5, request_body);
        } catch (Exception ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("md5", part_md5);
            response.put("length", part_number);
            response.put("partNumber", part_number);
            System.out.println(response);
            return ResponseEntity.badRequest().body(response);
        }
    }

//    @DeleteMapping(value = "/{bucket_name}/{object_name}", params = "delete")
//    public @ResponseBody ResponseEntity<?> deletePart(@RequestParam(value = "partNumber") String part_number,
//                                                      @PathVariable String bucket_name,
//                                                      @PathVariable String object_name) {
//        return objectFileService.deleteObjectPart(bucket_name, object_name.toLowerCase(), part_number);
//    }

    @PostMapping(value = "/{bucket_name}/{object_name}", params = "complete")
    public @ResponseBody ResponseEntity<?> completeUpload(@PathVariable String bucket_name,
                                                          @PathVariable String object_name){
        return objectFileService.completeObjectUpload(bucket_name, object_name.toLowerCase());
    }
}