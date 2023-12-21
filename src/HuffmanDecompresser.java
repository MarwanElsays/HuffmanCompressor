import java.io.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static java.lang.Math.min;

public class HuffmanDecompresser {

    HashMap<String,String> decompresedMap;

    private String endOfFile = "";
    private int n;

    private int fileSize = 0;
    private byte endOfFileLen;
    private byte compFileLastLen;

    public HuffmanDecompresser(){
        this.decompresedMap = new HashMap<>();
    }

    public static String convertToBinaryString(byte format,byte b){
        String intToBin = Integer.toBinaryString(b & 0xFF);
        byte reqLen = (byte) (format - intToBin.length());
        if(reqLen == 0)return intToBin;
        else if(reqLen == 1) return "0" + intToBin;
        else if(reqLen == 2) return "00" + intToBin;
        else if(reqLen == 3) return "000" + intToBin;
        else if(reqLen == 4) return "0000" + intToBin;
        else if(reqLen == 5) return "00000" + intToBin;
        else if(reqLen == 6) return "000000" + intToBin;
        else return "0000000" + intToBin;
    }

    public byte[] readCompressedFile(String inputFilePath) throws IOException {

        DataInputStream dis = new DataInputStream(new FileInputStream(inputFilePath));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(dis);

        n = dis.readInt();
        System.out.println("n:"+ n);

        fileSize = dis.readInt();
        System.out.println("fileSize:"+ fileSize);

        compFileLastLen = dis.readByte();
        System.out.println("CompcompFileLastLen:"+ compFileLastLen);

        endOfFileLen = dis.readByte();
        System.out.println("unCompendofFilelen:"+ endOfFileLen);

        int mapLen = dis.readInt();
        System.out.println("unCompMapLength:"+ mapLen);

        byte[] mapRead = dis.readNBytes(mapLen);
        byte[] fileBytes = dis.readAllBytes();
        System.out.println("Files Length:"+ fileBytes.length);

        bufferedInputStream.close();

        decompressMap(mapRead);

        return fileBytes;
    }

    public void decompressMap(byte[] map){

        int i = 0;
        while(i<map.length){

            String value = new String(map,i,n);
            i+=n;
            StringBuilder key = new StringBuilder();
            while(',' != ((char)map[i+2])){
                //key.append(String.format("%8s", Integer.toBinaryString(map[i] & 0xFF)).replace(' ', '0'));
                key.append(convertToBinaryString((byte)8,map[i]));
                i++;
            }

            byte format = map[i+1] == 0?8: map[i+1];
            //String lastByteLen = map[i+1] == 0?"%8s": "%" + map[i+1] + "s";
            //key.append(String.format(lastByteLen,Integer.toBinaryString(map[i] & 0xFF)).replace(' ', '0'));
            key.append(convertToBinaryString(format,map[i]));

            i+=3;
            decompresedMap.put(key.toString(),value);
        }

        System.out.println("Done Map Decompressing");
        //System.out.println(decompresedMap.toString());
    }

//    public byte[] decompressFile(byte[] fileBytes){
//
//        // StringBuilder charsToBinaryString = new StringBuilder();
//        int sz = fileBytes.length - endOfFileLen;
//        if(endOfFileLen > 0) endOfFile = new String(fileBytes,sz,fileBytes.length-sz);
//        byte format = 8;
//        StringBuilder s = new StringBuilder();
//        // StringBuilder decomprssed = new StringBuilder();
//        byte[] dec = new byte[fileSize];
//
//        int k = 0;
//        //StringBuilder s = new StringBuilder();
//        for(int i=0;i<sz;i++){
//            if(i==sz-1 && compFileLastLen!=0)format = compFileLastLen;
//            String BinaryString = convertToBinaryString(format,fileBytes[i]);
//            //charsToBinaryString.append(BinaryString);
//            for(int j=0;j<BinaryString.length();j++){
//
//                //s.append(BinaryString.charAt(j));
//                s.append(BinaryString.charAt(j));
////                if(decompresedMap.containsKey(s)){
////                    decomprssed.append(decompresedMap.get(s));
////                    //charsToBinaryString.setLength(0);
////                    s = "";
////                }
//
//                String found = decompresedMap.get(s.toString());
//                if(found != null) {
//                    byte[] fbytes = found.getBytes();
//                    for (int q = 0; q < fbytes.length; q++)
//                        dec[k++] = fbytes[q];
//
//                    s.setLength(0);
//                    //s.setLength(0);
//                }
//            }
//        }
//
//        fileBytes = null;
//        //System.out.println("finshed first part");
//        //StringBuilder decomprssed = new StringBuilder();
//        //byte[] dec = new byte[fileSize+1];
//        //int k = 0;
//        //String s = "";
//        //for(int i=0;i<charsToBinaryString.length();i++){
//        //charsToBinaryString.append(charsToBinaryString.charAt(i));
//        //String SearchKey = charsToBinaryString.toString();
////            if(i%100000 == 0)
////                System.out.println(i);
////            s = s + charsToBinaryString.charAt(i);
////            String found = decompresedMap.get(s);
////            if(found != null){
////                byte[] fbytes = found.getBytes();
////                for(int j=0;j<fbytes.length;j++)
////                    dec[k++] = fbytes[j];
//
//
//        //decomprssed.append(found);
//        //charsToBinaryString.setLength(0);
//        // s = "";
//        // }
//        // }
//        //if(endOfFileLen > 0) decomprssed.append(endOfFile);
//        if(endOfFileLen > 0) {
//            byte[] fbytes = endOfFile.getBytes();
//            for(int j=0;j<fbytes.length;j++)
//                dec[k++] = fbytes[j];
//        }
//
//        System.out.println("File Decompressed");
//        // return decomprssed.toString();
//        return dec;
//    }

    public byte[] decompressFile(byte[] fileBytes){

        // StringBuilder charsToBinaryString = new StringBuilder();
        int sz = fileBytes.length - endOfFileLen;
        if(endOfFileLen > 0) endOfFile = new String(fileBytes,sz,fileBytes.length-sz);
        int format = 8;
        StringBuilder s = new StringBuilder();
        // StringBuilder decomprssed = new StringBuilder();
        byte[] dec = new byte[fileSize];

        int k = 0;
        //StringBuilder s = new StringBuilder();
        for(int i=0;i<sz;i++){
            if(i==sz-1 && compFileLastLen!=0)format = compFileLastLen;
            //String BinaryString = convertToBinaryString(format,fileBytes[i]);
            //charsToBinaryString.append(BinaryString);
            for(int j=min(7,format-1);j>=0;j--){

                int val = ((fileBytes[i] >> j) & 1);
                s.append(val);

                String found = decompresedMap.get(s.toString());
                if(found != null) {
                    byte[] fbytes = found.getBytes();
                    for (int q = 0; q < fbytes.length; q++)
                        dec[k++] = fbytes[q];

                    s.setLength(0);
                    //s.setLength(0);
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

    public void outputToFile(/*String*/ byte[] decompressedFile,String outputFilePath) throws IOException {

        DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFilePath));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dos);

        //System.out.println("Decompressed File length:"+ decompressedFile.length());
        System.out.println("Decompressed File length:"+ decompressedFile.length);
        //bufferedOutputStream.write(decompressedFile.getBytes());
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
