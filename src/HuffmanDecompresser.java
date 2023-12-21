import java.io.*;
import java.util.*;

import static java.lang.Math.min;

public class HuffmanDecompresser {

    HashMap<List<Byte>,String> decompresedMap;

    private String endOfFile = "";
    private int n;

    private int fileSize = 0;
    private byte endOfFileLen;
    private byte compFileLastLen;

    public HuffmanDecompresser(){
        this.decompresedMap = new HashMap<>();
    }

    public byte[] readCompressedFile(String inputFilePath) throws IOException {

        DataInputStream dis = new DataInputStream(new FileInputStream(inputFilePath));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(dis);

        n = dis.readInt();
        System.out.println("n:"+ n);

        fileSize = dis.readInt();
        System.out.println("fileSize:"+ fileSize);

        compFileLastLen = dis.readByte();
        System.out.println("compressedFileLastLen:"+ compFileLastLen);

        endOfFileLen = dis.readByte();
        System.out.println("endofFilelen:"+ endOfFileLen);

        int mapLen = dis.readInt();
        System.out.println("MapLength:"+ mapLen);

        byte[] mapRead = dis.readNBytes(mapLen);
        byte[] fileBytes = dis.readAllBytes();
        System.out.println("FileBytes Array Length:"+ fileBytes.length + " Bytes");

        bufferedInputStream.close();

        decompressMap(mapRead);

        return fileBytes;
    }

    public void decompressMap(byte[] map){

        int i = 0;
        while(i<map.length){

            String value = new String(map,i,n);
            i+=n;
            List<Byte> bytes = new ArrayList<>();
            while(-106 != map[i+2]){
                for(int j=6;j>=0;j--){
                    bytes.add((byte) ((map[i] >> j) & 1));
                }
                i++;
            }
            int format = map[i+1] == 0?7: map[i+1];
            for(int j=format-1;j>=0;j--) bytes.add((byte)((map[i] >> j) & 1));
            i+=3;

            decompresedMap.put(bytes,value);
        }

        System.out.println("Done Map Decompressing");
    }

    public byte[] decompressFile(byte[] fileBytes){

        int sz = fileBytes.length - endOfFileLen;
        if(endOfFileLen > 0) {
            endOfFile = new String(fileBytes,sz,fileBytes.length-sz);
        }
        List<Byte> bytes = new ArrayList<>();
        byte[] dec = new byte[fileSize];

        int format = 8,k = 0;
        for(int i=0;i<sz;i++){
            if(i==sz-1 && compFileLastLen!=0){
                format = compFileLastLen;
            }

            int len = min(7,format-1);
            for(int j=len;j>=0;j--){

                bytes.add((byte)((fileBytes[i] >> j) & 1));
                String found = decompresedMap.get(bytes);
                if(found != null) {
                    byte[] fbytes = found.getBytes();
                    for (int q = 0; q < fbytes.length; q++){
                        dec[k++] = fbytes[q];
                    }
                    bytes = new ArrayList<>();
                }
            }
        }

        fileBytes = null;
        if(endOfFileLen > 0) {
            byte[] fbytes = endOfFile.getBytes();
            for(int j=0;j<fbytes.length;j++)
                dec[k++] = fbytes[j];
        }

        System.out.println("File Decompressed");
        return dec;
    }

    public void outputToFile(byte[] decompressedFile,String outputFilePath) throws IOException {

        DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFilePath));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dos);

        System.out.println("Decompressed File length:"+ decompressedFile.length);
        bufferedOutputStream.write(decompressedFile);
        System.out.println("Written to file Succesfully");
        bufferedOutputStream.close();
    }

    public void doDecompression(String inputFilePath,String outputFilePath) throws IOException {
        byte[] fileBytes = readCompressedFile(inputFilePath);
        byte[] decompressedFile = decompressFile(fileBytes);
        outputToFile(decompressedFile,outputFilePath);
    }

}
