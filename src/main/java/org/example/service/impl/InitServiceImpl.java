package org.example.service.impl;

import org.example.service.InitService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.example.constant.VcsConstants.*;

@Service
public class InitServiceImpl implements InitService {

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
