package ai.based.vcs.infrastructure.repository;


import ai.based.vcs.domain.model.Blob;
import ai.based.vcs.domain.repository.Repository;
import ai.based.vcs.shared.util.HexUtils;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * File-based repository implementation
 * Implements Repository interface using file system
 * Follows Dependency Inversion Principle
 */
public class FileRepositoryImpl implements Repository {
    private final Path workDir;
    private final Path gitDir;

    public FileRepositoryImpl(Path workDir) {
        this.workDir = workDir;
        this.gitDir = workDir.resolve(".git");
    }

    @Override
    public Path getWorkDir() {
        return workDir;
    }

    @Override
    public Path getGitDir() {
        return gitDir;
    }

    @Override
    public void init() throws IOException {
        if (Files.exists(gitDir)) throw new IOException(".git already exists");
        Files.createDirectories(gitDir);
        Files.createDirectories(gitDir.resolve("objects"));
        Files.createDirectories(gitDir.resolve("refs").resolve("heads"));
        // HEAD file points to refs/heads/master by default
        Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/master\n");
        // create empty index
        Files.writeString(gitDir.resolve("index"), "");
    }

    @Override
    public String writeObject(byte[] storageBytes) throws Exception {
        byte[] sha = MessageDigest.getInstance("SHA-1").digest(storageBytes);
        String hex = HexUtils.bytesToHex(sha);
        Path objDir = gitDir.resolve("objects").resolve(hex.substring(0, 2));
        Path objFile = objDir.resolve(hex.substring(2));
        if (!Files.exists(objDir)) Files.createDirectories(objDir);
        if (!Files.exists(objFile)) Files.write(objFile, storageBytes);
        return hex;
    }

    @Override
    public Optional<byte[]> readObjectRaw(String hash) throws Exception {
        Path objFile = gitDir.resolve("objects").resolve(hash.substring(0,2)).resolve(hash.substring(2));
        if (!Files.exists(objFile)) return Optional.empty();
        return Optional.of(Files.readAllBytes(objFile));
    }

    @Override
    public Map<String, String> readIndex() throws IOException {
        Map<String, String> map = new HashMap<>();
        Path index = gitDir.resolve("index");
        if (!Files.exists(index)) return map;
        List<String> lines = Files.readAllLines(index);
        for (String l : lines) {
            if (l.trim().isEmpty()) continue;
            int eq = l.indexOf('=');
            if (eq > 0) {
                String p = l.substring(0, eq);
                String h = l.substring(eq + 1);
                map.put(p, h);
            }
        }
        return map;
    }

    @Override
    public void writeIndex(Map<String, String> indexMap) throws IOException {
        Path index = gitDir.resolve("index");
        List<String> lines = indexMap.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        Files.write(index, lines);
    }

    @Override
    public void updateRef(String refPath, String value) throws IOException {
        Path ref = gitDir.resolve(refPath);
        Files.createDirectories(ref.getParent());
        Files.writeString(ref, value + "\n");
    }

    @Override
    public Optional<String> readRef(String refPath) throws IOException {
        Path ref = gitDir.resolve(refPath);
        if (!Files.exists(ref)) return Optional.empty();
        String v = Files.readString(ref).trim();
        return Optional.of(v);
    }

    @Override
    public Optional<String> readHEAD() throws IOException {
        Path head = gitDir.resolve("HEAD");
        if (!Files.exists(head)) return Optional.empty();
        String content = Files.readString(head).trim();
        if (content.startsWith("ref: ")) {
            String ref = content.substring(5).trim();
            Optional<String> v = readRef(ref);
            return v;
        } else {
            return Optional.of(content);
        }
    }

    @Override
    public String readHEADRaw() throws IOException {
        Path head = gitDir.resolve("HEAD");
        if (!Files.exists(head)) throw new IOException("HEAD not found");
        return Files.readString(head).trim();
    }

    @Override
    public void setHEADToRef(String ref) throws IOException {
        Files.writeString(gitDir.resolve("HEAD"), "ref: " + ref + "\n");
    }

    @Override
    public void setHEADDetached(String commitHash) throws IOException {
        Files.writeString(gitDir.resolve("HEAD"), commitHash + "\n");
    }

    @Override
    public byte[] blobObjectFromFile(Path file) throws Exception {
        byte[] data = Files.readAllBytes(file);
        Blob blob = new Blob(data);
        return blob.getStorageBytes();
    }

    @Override
    public String bytesToHex(byte[] bytes) {
        return HexUtils.bytesToHex(bytes);
    }
}
