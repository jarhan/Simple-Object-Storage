package storage.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document

public class Bucket {

    @Id
    private ObjectId id;

    private String name;
    private long created;
    private long modified;
    private String uuid;
    private ArrayList<ObjectFile> objects;

    public Bucket() {};

    public Bucket(String name, long created, long modified, String uuid) {
        this.name = name;
        this.created = created;
        this.modified = modified;
        this.uuid = uuid;
        this.objects = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format(
                "\nBucket[id=%s, name='%s', created='%s', modified='%s']",
                id, name, created, modified);
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
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

    public ArrayList<ObjectFile> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<ObjectFile> objects) {
        this.objects = objects;
    }
}
