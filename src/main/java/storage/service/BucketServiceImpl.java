package storage.service;

import storage.model.Bucket;

import java.util.Optional;

public class BucketServiceImpl implements BucketService {
    @Override
    public Iterable<Bucket> findAll() {
        return null;
    }

    @Override
    public Bucket save(Bucket bucket) {
        return null;
    }

    @Override
    public Optional<Bucket> findByName(String name) {
        return Optional.empty();
    }

    @Override
    public void removeBucket(String name) {

    }
}
