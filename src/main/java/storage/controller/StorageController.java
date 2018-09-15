package storage.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class StorageController {

    @RequestMapping("/")
    public String index() {
        return "Hello World from Spring Boot!";
    }

    @RequestMapping("/hi")
    public String hi() {
        return "Hi from Spring Boot!";
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.POST)
    public String createBucket(@RequestParam(value = "create") String create,
            @PathVariable String bucket_name) {
        return "Create bucket named: " + bucket_name + "\ncreate param:" + create;
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.DELETE)
    public String deleteBucket(@RequestParam(value = "delete") String delete,
                               @PathVariable String bucket_name) {
        return "Delete bucket named: " + bucket_name + "\ndelete param:" + delete;
    }

    @RequestMapping(value = "/{bucket_name}", method = RequestMethod.GET)
    public String listBucket(@RequestParam(value = "list") String list,
                               @PathVariable String bucket_name) {
        return "List bucket named: " + bucket_name + "\nlist param:" + list;
    }
}