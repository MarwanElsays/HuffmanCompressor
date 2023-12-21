import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    HuffmanCompresser huffmanCompresser = new HuffmanCompresser();
    HuffmanDecompresser huffmanDecompresser = new HuffmanDecompresser();
    public String getOutputPath(String inputFilePath,String opType,int n){
        Path inputPath = Paths.get(inputFilePath);
        String fileName = inputPath.getFileName().toString();
        Path inputDirectory = inputPath.getParent();
        String outputFileName;
        if("c".equals(opType)) outputFileName = "20011867." + n +"." + fileName + ".hc";
        else outputFileName = "extracted." + fileName.substring(0,fileName.length()-3);

        return inputDirectory.resolve(outputFileName).toString();
    }

    public static void main(String[] args) throws IOException {

        Main obj = new Main();
        if(args.length > 1) {
            String opType = args[0];
            String inputFilePath = args[1];
            int n = 1;
            if (args.length > 2) n = Integer.parseInt(args[2]);

            String outputFilePath = obj.getOutputPath(inputFilePath,opType,n);
            if ( "c".equals(opType)) {
                obj.huffmanCompresser.DoCompression(inputFilePath,outputFilePath,n);
            } else if ("d".equals(opType)) {
                obj.huffmanDecompresser.doDecompression(inputFilePath,outputFilePath);
            } else {
                System.out.println("Invalid Operation");
            }
        }
        else{
            System.out.println("you provided few number of args");
        }
    }
}