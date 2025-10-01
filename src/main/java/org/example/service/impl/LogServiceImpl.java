package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.LogService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.example.constant.VcsConstants.*;
@Service
public class LogServiceImpl implements LogService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void showLog() throws Exception {
        String branch = getCurrentBranch();
        File logFile = new File(LOGS_DIR, branch);
        if (!logFile.exists()) {
            System.out.println("No commits yet.");
            return;
        }
        List<String> lines = Files.readAllLines(logFile.toPath());
        Collections.reverse(lines);
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private String getCurrentBranch() throws Exception {
        String ref = Files.readString(HEAD_FILE.toPath()).trim();
        return ref.replace("refs/heads/", "");
    }
}
