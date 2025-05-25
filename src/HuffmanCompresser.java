import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

/*
n:window Size   int
fileSize int
compFileLastLen  // for 0's and 1's  byte
endofFilelen    byte
MapSize       int
CompressedMap
File
endOfFile  // trimmed from inputed file to make file length divisble by n

*/
public class HuffmanCompresser {

    private HashMap<ByteArrayWrapper,String> sequenceCode;
    private ByteArrayWrapper endOfFile = new ByteArrayWrapper(new byte[0]);    // Thrown at the end of the file
    private byte endOfFileLen = 0;
    private int fileSize = 0;
    private byte compFileLastLen;


    public HuffmanCompresser(){
        this.sequenceCode = new HashMap<>();
    }

    public byte[] readNormalFile(String inputFilePath) throws IOException {
        Path filePath = Paths.get(inputFilePath);
        return Files.readAllBytes(filePath);
    }

    public HashMap<ByteArrayWrapper, Integer> getFrequency(byte[] fileBytes, int n) {
        System.out.println("Original file size = " + fileBytes.length + " Bytes");
        fileSize = fileBytes.length;
        if (fileBytes.length % n != 0) {
            endOfFile = new ByteArrayWrapper(Arrays.copyOfRange(fileBytes, fileBytes.length - fileBytes.length % n, fileBytes.length));
            endOfFileLen = (byte) endOfFile.getContents().length;
        }
        HashMap<ByteArrayWrapper, Integer> map = new HashMap<>();
        int sz = fileBytes.length - fileBytes.length % n;
        for (int i = 0; i < sz; i += n) {
            byte[] segment = Arrays.copyOfRange(fileBytes, i, i + n);
            ByteArrayWrapper key = new ByteArrayWrapper(segment);
            map.put(key, map.getOrDefault(key, 0) + 1);
        }
        return map;
    }

    public HuffmanNode createHuffmanTree(HashMap<ByteArrayWrapper,Integer> freqMap){

        Comparator<HuffmanNode> frequencyComparator = Comparator.comparingInt(node -> node.freq);

        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>(frequencyComparator);
        for (Map.Entry<ByteArrayWrapper, Integer> entry : freqMap.entrySet()) {
            HuffmanNode node = new HuffmanNode(null,null,entry.getKey(),entry.getValue());
            priorityQueue.add(node);
        }

        while(priorityQueue.size()>1){
            HuffmanNode newnode = new HuffmanNode();
            newnode.left = priorityQueue.poll();
            newnode.right = priorityQueue.poll();
            newnode.freq = newnode.left.freq + newnode.right.freq;
            priorityQueue.add(newnode);
        }
        return priorityQueue.poll();
    }

    public void generateCode(HuffmanNode node,String val){
        if(node == null)return;
        if(node.isLeaf()) sequenceCode.put(node.ch,val);

        generateCode(node.left,val+"0");
        generateCode(node.right,val+"1");
    }

    public byte[] compressMap() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteStream);
        try {
            for (Map.Entry<ByteArrayWrapper, String> entry : sequenceCode.entrySet()) {
                byte[] keyBytes = entry.getKey().getContents();
                String code = entry.getValue();
                // 1. Write key length (1 byte) + key bytes
                dos.writeByte(keyBytes.length);  // Key length (0-255)
                dos.write(keyBytes);             // Actual key bytes
                // 2. Write code length in bits (2 bytes) + packed bits
                int bitLength = code.length();
                dos.writeShort(bitLength);
                // Pack the Huffman code into bytes
                int pos = 0;
                while (pos < bitLength) {
                    byte b = 0;
                    int bitsToPack = Math.min(8, bitLength - pos);
                    for (int i = 0; i < bitsToPack; i++) {
                        if (code.charAt(pos + i) == '1') {
                            b |= (byte) (1 << (7 - i));  // Set the bit
                        }
                    }
                    dos.writeByte(b);
                    pos += 8;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress Huffman map", e);
        }
        return byteStream.toByteArray();
    }

    public byte[] compressFile(byte[] fileBytes,int n){

        StringBuilder st = new StringBuilder();
        int sz = fileBytes.length - fileBytes.length%n;
        for(int i=0;i<sz;i+=n){
            ByteArrayWrapper s = new ByteArrayWrapper(Arrays.copyOfRange(fileBytes, i, i + n));
            st.append(sequenceCode.get(s));
        }

        int len = st.length(),j=0;
        compFileLastLen = (byte) (st.length()%8);
        System.out.println("compFileLastLen:"+ compFileLastLen);
        byte[] bytesToFile = new byte[(int) ceil(st.length()/8.0)];
        for(int i=0;i<st.length();i+=8){
            String binaryString = st.substring(i,min(i+8,st.length()));
            byte b = (byte) Integer.parseInt(binaryString, 2);
            bytesToFile[j++] = b;
        }
        return bytesToFile;
    }

    public void outputToFile(int n,byte[] compressedMap,byte[] bytesToFile,String outputFilePath) throws IOException {

        DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFilePath));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dos);

        // System.out.println(sequenceCode.toString());

        //System.out.println("compressedMap Length:"+ compressedMap.length);
        //System.out.println("compressedFile length:"+bytesToFile.length);

        long compressedFileSize = 14 + compressedMap.length + bytesToFile.length + endOfFileLen;
        System.out.println("Compressed file size = "+compressedFileSize + " Bytes");

        double compressionRatio = (double) compressedFileSize/fileSize;
        System.out.println("Compression ratio = " + compressionRatio);

