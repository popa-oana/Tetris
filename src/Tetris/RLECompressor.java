package Tetris;

public class RLECompressor {

    public static String compress(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder compressed = new StringBuilder();
        char currentChar = input.charAt(0);
        int count = 1;

        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == currentChar) {
                count++;
            } else {
                compressed.append(currentChar).append(count).append(" ");
                currentChar = input.charAt(i);
                count = 1;
            }
        }

        compressed.append(currentChar).append(count);
        return compressed.toString();
    }

    public static String decompress(String compressed) {
        if (compressed == null || compressed.isEmpty()) {
            return "";
        }

        StringBuilder decompressed = new StringBuilder();
        String[] parts = compressed.split(" ");

        for (String part : parts) {
            if (part.length() < 2) continue;

            char value = part.charAt(0);
            int count = Integer.parseInt(part.substring(1));

            for (int i = 0; i < count; i++) {
                decompressed.append(value);
            }
        }

        return decompressed.toString();
    }
}