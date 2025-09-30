package org.example.cli;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class VcsCliGraph {

    private static final File VCS_DIR = new File(".vcs");
    private static final File OBJECTS_DIR = new File(VCS_DIR, "objects");
    private static final File REFS_HEADS_DIR = new File(VCS_DIR, "refs/heads");
    private static final File HEAD_FILE = new File(VCS_DIR, "HEAD");
    private static final File LOGS_DIR = new File(VCS_DIR, "logs/refs/heads");
    private static final File STAGING_FILE = new File(VCS_DIR, "staging.json");
    private static final File HOOKS_DIR = new File(VCS_DIR, "hooks");

    private Map<String, String> staging = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void startCLI() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to MiniVCS CLI!");

        while (true) {
            System.out.print("> ");
            String input = sc.nextLine();
            String[] parts = input.trim().split("\\s+", 2);
            if (parts.length == 0) continue;

            switch (parts[0]) {
                case "init":
                    handleInit();
                    break;
                case "add":
                    handleAdd(parts.length > 1 ? parts[1] : ".");
                    break;
                case "commit":
                    handleCommit(parts.length > 1 ? parts[1] : "No message");
                    break;
                case "log":
                    handleLog();
                    break;
                case "status":
                    handleStatus();
                    break;
                case "branch":
                    handleBranch(parts[1]);
                    break;
                case "checkout":
                    handleCheckout(parts[1]);
                    break;
                case "merge":
                    handleMerge(parts[1]);
                    break;
                case "exit":
                    return;
                default:
                    System.out.println("Unknown command: " + parts[0]);
            }
        }
    }

    // ====================== INIT ======================
    private void handleInit() throws IOException {
        if (VCS_DIR.exists()) {
            System.out.println("Repository already initialized.");
            return;
        }
        OBJECTS_DIR.mkdirs();
        REFS_HEADS_DIR.mkdirs();
        LOGS_DIR.mkdirs();
        HOOKS_DIR.mkdirs();

        Files.writeString(HEAD_FILE.toPath(), "refs/heads/main");
        Files.writeString(new File(REFS_HEADS_DIR, "main").toPath(), "");
        saveStaging();
        System.out.println("Initialized empty MiniVCS repository in " + VCS_DIR.getAbsolutePath());
    }

    // ====================== ADD ======================
    private void handleAdd(String target) throws Exception {
        List<File> files = resolveFiles(target);
        loadStaging();
        for (File f : files) {
            if (isInsideVcs(f)) continue;
            String hash = getFileHash(f);
            File objFile = new File(OBJECTS_DIR, hash.substring(0, 2) + "/" + hash.substring(2));
            objFile.getParentFile().mkdirs();
            Files.write(objFile.toPath(), Files.readAllBytes(f.toPath()));
            staging.put(f.getCanonicalPath(), hash);
        }
        saveStaging();
        System.out.println("Files staged for commit: " + staging.keySet());
    }

    // ====================== COMMIT ======================
    private void handleCommit(String message) throws Exception {
        loadStaging();
        if (staging.isEmpty()) {
            System.out.println("No changes added to commit.");
            return;
        }
        runHook("pre-commit");

        String parent = Files.readString(new File(REFS_HEADS_DIR, getCurrentBranch()).toPath());
        String commitId = getHash(UUID.randomUUID().toString() + LocalDateTime.now());
        Map<String, Object> commitObj = new HashMap<>();
        commitObj.put("id", commitId);
        commitObj.put("message", message);
        commitObj.put("timestamp", LocalDateTime.now().toString());
        commitObj.put("branch", getCurrentBranch());
        commitObj.put("files", staging);
        commitObj.put("parents", parent.isEmpty() ? new ArrayList<>() : Collections.singletonList(parent));

        File commitFile = new File(OBJECTS_DIR, commitId.substring(0, 2) + "/" + commitId.substring(2));
        commitFile.getParentFile().mkdirs();
        mapper.writeValue(commitFile, commitObj);

        Files.writeString(new File(REFS_HEADS_DIR, getCurrentBranch()).toPath(), commitId);

        File logFile = new File(LOGS_DIR, getCurrentBranch());
        String logLine = String.format("%s %s %s\n", parent, commitId, message);
        Files.writeString(logFile.toPath(), logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        staging.clear();
        saveStaging();
        runHook("post-commit");

        System.out.println("[" + getCurrentBranch() + " " + commitId + "] " + message);
    }

    // ====================== LOG ======================
    private void handleLog() throws Exception {
        String branch = getCurrentBranch();
        File logFile = new File(LOGS_DIR, branch);
        if (!logFile.exists()) {
            System.out.println("No commits yet.");
            return;
        }
        List<String> lines = Files.readAllLines(logFile.toPath());
        Collections.reverse(lines);
        for (String line : lines) System.out.println(line);
    }

    // ====================== STATUS ======================
    private void handleStatus() {
        System.out.println("Staged files:");
        staging.forEach((k, v) -> System.out.println(" - " + k));
    }

    // ====================== BRANCH ======================
    private void handleBranch(String branchName) throws IOException {
        File branchFile = new File(REFS_HEADS_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("Branch already exists.");
            return;
        }
        String currentCommit = Files.readString(new File(REFS_HEADS_DIR, getCurrentBranch()).toPath());
        Files.writeString(branchFile.toPath(), currentCommit);
        System.out.println("Branch " + branchName + " created at commit " + currentCommit);
    }

    // ====================== CHECKOUT ======================
    private void handleCheckout(String branchName) throws Exception {
        File branchFile = new File(REFS_HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("Branch does not exist.");
            return;
        }
        Files.writeString(HEAD_FILE.toPath(), "refs/heads/" + branchName);
        String commitId = Files.readString(branchFile.toPath()).trim();
        if (!commitId.isEmpty()) restoreCommitFiles(commitId);
        System.out.println("Switched to branch " + branchName);
    }

    private void restoreCommitFiles(String commitId) throws Exception {
        File commitFile = new File(OBJECTS_DIR, commitId.substring(0, 2) + "/" + commitId.substring(2));
        Map<String, Object> commit = mapper.readValue(commitFile, HashMap.class);
        Map<String, String> files = (Map<String, String>) commit.get("files");
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String path = entry.getKey();
            String hash = entry.getValue();
            File objFile = new File(OBJECTS_DIR, hash.substring(0, 2) + "/" + hash.substring(2));
            Files.createDirectories(Paths.get(path).getParent());
            Files.copy(objFile.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // ====================== MERGE ======================
    private void handleMerge(String branchName) throws Exception {
        File targetBranch = new File(REFS_HEADS_DIR, branchName);
        if (!targetBranch.exists()) {
            System.out.println("Branch does not exist.");
            return;
        }

        String currentCommit = Files.readString(new File(REFS_HEADS_DIR, getCurrentBranch()).toPath()).trim();
        String mergeCommit = Files.readString(targetBranch.toPath()).trim();

        restoreCommitFiles(mergeCommit); // fast-forward merge

        staging.clear();
        staging.putAll(getAllFilesWithHashes());
        handleCommit("Merged branch " + branchName + " into " + getCurrentBranch());
    }

    private Map<String, String> getAllFilesWithHashes() throws Exception {
        Map<String, String> files = new HashMap<>();
        List<File> allFiles = resolveFiles(".");
        for (File f : allFiles) {
            if (isInsideVcs(f)) continue;
            files.put(f.getCanonicalPath(), getFileHash(f));
        }
        return files;
    }

    // ====================== HOOKS ======================
    private void runHook(String hookName) throws IOException, InterruptedException {
        File hookFile = new File(HOOKS_DIR, hookName);
        if (hookFile.exists() && hookFile.canExecute()) {
            Process p = new ProcessBuilder(hookFile.getAbsolutePath()).inheritIO().start();
            p.waitFor();
        }
    }

    // ====================== HELPERS ======================
    private List<File> resolveFiles(String target) throws IOException {
        List<File> files = new ArrayList<>();
        File f = new File(target);
        if (target.equals(".")) files.addAll(listAllFiles(new File(".")));
        else if (f.exists()) files.add(f);
        return files;
    }

    private List<File> listAllFiles(File dir) throws IOException {
        List<File> files = new ArrayList<>();
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isDirectory()) {
                if (f.getName().equals(".vcs") || f.getName().equals(".git") || f.getName().equals("target") || f.getName().equals(".idea"))
                    continue;
                files.addAll(listAllFiles(f));
            } else files.add(f);
        }
        return files;
    }

    private boolean isInsideVcs(File f) throws IOException {
        return f.getCanonicalPath().contains(".vcs");
    }

    private String getCurrentBranch() throws IOException {
        String ref = Files.readString(HEAD_FILE.toPath()).trim();
        return ref.replace("refs/heads/", "");
    }

    private void loadStaging() throws IOException {
        if (STAGING_FILE.exists()) staging = mapper.readValue(STAGING_FILE, HashMap.class);
    }

    private void saveStaging() throws IOException {
        STAGING_FILE.getParentFile().mkdirs();
        mapper.writeValue(STAGING_FILE, staging);
    }

    private String getFileHash(File f) throws Exception {
        byte[] content = Files.readAllBytes(f.toPath());
        return getHash(new String(content));
    }

    private String getHash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
