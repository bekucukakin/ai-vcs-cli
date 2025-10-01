package org.example.model;

import org.example.enums.FileState;

public class FileStatus {
    private final String filePath;
    private final String hash;
    private final FileState state;

    public FileStatus(String filePath, String hash, FileState state) {
        this.filePath = filePath;
        this.hash = hash;
        this.state = state;
    }

    public String getFilePath() { return filePath; }
    public String getHash() { return hash; }
    public FileState getState() { return state; }
}
