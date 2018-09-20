package storage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import storage.model.Bucket;
import storage.model.ObjectFile;

import java.util.List;

@Repository
public interface BucketRepository extends MongoRepository<Bucket, String> {

    Bucket findByName(String name);

    Bucket findByModified(long modified);

    void deleteBucketByName(String name);
}
