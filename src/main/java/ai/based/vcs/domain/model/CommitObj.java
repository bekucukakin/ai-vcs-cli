package ai.based.vcs.domain.model;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Commit domain model
 * Represents a commit object
 */
public class CommitObj {
    private final List<String> parents = new ArrayList<>();
    private final String treeHash;
    private final String author;
    private final long timestamp; // epoch seconds
    private final String message;

    public CommitObj(String treeHash, List<String> parents, String author, long timestamp, String message) {
        this.treeHash = treeHash;
        if (parents != null) this.parents.addAll(parents);
        this.author = author;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getTreeHash() {
        return treeHash;
    }

    public List<String> getParents() {
        return parents;
    }

    public String getAuthor() {
        return author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getStorageBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("tree ").append(treeHash).append("\n");
        for (String p : parents) sb.append("parent ").append(p).append("\n");
        sb.append("author ").append(author).append(" ").append(timestamp).append("\n\n");
        sb.append(message).append("\n");
        byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
        String header = "commit " + content.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, out, 0, headerBytes.length);
        System.arraycopy(content, 0, out, headerBytes.length, content.length);
        return out;
    }

    public static CommitObj fromContent(String content) {
        // parse basic format above
        String[] parts = content.split("\n\n", 2);
        String metadata = parts[0];
        String msg = parts.length > 1 ? parts[1].trim() : "";
        String[] lines = metadata.split("\n");
        String tree = null;
        List<String> parents = new ArrayList<>();
        String authorLine = null;
        for (String l : lines) {
            if (l.startsWith("tree ")) tree = l.substring(5).trim();
            else if (l.startsWith("parent ")) parents.add(l.substring(7).trim());
            else if (l.startsWith("author ")) authorLine = l.substring(7).trim();
        }
        String author = "";
        long ts = Instant.now().getEpochSecond();
        if (authorLine != null) {
            String[] aParts = authorLine.split(" ");
            if (aParts.length >= 2) {
                author = String.join(" ", Arrays.copyOfRange(aParts, 0, aParts.length - 1));
                try {
                    ts = Long.parseLong(aParts[aParts.length - 1]);
                } catch (NumberFormatException e) {
                    // ignore, ts default kalır
                }
            } else {
                author = authorLine;
            }
        }

        return new CommitObj(tree, parents, author, ts, msg);
    }
}
