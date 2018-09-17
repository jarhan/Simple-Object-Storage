package storage.service;

import java.util.Optional;

import storage.model.Bucket;

public interface BucketService {

    public Iterable<Bucket> findAll();

    public Bucket save(Bucket bucket);

    public Optional<Bucket> findByName(String name);

    public void removeBucket(String name);
}
