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
MapLength       int
CompressedMap
File
endOfFile  // trimmed from inputed file to make file length divisble by n

*/
public class HuffmanCompresser {

    private HashMap<String,String> sequenceCode;
    private String endOfFile = "";    //Thrown at the end of the file
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

    public HashMap<String,Integer> getFrequency(byte[] fileBytes,int n){

        System.out.println("File Size: " + fileBytes.length + " bytes");
        fileSize = fileBytes.length;
        if(fileBytes.length%n != 0){
            endOfFile = new String(fileBytes,fileBytes.length-fileBytes.length%n,fileBytes.length%n);
            endOfFileLen = (byte)endOfFile.length();
        }

        HashMap<String,Integer> map = new HashMap<>();
        int sz = fileBytes.length - fileBytes.length%n;
        for(int i=0;i<sz;i+=n){
            String resultString = new String(fileBytes, i, n);
            Integer currentValue = map.get(resultString);
            if (currentValue != null) map.put(resultString, currentValue + 1);
            else map.put(resultString, 1);
        }

        return map;
    }

    public HuffmanNode createHuffmanTree(HashMap<String,Integer> freqMap){

        Comparator<HuffmanNode> frequencyComparator = Comparator.comparingInt(node -> node.freq);

        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>(frequencyComparator);
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
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

    public byte[] compressMap(){
        List<Byte> compressedMapByte = new ArrayList<>();
        for (Map.Entry<String, String> entry : this.sequenceCode.entrySet()) {

            // Put the key first
            byte[] keyBytes = entry.getKey().getBytes();
            for(int i=0;i<keyBytes.length;i++)
                compressedMapByte.add(keyBytes[i]);

            //encode the value then put it to the file
            String val = entry.getValue();
            for(int i=0;i<val.length();i+=8){
                String binaryString = val.substring(i,min(i+8,val.length()));
                byte b = (byte) Integer.parseInt(binaryString, 2);
                compressedMapByte.add(b);
            }
            byte len = (byte) (val.length()%8); //length of the last byte
            compressedMapByte.add(len);

            //Add a comma
            compressedMapByte.add((byte)44);
        }

        byte[] mapBytesToFile = new byte[compressedMapByte.size()];
        int i = 0;
        for(byte b:compressedMapByte) mapBytesToFile[i++] = b;

        return mapBytesToFile;
    }

    public byte[] compressFile(byte[] fileBytes,int n){

        StringBuilder st = new StringBuilder();
        int sz = fileBytes.length - fileBytes.length%n;
        for(int i=0;i<sz;i+=n){
            String s = new String(fileBytes, i, n);
            st.append(sequenceCode.get(s));
        }

        int len = st.length(),j=0;
        compFileLastLen = (byte) (st.length()%8);
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

        System.out.println("compressedMap Length:"+ compressedMap.length);
        System.out.println("compressedFile length:"+bytesToFile.length);

//        System.out.println("Map Before Saving");
//        for(int i=0;i<compressedMap.length;i++) System.out.print((char)compressedMap[i]);
//        System.out.println();
//        System.out.println("file Before Saving");
//        for(int i=0;i<bytesToFile.length;i++) System.out.print((char)bytesToFile[i]);

        dos.writeInt(n);
        dos.writeInt(fileSize);
        dos.writeByte(compFileLastLen);
        dos.writeByte(endOfFileLen);
        dos.writeInt(compressedMap.length);
        bufferedOutputStream.write(compressedMap);
        bufferedOutputStream.write(bytesToFile);
        bufferedOutputStream.write(endOfFile.getBytes());

        bufferedOutputStream.close();

    /*
        FileInputStream fis = new FileInputStream(outputFilePath);
        DataInputStream dis = new DataInputStream(fis);
        System.out.println();
        int unCompN = dis.readInt();
        System.out.println("unCompN:"+ unCompN);

        byte unCompcompFileLastLen = dis.readByte();
        System.out.println("CompcompFileLastLen:"+ unCompcompFileLastLen);

        byte unCompendofFilelen = dis.readByte();
        System.out.println("unCompendofFilelen:"+ unCompendofFilelen);

        int unCompMapLength = dis.readInt();
        System.out.println("unCompMapLength:"+ unCompMapLength);

        byte[] mapRead = dis.readNBytes(unCompMapLength);
        byte[] file = dis.readAllBytes();
        System.out.println("Map after Saving");
        for(int i=0;i<mapRead.length;i++)
            System.out.print((char)mapRead[i]);
        System.out.println();
        System.out.println("file after Saving");
        for(int i=0;i<file.length;i++)
            System.out.print((char)file[i]);
     */
    }

    public void DoCompression(String inputFilePath,String outputFilePath,int n) throws IOException {
        byte[] fileBytes = readNormalFile(inputFilePath);

        // Get Frequency Map
        HashMap<String,Integer> map = getFrequency(fileBytes,n);

        //Create Huffman Tree
        HuffmanNode root = createHuffmanTree(map);

        //Generate code for each n bytes
        generateCode(root,"");

        //Return Compressed File
        byte[] bytesToFile = compressFile(fileBytes,n);

        //compress Map
        byte[] compressedMap = compressMap();

        //Save map and file to compressed file
        outputToFile(n,compressedMap,bytesToFile,outputFilePath);
    }

}
