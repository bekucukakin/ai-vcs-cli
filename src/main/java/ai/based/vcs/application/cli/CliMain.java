package ai.based.vcs.application.cli;

import ai.based.vcs.infrastructure.repository.FileRepositoryImpl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Git CLI Application
 * Entry point for the command line interface
 * Follows Single Responsibility Principle
 */
public class CliMain {

    public static void main(String[] args) throws Exception {
        // Dependency injection setup
        FileRepositoryImpl repository = new FileRepositoryImpl(Paths.get("").toAbsolutePath());
        GitService gitService = new GitServiceImpl(repository, System.getProperty("user.name", "unknown"));

        Scanner sc = new Scanner(System.in);
        System.out.println("mini-git CLI. Type 'help' for commands.");
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = splitArgs(line);
            String cmd = parts[0];
            try {
                switch (cmd) {
                    case "exit":
                    case "quit":
                        System.out.println("bye");
                        return;
                    case "help":
                        printHelp();
                        break;
                    case "init":
                        gitService.init();
                        break;
                    case "add":
                        if (parts.length < 2) System.out.println("usage: add <file>");
                        else gitService.add(parts[1]);
                        break;
                    case "commit":
                        // support commit -m "msg"
                        String msg = parseCommitMessage(parts);
                        if (msg == null) System.out.println("usage: commit -m \"message\"");
                        else gitService.commit(msg);
                        break;
                    case "status":
                        gitService.status();
                        break;
                    case "diff":
                        if (parts.length < 2) {
                            System.out.println("usage: diff <file>");
                        } else {
                            gitService.diff(parts[1]);
                        }
                        break;
                    case "log":
                        gitService.log();
                        break;
                    case "checkout":
                        if (parts.length < 2) System.out.println("usage: checkout <commit|branch>");
                        else gitService.checkout(parts[1]);
                        break;
                    case "branch":
                        if (parts.length < 2) System.out.println("usage: branch <name>");
                        else gitService.branch(parts[1]);
                        break;
                    default:
                        System.out.println("unknown command: " + cmd);
                }
            } catch (Exception ex) {
                System.out.println("error: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  init                 initialize repository");
        System.out.println("  add <file>           add file to staging");
        System.out.println("  commit -m \"msg\"     commit staged changes");
        System.out.println("  status               show status");
        System.out.println("  diff <file>          show file differences");
        System.out.println("  log                  show commits");
        System.out.println("  checkout <commit|branch>  checkout commit or branch");
        System.out.println("  branch <name>        create branch at HEAD");
        System.out.println("  help, exit");
    }

    private static String[] splitArgs(String line) {
        // crude split keeping quoted strings
        List<String> out = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                continue;
            }
            if (!inQuote && Character.isWhitespace(c)) {
                if (sb.length() > 0) {
                    out.add(sb.toString());
                    sb.setLength(0);
                }
            } else sb.append(c);
        }
        if (sb.length() > 0) out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    private static String parseCommitMessage(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if ("-m".equals(parts[i]) && i + 1 < parts.length) return parts[i + 1];
        }
        return null;
    }
}
