package storage.controller;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import storage.model.Bucket;
import storage.repository.BucketRepository;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BucketController {
    private BucketRepository bucketRepository;

    public BucketController(BucketRepository bucketRepository) {
        this.bucketRepository = bucketRepository;
    }


    @RequestMapping("/")
    public String index() {
        return "Hello World from Spring Boot!";
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Bucket> getAll(){
        List<Bucket> buckets = this.bucketRepository.findAll();
        System.out.println(buckets);
        return buckets;
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> createBucket(@RequestParam(value = "create") String create,
                                          @PathVariable String bucket_name) {
        try {
            Bucket bucket = this.bucketRepository.findByName(bucket_name);
            if (bucket == null) {
                Bucket b = new Bucket(bucket_name);
                this.bucketRepository.save(b);
                File newDirectory = new File("data/" + b.getName());
                newDirectory.mkdirs();

                Map<String, Object> response = new HashMap<>();
                response.put("name", b.getName());

                return ResponseEntity.ok().body(response);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.DELETE)
    public @ResponseBody ResponseEntity<?> deleteBucket(@RequestParam(value = "delete") String delete,
                               @PathVariable String bucket_name) {
        Bucket bucket_to_delete = this.bucketRepository.findByName(bucket_name);
        if (bucket_to_delete != null) {
            try {
                FileUtils.deleteDirectory(new File("data/" + bucket_name));
                this.bucketRepository.delete(bucket_to_delete);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception ex) {
                System.out.println(ex.toString());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.GET)
    public String listBucket(@RequestParam(value = "list") String list,
                               @PathVariable String bucket_name) {
        return "List bucket named: " + bucket_name + "\nlist param:" + list;
    }
}