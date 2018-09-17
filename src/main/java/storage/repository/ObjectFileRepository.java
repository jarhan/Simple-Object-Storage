package storage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import storage.model.ObjectFile;

@Repository
public interface ObjectFileRepository extends MongoRepository<ObjectFile, String> {

    ObjectFile findByName(String name);
}
