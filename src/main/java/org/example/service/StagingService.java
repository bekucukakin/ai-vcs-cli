package org.example.service;

import java.io.File;
import java.util.Map;

public interface StagingService {
    void add(String target) throws Exception;
    Map<String, String> loadStaging() throws Exception;
    void saveStaging(Map<String, String> staging) throws Exception;
    void clearStaging() throws Exception;
}
