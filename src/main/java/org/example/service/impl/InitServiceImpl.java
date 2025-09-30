package org.example.service.impl;

import org.example.service.InitService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class InitServiceImpl implements InitService {
    private static final File VCS_DIR = new File(".vcs");
    private static final File OBJECTS_DIR = new File(VCS_DIR, "objects");
    private static final File REFS_HEADS_DIR = new File(VCS_DIR, "refs/heads");
    private static final File HEAD_FILE = new File(VCS_DIR, "HEAD");
    private static final File LOGS_DIR = new File(VCS_DIR, "logs/refs/heads");
    private static final File HOOKS_DIR = new File(VCS_DIR, "hooks");
    private static final File STAGING_FILE = new File(VCS_DIR, "staging.json");

    @Override
    public void initRepo() throws IOException {
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

        STAGING_FILE.getParentFile().mkdirs();
        Files.writeString(STAGING_FILE.toPath(), "{}");
        //todo: init kısmının doğruluğu kontrol edilecek. incelenecek
        System.out.println("Initialized empty AI_VCS repository in " + VCS_DIR.getAbsolutePath());

    }

}
