package storage.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document

public class Bucket {

    @Id
    private String id;

    private String name;
    private long created;
    private long modified;

    public Bucket() {};

    public Bucket(String name, long created, long modified) {
        this.name = name;
        this.created = created;
        this.modified = modified;
    }

    @Override
    public String toString() {
        return String.format(
                "\nBucket[id=%s, name='%s', created='%s', modified='%s']",
                id, name, created, modified);
    }

    public String getName() {
        return name;
    }

    public long getCreated() {
        return created;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }
}
