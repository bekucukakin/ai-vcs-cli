package org.example.constant;

import java.io.File;

public final class VcsConstants {
    private VcsConstants() {} // new ile oluşturulmasın diye

    public static final File VCS_DIR = new File(".vcs");
    public static final File OBJECTS_DIR = new File(VCS_DIR, "objects");
    public static final File REFS_HEADS_DIR = new File(VCS_DIR, "refs/heads");
    public static final File HEAD_FILE = new File(VCS_DIR, "HEAD");
    public static final File LOGS_DIR = new File(VCS_DIR, "logs/refs/heads");
    public static final File HOOKS_DIR = new File(VCS_DIR, "hooks");
    public static final File STAGING_FILE = new File(VCS_DIR, "staging.json");
    public static final File STAGING_DIR = new File(".vcs/staging");

}