//        System.out.println("Map Before Saving");
//        for(int i=0;i<compressedMap.length;i++)
//            System.out.print((char)compressedMap[i]);
//        System.out.println();
//        System.out.println("file Before Saving");
//        for(int i=0;i<bytesToFile.length;i++)
//            System.out.print((char)bytesToFile[i]);
//        for(int i=0;i<endOfFile.getBytes().length;i++)
//            System.out.print((char)endOfFile.getBytes()[i]);
//
//        System.out.println();
//        System.out.println("--------------------------------------------------");
//
        dos.writeInt(n);
        dos.writeInt(fileSize);
        dos.writeByte(compFileLastLen);
        dos.writeByte(endOfFileLen);
        dos.writeInt(compressedMap.length);
        bufferedOutputStream.write(compressedMap);
        bufferedOutputStream.write(bytesToFile);
        bufferedOutputStream.write(endOfFile.getContents());

        bufferedOutputStream.close();


//        FileInputStream fis = new FileInputStream(outputFilePath);
//        DataInputStream dis = new DataInputStream(fis);
//
//        int unCompN = dis.readInt();
//        System.out.println("n:"+ unCompN);
//
//        int fileSize = dis.readInt();
//        System.out.println("fileSize:"+ fileSize);
//
//        byte unCompcompFileLastLen = dis.readByte();
//        System.out.println("compFileLastLen:"+ unCompcompFileLastLen);
//
//        byte unCompendofFilelen = dis.readByte();
//        System.out.println("endofFilelen:"+ unCompendofFilelen);
//
//        int unCompMapLength = dis.readInt();
//        System.out.println("MapLength:"+ unCompMapLength);
//
//        byte[] mapRead = dis.readNBytes(unCompMapLength);
//        byte[] file = dis.readAllBytes();
//        System.out.println("Map after Saving");
//        for(int i=0;i<mapRead.length;i++)
//            System.out.print((char)mapRead[i]);
//        System.out.println();
//        System.out.println("file after Saving");
//        for(int i=0;i<file.length;i++)
//            System.out.print((char)file[i]);

    }

    public void DoCompression(String inputFilePath,String outputFilePath,int n) throws IOException {
        byte[] fileBytes = readNormalFile(inputFilePath);

        // Get Frequency Map
        HashMap<ByteArrayWrapper,Integer> map = getFrequency(fileBytes,n);
        //Create Huffman Tree
        HuffmanNode root = createHuffmanTree(map);

        //Generate code for each n bytes
        generateCode(root, "");
//        for (Map.Entry<ByteArrayWrapper, String> entry : sequenceCode.entrySet()) {
//            System.out.println(Arrays.toString(entry.getKey().getContents()) + " : " + entry.getValue());
//        }

        //Return Compressed File
        byte[] bytesToFile = compressFile(fileBytes,n);

        //compress Map
        byte[] compressedMap = compressMap();

        //Save map and file to compressed file
        outputToFile(n,compressedMap,bytesToFile,outputFilePath);
    }

}
