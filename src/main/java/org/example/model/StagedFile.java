package org.example.model;

public class StagedFile {
    private final String path;
    private final String hash;

    public StagedFile(String path, String hash) {
        this.path = path;
        this.hash = hash;
    }

    public String getPath() {
        return path;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return path + "=" + hash;
    }
}
