package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.StagingService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.constant.VcsConstants.*;
@Service
public class StagingServiceImpl implements StagingService {

    private Map<String, String> staging = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handleAdd(String target) throws Exception {
        List<File> files = resolveFiles(target);
        loadStaging();

        for (File f : files) {
            if (isInsideVcs(f)) continue;

            String hash = getFileHash(f);

            // Object dosyasını oluştur
            File objFile = new File(OBJECTS_DIR, hash.substring(0, 2) + "/" + hash.substring(2));
            objFile.getParentFile().mkdirs();
            Files.write(objFile.toPath(), Files.readAllBytes(f.toPath()));

            staging.put(f.getCanonicalPath(), hash);
        }

        saveStaging();
        System.out.println("Files staged for commit: " + staging.keySet());
    }

    private void loadStaging() throws Exception {
        if (STAGING_FILE.exists())
            staging = mapper.readValue(STAGING_FILE, HashMap.class);
    }

    private void saveStaging() throws Exception {
        STAGING_FILE.getParentFile().mkdirs();
        mapper.writeValue(STAGING_FILE, staging);
    }

    private boolean isInsideVcs(File f) throws Exception {
        return f.getCanonicalPath().contains(".vcs");
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

    private List<File> resolveFiles(String target) throws Exception {
        File f = new File(target);
        if (target.equals(".")) return listAllFiles(new File("."));
        else if (f.exists()) return List.of(f);
        else return List.of();
    }

    private List<File> listAllFiles(File dir) throws Exception {
        List<File> files = new java.util.ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                if (f.getName().equals(".vcs") || f.getName().equals(".git") || f.getName().equals("target") || f.getName().equals(".idea"))
                    continue;
                files.addAll(listAllFiles(f));
            } else files.add(f);
        }
        return files;
    }
}
