package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.StatusService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.constant.VcsConstants.*;
@Service
public class StatusServiceImpl implements StatusService {

    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, String> staging = new HashMap<>();

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    @Override
    public void showStatus(boolean showDiff, String targetFile) throws Exception {
        loadStaging();

        System.out.println("Staged files:");
        staging.forEach((k, v) -> System.out.println(" - " + k));

        System.out.println("\nModified / New / Deleted files:");

        Set<String> committedFiles = getAllCommittedFilePaths();
        List<File> allFiles = resolveFiles(".");
        Set<String> currentFiles = allFiles.stream()
                .map(f -> {
                    try { return f.getCanonicalPath(); }
                    catch (Exception e) { return ""; }
                })
                .collect(Collectors.toSet());

        for (File f : allFiles) {
            if (isInsideVcs(f)) continue;
            String path = f.getCanonicalPath();
            if (staging.containsKey(path)) continue;

            String committedHash = getLastCommitFileHash(f);
            String currentHash = getFileHash(f);

            boolean isNew = committedHash == null;
            boolean isModified = !isNew && !committedHash.equals(currentHash);

            if (isNew) {
                System.out.println(" - " + path + " (new)");
                if (showDiff && (targetFile == null || path.endsWith(targetFile))) {
                    List<String> newLines = Files.readAllLines(f.toPath());
                    System.out.println("   Diff:");
                    for (String line : newLines) {
                        System.out.println(GREEN + "+ " + line + RESET);
                    }
                }
            } else if (isModified) {
                System.out.println(" - " + path + " (modified)");
                if (showDiff && (targetFile == null || path.endsWith(targetFile))) {
                    List<String> oldLines = getCommitFileLines(f, committedHash);
                    List<String> newLines = Files.readAllLines(f.toPath());
                    System.out.println("   Diff:");
                    printUnifiedDiff(oldLines, newLines);
                }
            }
        }

        for (String path : committedFiles) {
            if (!currentFiles.contains(path)) {
                System.out.println(" - " + path + " (deleted)");
                if (showDiff && (targetFile == null || path.endsWith(path))) {
                    List<String> oldLines = getCommitFileLines(new File(path), getLastCommitFileHash(new File(path)));
                    System.out.println("   Diff:");
                    for (String line : oldLines) {
                        System.out.println(RED + "- " + line + RESET);
                    }
                }
            }
        }
    }

    // ====================== HELPERS ======================
    private void printUnifiedDiff(List<String> oldLines, List<String> newLines) {
        int max = Math.max(oldLines.size(), newLines.size());
        for (int i = 0; i < max; i++) {
            String oldLine = i < oldLines.size() ? oldLines.get(i) : null;
            String newLine = i < newLines.size() ? newLines.get(i) : null;

            if (Objects.equals(oldLine, newLine)) {
                System.out.println("  " + (oldLine != null ? oldLine : ""));
            } else {
                if (oldLine != null) System.out.println(RED + "- " + oldLine + RESET);
                if (newLine != null) System.out.println(GREEN + "+ " + newLine + RESET);
            }
        }
    }

    private List<File> resolveFiles(String target) throws Exception {
        List<File> files = new ArrayList<>();
        File f = new File(target);
        if (target.equals(".")) files.addAll(listAllFiles(new File(".")));
        else if (f.exists()) files.add(f);
        return files;
    }

    private List<File> listAllFiles(File dir) throws Exception {
        List<File> files = new ArrayList<>();
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isDirectory()) {
                if (f.getName().equals(".vcs") || f.getName().equals(".git") ||
                        f.getName().equals("target") || f.getName().equals(".idea"))
                    continue;
                files.addAll(listAllFiles(f));
            } else files.add(f);
        }
        return files;
    }

    private boolean isInsideVcs(File f) throws Exception {
        return f.getCanonicalPath().contains(".vcs");
    }

    private void loadStaging() throws Exception {
        if (STAGING_FILE.exists()) staging = mapper.readValue(STAGING_FILE, HashMap.class);
    }

    private Set<String> getAllCommittedFilePaths() throws Exception {
        String branch = getCurrentBranch();
        String commitId = Files.readString(new File(REFS_HEADS_DIR, branch).toPath()).trim();
        if (commitId.isEmpty()) return Collections.emptySet();

        File commitFile = new File(OBJECTS_DIR, commitId.substring(0,2) + "/" + commitId.substring(2));
        if (!commitFile.exists()) return Collections.emptySet();

        Map<String,Object> commit = mapper.readValue(commitFile, HashMap.class);
        Map<String,String> files = (Map<String,String>) commit.get("files");
        return files.keySet();
    }

    private String getLastCommitFileHash(File f) throws Exception {
        String branch = getCurrentBranch();
        String commitId = Files.readString(new File(REFS_HEADS_DIR, branch).toPath()).trim();
        if (commitId.isEmpty()) return null;

        File commitFile = new File(OBJECTS_DIR, commitId.substring(0,2) + "/" + commitId.substring(2));
        if (!commitFile.exists()) return null;

        Map<String,Object> commit = mapper.readValue(commitFile, HashMap.class);
        Map<String,String> files = (Map<String,String>) commit.get("files");
        return files.get(f.getCanonicalPath());
    }

    private List<String> getCommitFileLines(File f, String hash) throws Exception {
        if (hash == null) return Collections.emptyList();
        File objFile = new File(OBJECTS_DIR, hash.substring(0,2) + "/" + hash.substring(2));
        if (!objFile.exists()) return Collections.emptyList();
        return Files.readAllLines(objFile.toPath());
    }

    private String getFileHash(File f) throws Exception {
        byte[] content = Files.readAllBytes(f.toPath());
        return getHash(new String(content));
    }

    private String getHash(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String getCurrentBranch() throws Exception {
        String ref = Files.readString(HEAD_FILE.toPath()).trim();
        return ref.replace("refs/heads/", "");
    }
}
