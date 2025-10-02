package ai.based.vcs.infrastructure.service;

import ai.based.vcs.domain.repository.Repository;
import ai.based.vcs.domain.service.FileService;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;

/**
 * File service implementation
 * Handles file operations and staging
 * Follows Single Responsibility Principle and Dependency Inversion Principle
 */
public class FileServiceImpl implements FileService {

    private final Repository repository;

    public FileServiceImpl(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void addFile(String filePath) throws Exception {
        Path file = repository.getWorkDir().resolve(filePath);
        if (!file.toFile().exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }
        byte[] storage = repository.blobObjectFromFile(file);
        String hash = repository.writeObject(storage);
        Map<String, String> index = repository.readIndex();
        index.put(filePath, hash);
        repository.writeIndex(index);
        System.out.println("Added " + filePath + " as blob " + hash);
    }
}
