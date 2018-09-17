package storage.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document

public class ObjectFile {

    @Id
    private String id;

    private String name;
    private long created;
    private long modified;
    private String uuid;
    private Set<Integer> file_parts;
    private boolean ticket;

    public ObjectFile() {};

    public ObjectFile(String name, long created, long modified, String uuid) {
        this.name = name;
        this.created = created;
        this.modified = modified;
        this.uuid = uuid;
        this.file_parts =  new HashSet<>();
        this.ticket = false;
    }

    @Override
    public String toString() {
        return String.format(
                "\nObject[id=%s, name='%s', created='%s', modified='%s']",
                id, name, created, modified);
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
}
