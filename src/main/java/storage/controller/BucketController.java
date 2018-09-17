package storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import storage.model.Bucket;
import storage.service.BucketServiceImpl;

@RestController
public class BucketController {
    @Autowired
    private BucketServiceImpl bucketService;

    @RequestMapping("/")
    public String index() {
        return "Hello World from Spring Boot!";
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Iterable<Bucket> getAll(){
        Iterable<Bucket> buckets = this.bucketService.findAll();
        System.out.println(buckets);
        return buckets;
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> createBucket(@RequestParam(value = "create") String create,
                                          @PathVariable String bucket_name) {
        return this.bucketService.createBucket(bucket_name);
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.DELETE)
    public @ResponseBody ResponseEntity<?> deleteBucket(@RequestParam(value = "delete") String delete,
                               @PathVariable String bucket_name) {
        return this.bucketService.deleteBucket(bucket_name);
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<?> listObjects(@RequestParam(value = "list") String list,
                               @PathVariable String bucket_name) {
        return this.bucketService.listObjects(bucket_name);
    }
}