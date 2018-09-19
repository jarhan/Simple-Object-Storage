package storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import storage.model.Bucket;
import storage.model.ObjectFile;
import storage.repository.BucketRepository;
import storage.service.ObjectFileServiceImpl;

import javax.servlet.http.HttpServletRequest;


@RestController
public class ObjectFileController {
    @Autowired
    private ObjectFileServiceImpl objectFileService;

    @Autowired
    private BucketRepository bucketRepository;

    @RequestMapping(value = "/{bucket_name}/all", method = RequestMethod.GET)
    public Iterable<ObjectFile> getAll(@PathVariable String bucket_name){
        Bucket bucket = bucketRepository.findByName(bucket_name);
        return bucket.getObjects();
    }

    @RequestMapping(value = "/{bucket_name}/{object_name}", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> createObjectFile(@RequestParam(value = "create") String create,
                                                        @PathVariable String bucket_name,
                                                        @PathVariable String object_name) {
        return objectFileService.createObjectFile(bucket_name, object_name);
    }

    @RequestMapping(value = "/{bucket_name}/{object_name}", method = RequestMethod.DELETE)
    public @ResponseBody ResponseEntity<?> deleteBucket(@RequestParam(value = "delete") String delete,
                                                        @PathVariable String bucket_name,
                                                        @PathVariable String object_name) {
        return objectFileService.deleteObjectFile(bucket_name, object_name);
    }

//    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.GET)
//    public @ResponseBody ResponseEntity<?> listObjects(@RequestParam(value = "list") String list,
//                                                       @PathVariable String bucket_name) {
//        return this.bucketService.listObjects(bucket_name);
//    }

    @RequestMapping(value = "/{bucket_name}/{object_name}", method = RequestMethod.PUT)
    public @ResponseBody ResponseEntity<?> uploadPart(@RequestHeader(value="Content-Length") Integer part_size,
                                                      @RequestHeader(value="Content-MD5") String part_md5,
                                                      @RequestParam(value = "partNumber") Integer part_number,
                                                      @PathVariable String bucket_name,
                                                      @PathVariable String object_name,
                                                      HttpServletRequest request_body){
        return objectFileService.uploadObjectPart(bucket_name, object_name, part_number, part_size, part_md5, request_body);
    }
}