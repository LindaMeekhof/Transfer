package general;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;



public class FileManager {

    
    
    /**
     * Constructor for FileManager. 
     * The FileMager will handle the transformation from file to byteArray and the other way around. 
     */
    public FileManager() {
        
    }
    
    public static byte[] FileToByteArray(String path) throws Exception {
      //  String pathName = "/Users/Linda.Meekhof/Downloads/num2/src/main/java/files/DSC_0042.jpg ";
        
        File fileToTransmit = new File(path);
       
        //read all the data from the file 
        try (FileInputStream fileStream = new FileInputStream(fileToTransmit)) {
        
            //create new array of bytes
            byte[] fileContentToBytes = new byte[(int) fileToTransmit.length()];
            
           // Print bytes[]
            for (int i = 0; i < fileContentToBytes.length; i++) {
                //reads some number of bytes form the input stream and stores them into the buffer array 
                int nextByte = fileStream.read();
                if (nextByte == -1) {
                    throw new Exception("File is smaller than indicated");
                }
                
                fileContentToBytes[i] = (byte) nextByte;
                System.out.print((char) fileContentToBytes[i]);
            }
            return fileContentToBytes;
        } catch (FileNotFoundException e) {
            System.out.println("ERROR FileNotFound when FileToByteArray");
            e.printStackTrace();
            return null;
        } catch (IOException e1) {
            System.out.println("ERROR IOException FileToByteArray");
            e1.printStackTrace();
            return null;
        }
    }
   
    //List of filepaths
    //FileID
    
    private int HEADERSIZE = 8; //number of header bytes UDP
    private int DATASIZE = 200; //max. number of user data bytes in each packet. 
    
    public void fromFileContentToDataPacket() {
        //arraycopy(object scr, int srcPis, Object dest, int destPos, int length)
      
        
        //Determine length packet
      //  int dataLength = Math.min(DATASIZE, file);
        
    }
    
    
    private static final String fileDirectory = "files";
    private static final String slash = "/";
    /**
     * Writes the contents of file array to the specified file.
     * @param fileContents the contents to write
     * @param id the file ID
     */
    public static void setFileContents(byte[] fileContents, String fileName) {
        //File path/name/...
        String workingDirectory = System.getProperty("user.dir");


        String filePath = workingDirectory + slash + fileDirectory + slash + fileName;

        File fileToWrite = new File(fileName);
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (Exception e) {
            System.out.println("ERROR by setFileContents");
        }
    }

    
    
    
    public static void main(String[] arg) throws Exception {
        String workingDirectory = System.getProperty("user.dir");
       String path = workingDirectory + "/" + "DSC_0042.jpg";
        
        byte[] byteArray =  FileToByteArray(path);
        
        System.out.println(byteArray);
        
    }
    
    
}
