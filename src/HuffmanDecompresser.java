import java.io.*;
import java.util.*;

import static java.lang.Math.min;

public class HuffmanDecompresser {

    private final HashMap<ByteArrayWrapper, String> decompressedMap;
    private int n;
    private int fileSize;
    private byte endOfFileLen;
    private byte compFileLastLen;
    private byte[] compressedData;
    private byte[] endOfFileBytes;

    public HuffmanDecompresser() {
        this.decompressedMap = new HashMap<>();
    }

    public void readCompressedFile(String inputFilePath) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFilePath)));

        // Read header information
        n = dis.readInt();
        fileSize = dis.readInt();
        compFileLastLen = dis.readByte();
        endOfFileLen = dis.readByte();

        int mapLen = dis.readInt();
        // Read compressed map
        byte[] mapRead = dis.readNBytes(mapLen);

        // Read compressed data (excluding endOfFile bytes)
        int compressedDataLength = dis.available() - endOfFileLen;
        byte[] compressedData = dis.readNBytes(compressedDataLength);

        // Read endOfFile bytes if they exist
        byte[] endOfFileBytes = endOfFileLen > 0 ? dis.readNBytes(endOfFileLen) : new byte[0];

        dis.close();

        decompressMap(mapRead);
    }

    public void decompressMap(byte[] map) {
        decompressedMap.clear();
        int i = 0;

        while (i < map.length) {
            // 1. Read key length (1 byte)
            int keyLength = map[i++] & 0xFF;

            // 2. Read key bytes
            byte[] keyBytes = new byte[keyLength];
            System.arraycopy(map, i, keyBytes, 0, keyLength);
            i += keyLength;
            ByteArrayWrapper key = new ByteArrayWrapper(keyBytes);

            // 3. Read bit length (2 bytes)
            int bitLength = ((map[i] & 0xFF) << 8) | (map[i + 1] & 0xFF);
            i += 2;

            // 4. Calculate how many bytes the packed bits occupy
            int byteCount = (bitLength + 7) / 8;

            // 5. Read those bytes and unpack bits to recreate code string
            StringBuilder codeBuilder = new StringBuilder(bitLength);
            for (int b = 0; b < byteCount; b++) {
                byte currentByte = map[i++];
                int bitsToRead = Math.min(8, bitLength - b * 8);
                for (int bit = 7; bit >= 8 - bitsToRead; bit--) {
                    codeBuilder.append((currentByte >> bit) & 1);
                }
            }
            String code = codeBuilder.toString();

            // 6. Put into decompressed map
            decompressedMap.put(key, code);
        }
    }

    public byte[] decompressData() {
        // Convert compressed bytes to bit string
        StringBuilder bitString = new StringBuilder();
        for (int i = 0; i < compressedData.length; i++) {
            byte b = compressedData[i];
            int bitsToRead = (i == compressedData.length - 1) ? compFileLastLen : 8;
            if (bitsToRead == 0) bitsToRead = 8;

            for (int bit = min(8, bitsToRead) - 1; bit >= 0; bit--) {
                bitString.append((b >> bit) & 1);
            }
        }

        // Build reverse map for efficient lookup
        Map<String, ByteArrayWrapper> reverseMap = new HashMap<>();
        for (Map.Entry<ByteArrayWrapper, String> entry : decompressedMap.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }

        // Decode the bit string
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder currentCode = new StringBuilder();

        for (int i = 0; i < bitString.length(); i++) {
            currentCode.append(bitString.charAt(i));
            ByteArrayWrapper originalBytes = reverseMap.get(currentCode.toString());

            if (originalBytes != null) {
                try {
                    outputStream.write(originalBytes.getContents());
                } catch (IOException e) {
                    throw new RuntimeException("Error writing decompressed data", e);
                }
                currentCode.setLength(0);
            }
        }

        if (endOfFileLen > 0) {
            outputStream.write(endOfFileBytes, 0, endOfFileLen);
        }

        return outputStream.toByteArray();
    }

    public void doDecompression(String inputFilePath, String outputFilePath) throws IOException {
        // Read and separate compressed data and end-of-file bytes
        readCompressedFile(inputFilePath);

        // Decompress the main data
        byte[] decompressedData = decompressData();

        // Combine with end-of-file bytes
        byte[] finalOutput = new byte[decompressedData.length + endOfFileBytes.length];
        System.arraycopy(decompressedData, 0, finalOutput, 0, decompressedData.length);
        System.arraycopy(endOfFileBytes, 0, finalOutput, decompressedData.length, endOfFileBytes.length);

        // Verify size matches original
        if (finalOutput.length != fileSize) {
            System.out.println("Warning: Decompressed size " + finalOutput.length +
                    " doesn't match original size " + fileSize);
        }

        // Write to output file
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFilePath))) {
            out.write(finalOutput);
        }

        System.out.println("Decompression completed successfully");
        System.out.println("Original size: " + fileSize + " bytes");
        System.out.println("Decompressed size: " + finalOutput.length + " bytes");
    }
}