package ai.based.vcs.domain.model;

import java.nio.charset.StandardCharsets;

/**
 * Blob domain model
 * Represents a file object
 */
public class Blob {
    private final byte[] content;

    public Blob(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    public byte[] getStorageBytes() {
        // Git-style header: "blob {len}\0" + content
        String header = "blob " + content.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, out, 0, headerBytes.length);
        System.arraycopy(content, 0, out, headerBytes.length, content.length);
        return out;
    }
}
