package org.example.service;

import java.io.File;

public interface CommitService {
    void commit(String message) throws Exception;
    void log() throws Exception;
    String getLastCommitFileHash(File file) throws Exception;
}
