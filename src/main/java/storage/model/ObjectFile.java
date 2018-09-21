package storage.model;

import javafx.util.Pair;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document

public class ObjectFile {

    @Id
    private ObjectId id;

    private String name;
    private long created;
    private long modified;
    private String uuid;
    private Map<Integer, ArrayList<Object>> file_parts;
    private Map<String, String> metadata;
    private long file_length;
    private boolean ticket;

    public ObjectFile() {};

    public ObjectFile(String name, long created, long modified, String uuid) {
        this.name = name;
        this.created = created;
        this.modified = modified;
        this.uuid = uuid;
        this.file_parts =  new HashMap<>();
        this.metadata = new HashMap<>();
        this.file_length = 0;
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

    public void setFile_length(long file_length) {
        this.file_length = file_length;
    }

    public long getFile_length() {
        return file_length;
    }

    public boolean containsFilePart(Integer file_part) {
        return file_parts.containsKey(file_part);
    }

    public void addFilePart(Integer file_part, String part_md5, Long part_size) {
        ArrayList<Object> data = new ArrayList<>();
        data.add(part_md5);
        data.add(part_size);
        this.file_parts.put(file_part, data);
    }

    public void removeFilePart(Integer file_part) {
        ArrayList<Object> data = new ArrayList<>();
        this.file_parts.remove(file_part);
    }

    public Map<Integer, ArrayList<Object>> getFile_parts() {
        return file_parts;
    }

    public boolean isTicketFlagged() {
        return ticket;
    }

    public boolean isPartNumberValidToAdd(Integer part_number) {
        if (part_number >= 1 && part_number <= 10000) {
            return true;
        }
        return false;
    }

    public void flagTicket() {
        this.ticket = true;
    }

    public void unflagTicket() {
        this.ticket = false;
    }

    public Map<String, String> getAllMetadata() {
        return metadata;
    }

    public String getMetadataWithKey(String key) {
        return metadata.get(key);
    }

    public void updateMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public void removeMetadata(String key) {
        this.metadata.remove(key);
    }

    public boolean containMetatdataKey(String key) {
        return this.metadata.containsKey(key);
    }
}
