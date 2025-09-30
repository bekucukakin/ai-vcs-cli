//package org.example.service.impl;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.example.service.*;
//import java.io.*;
//import java.nio.file.*;
//import java.time.LocalDateTime;
//import java.util.*;
//
//public class CommitServiceImpl implements CommitService {
//
//    private final ObjectMapper mapper;
//    private final StagingService stagingService;
//    private final FileService fileService;
//    private final HookService hookService;
//
//    public CommitServiceImpl(ObjectMapper mapper,
//                             StagingService stagingService,
//                             FileService fileService,
//                             HookService hookService) {
//        this.mapper = mapper;
//        this.stagingService = stagingService;
//        this.fileService = fileService;
//        this.hookService = hookService;
//    }
//
//    @Override
//    public void commit(String message) throws Exception {
//        Map<String, String> staging = stagingService.loadStaging();
//        if (staging.isEmpty()) {
//            System.out.println("No changes added to commit.");
//            return;
//        }
//        hookService.runHook("pre-commit");
//
//        String parent = fileService.readCurrentCommit();
//        String commitId = fileService.generateCommitId();
//
//        Map<String, Object> commitObj = new HashMap<>();
//        commitObj.put("id", commitId);
//        commitObj.put("message", message);
//        commitObj.put("timestamp", LocalDateTime.now().toString());
//        commitObj.put("branch", fileService.getCurrentBranch());
//        commitObj.put("files", staging);
//        commitObj.put("parents", parent.isEmpty() ? new ArrayList<>() : Collections.singletonList(parent));
//
//        fileService.writeCommit(commitId, commitObj);
//        fileService.updateBranchHead(commitId);
//        fileService.appendLog(parent, commitId, message);
//
//        stagingService.clearStaging();
//        hookService.runHook("post-commit");
//
//        System.out.println("[" + fileService.getCurrentBranch() + " " + commitId + "] " + message);
//    }
//
//    @Override
//    public void log() throws Exception {
//        fileService.printLogs(fileService.getCurrentBranch());
//    }
//
//    @Override
//    public String getLastCommitFileHash(File f) throws Exception {
//        return fileService.getLastCommitFileHash(f);
//    }
//}
