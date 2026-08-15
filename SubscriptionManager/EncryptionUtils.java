//This Part is done By Raiyan Choudhury
import java.util.Base64;


public class EncryptionUtils {

    private static final int XOR_KEY = 21;

 
    public static String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        char[] chars = plainText.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ XOR_KEY);
        }

        return Base64.getEncoder().encodeToString(new String(chars).getBytes());
    }

    // Reverses the transformation performed by encrypt().
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            char[] chars = new String(decodedBytes).toCharArray();

            for (int i = 0; i < chars.length; i++) {
                chars[i] = (char) (chars[i] ^ XOR_KEY);
            }

            return new String(chars);
        } catch (IllegalArgumentException ex) {
            // Not a validly encoded value (e.g. legacy plain text) - return as-is
            return encryptedText;
        }
    }
}