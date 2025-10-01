package org.example.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

public class VcsUtils {

    // Dosya hash hesaplama
    public static String getFileHash(File f) throws Exception {
        byte[] content = Files.readAllBytes(f.toPath());
        return getHash(new String(content));
    }

    // SHA-1 hash
    public static String getHash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // verilen path için dosyaları File'a çeviriyoruz. eğer bu path " . " ise hepsini alıyoruz.
    public static List<File> resolveFilesFromPath(String path) throws IOException {
        List<File> files = new ArrayList<>();
        File f = new File(path);

        if (path.equals(".")) {
            files.addAll(listAllFiles(new File(".")));
        } else if (f.exists()) {
            files.add(f);
        }

        return files;
    }


    private static List<File> listAllFiles(File dir) throws IOException {
        List<File> files = new ArrayList<>();
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isDirectory()) {
                if (f.getName().equals(".vcs") || f.getName().equals(".git")
                        || f.getName().equals("target") || f.getName().equals(".idea"))
                    continue;
                files.addAll(listAllFiles(f));
            } else files.add(f);
        }
        return files;
    }

    // Dosya .vcs içindeyse işleme alma
    public static boolean isInsideVcs(File f) throws IOException {
        return f.getCanonicalPath().contains(".vcs");
    }
}
