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

    @GetMapping(value = "/all")
    public Iterable<Bucket> getAll(){
        Iterable<Bucket> buckets = this.bucketService.findAll();
        System.out.println(buckets);
        return buckets;
    }

    @PostMapping(value = "/{bucket_name}", params = "create")
    public @ResponseBody ResponseEntity<?> createBucket(@PathVariable String bucket_name) {
        return this.bucketService.createBucket(bucket_name);
    }

    @DeleteMapping(value = "/{bucket_name}", params = "delete")
    public @ResponseBody ResponseEntity<?> deleteBucket(@PathVariable String bucket_name) {
        return this.bucketService.deleteBucket(bucket_name);
    }

    @GetMapping(value = "/{bucket_name}", params = "list")
    public @ResponseBody ResponseEntity<?> listObjects(@PathVariable String bucket_name) {
        return this.bucketService.listObjects(bucket_name);
    }
}