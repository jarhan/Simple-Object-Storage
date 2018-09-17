package storage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import storage.model.Bucket;

@Repository
public interface BucketRepository extends MongoRepository<Bucket, String> {

    Bucket findByName(String name);

    void deleteBucketByName(String name);
}
