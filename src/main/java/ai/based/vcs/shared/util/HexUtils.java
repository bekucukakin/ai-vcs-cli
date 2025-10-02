package ai.based.vcs.shared.util;

/**
 * Hexadecimal utility class
 * Provides methods for converting bytes to hex strings
 * Follows Single Responsibility Principle
 */
public class HexUtils {
    
    private HexUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Converts byte array to hexadecimal string
     * @param bytes the byte array to convert
     * @return hexadecimal string representation
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
