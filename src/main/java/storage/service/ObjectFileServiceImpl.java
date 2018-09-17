package storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import storage.model.Bucket;
import storage.model.ObjectFile;
import storage.repository.ObjectFileRepository;

import java.io.File;
import java.util.UUID;

@Service
public class ObjectFileServiceImpl implements ObjectFileService {
    @Autowired
    private ObjectFileRepository objectFileRepository;

    @Override
    public String createInternalName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Iterable<ObjectFile> findAll() {
        return objectFileRepository.findAll();
    }

    @Override
    public ResponseEntity<?> createObjectFile(String bucket_name, String object_name) {
        System.out.println("in create object");
        return null;
    }

    @Override
    public boolean createObjectFileDirectory(ObjectFile object) {
        File newDirectory = new File("data/" + object.getName());
        return newDirectory.mkdirs();
    }
}
