package org.example.service;

import java.util.Map;

public interface CommitService {
    void commit(String message, Map<String, String> stagedFiles) throws Exception;
}
