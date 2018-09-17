package storage.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document

public class Bucket {

    @Id
    private String id;

    private String name;

    public Bucket() {};

    public Bucket(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format(
                "Bucket[id=%s, name='%s']",
                id, name);
    }

    public String getName() {
        return name;
    }

}
