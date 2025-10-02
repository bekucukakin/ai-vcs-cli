package ai.based.vcs.domain.model;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree domain model
 * Represents a directory object
 */
public class Tree {
    public static class Entry {
        public final String type; // "blob" or "tree"
        public final String hash;
        public final String name;

        public Entry(String type, String hash, String name) {
            this.type = type;
            this.hash = hash;
            this.name = name;
        }

        @Override
        public String toString() {
            return type + " " + hash + " " + name;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void addEntry(String type, String hash, String name) {
        entries.add(new Entry(type, hash, name));
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public byte[] getStorageBytes() {
        // simple textual representation: each line as "type hash name\n"
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            sb.append(e.toString()).append("\n");
        }
        byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
        String header = "tree " + content.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, out, 0, headerBytes.length);
        System.arraycopy(content, 0, out, headerBytes.length, content.length);
        return out;
    }

    public static Tree fromContent(String content) {
        Tree t = new Tree();
        String[] lines = content.split("\n");
        for (String l : lines) {
            if (l.trim().isEmpty()) continue;
            String[] parts = l.split(" ", 3);
            if (parts.length == 3) {
                t.addEntry(parts[0], parts[1], parts[2]);
            }
        }
        return t;
    }
}
