package storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import storage.model.Bucket;
import storage.model.ObjectFile;
import storage.repository.BucketRepository;
import storage.service.ObjectFileServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping(value = "/{bucket_name}/{object_name}")
    public @ResponseBody ResponseEntity<?> downloadObjectFile(@RequestHeader(value = "Range", required = false, defaultValue = "no range") String range,
                                                              @PathVariable String bucket_name,
                                                              @PathVariable String object_name,
                                                              HttpServletResponse response){
        try {
            System.out.println("download");
            if (!range.equals("no range")) {
                return objectFileService.downloadObjectWithRange(bucket_name, object_name, range, response);
            }
            else {
                System.out.println("download all");
                return objectFileService.downloadObjectFullRange(bucket_name, object_name, response);
            }
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/{bucket_name}/{object_name}")
    public @ResponseBody ResponseEntity<?> deletePart(@RequestParam(value = "partNumber") String part_number,
                                                      @PathVariable String bucket_name,
                                                      @PathVariable String object_name) {
        try {
            Integer casted_part_number = objectFileService.castPartNumber(part_number);

            return objectFileService.deleteObjectPart(bucket_name, object_name.toLowerCase(), casted_part_number);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/{bucket_name}/{object_name}", params = "complete")
    public @ResponseBody ResponseEntity<?> completeUpload(@PathVariable String bucket_name,
                                                          @PathVariable String object_name){
        return objectFileService.completeObjectUpload(bucket_name, object_name.toLowerCase());
    }

    @PutMapping(value = "/{bucket_name}/{object_name}", params = "metadata")
    public @ResponseBody ResponseEntity<?> updateObjectMetadata(@RequestParam(value = "key") String metadata_key,
                                                                @RequestBody String metadata_value,
                                                                @PathVariable String bucket_name,
                                                                @PathVariable String object_name){
        return objectFileService.updateObjectMetadata(bucket_name, object_name.toLowerCase(), metadata_key, metadata_value);
    }

    @DeleteMapping(value = "/{bucket_name}/{object_name}", params = "metadata")
    public @ResponseBody ResponseEntity<?> deleteObjectMetadata(@RequestParam(value = "key") String metadata_key,
                                                                @PathVariable String bucket_name,
                                                                @PathVariable String object_name){
        return objectFileService.deleteObjectMetadata(bucket_name, object_name.toLowerCase(), metadata_key);
    }

    @GetMapping(value = "/{bucket_name}/{object_name}", params = "metadata")
    public @ResponseBody ResponseEntity<?> getObjectMetadata(@RequestParam(value = "key", defaultValue = "all") String metadata_key,
                                                             @PathVariable String bucket_name,
                                                             @PathVariable String object_name){
        if (metadata_key.equals("all")) {
            return objectFileService.getAllObjectMetadata(bucket_name, object_name.toLowerCase());
        }
        return objectFileService.getObjectMetadata(bucket_name, object_name.toLowerCase(), metadata_key);
    }
}