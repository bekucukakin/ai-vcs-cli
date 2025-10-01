package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.CommitService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

import static org.example.constant.VcsConstants.*;
@Service
public class CommitServiceImpl implements CommitService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commit(String message, Map<String, String> stagedFiles) throws Exception {
        if (stagedFiles.isEmpty()) {
            System.out.println("No changes added to commit.");
            return;
        }

        runHook("pre-commit");

        String branch = getCurrentBranch();
        String parent = Files.readString(new File(REFS_HEADS_DIR, branch).toPath()).trim();
        String commitId = getHash(UUID.randomUUID().toString() + LocalDateTime.now());

        Map<String, Object> commitObj = new HashMap<>();
        commitObj.put("id", commitId);
        commitObj.put("message", message);
        commitObj.put("timestamp", LocalDateTime.now().toString());
        commitObj.put("branch", branch);
        commitObj.put("files", stagedFiles);
        commitObj.put("parents", parent.isEmpty() ? new ArrayList<>() : Collections.singletonList(parent));

        File commitFile = new File(OBJECTS_DIR, commitId.substring(0, 2) + "/" + commitId.substring(2));
        commitFile.getParentFile().mkdirs();
        mapper.writeValue(commitFile, commitObj);

        Files.writeString(new File(REFS_HEADS_DIR, branch).toPath(), commitId);

        File logFile = new File(LOGS_DIR, branch);
        String logLine = String.format("%s %s %s\n", parent, commitId, message);
        Files.writeString(logFile.toPath(), logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        stagedFiles.clear();

        runHook("post-commit");

        System.out.println("[" + branch + " " + commitId + "] " + message);
    }

    private void runHook(String hookName) throws Exception {
        File hookFile = new File(HOOKS_DIR, hookName);
        if (hookFile.exists() && hookFile.canExecute()) {
            Process p = new ProcessBuilder(hookFile.getAbsolutePath()).inheritIO().start();
            p.waitFor();
        }
    }

    private String getCurrentBranch() throws Exception {
        String ref = Files.readString(HEAD_FILE.toPath()).trim();
        return ref.replace("refs/heads/", "");
    }

    private String getHash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
