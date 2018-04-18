package general;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {

    private static String fileMap = "files";
    private static String fileDirectory = "filedirectory";
    /**
     * Constructor for FileManager. 
     * The FileMager will handle the transformation from file to byteArray and the other way around. 
     */
    public FileManager() {
        
    }
    
    public static byte[] FileToByteArray(String filename)  {
        String workingDirectory = System.getProperty("user.dir");
        
        System.out.println(new File(".").getAbsoluteFile() + "absolute");
        
        String path = workingDirectory + File.separator + fileMap + File.separator + filename; 
     
        File fileToTransmit = new File(path.trim());

        //read all the data from the file 
        try (FileInputStream fileStream = new FileInputStream(fileToTransmit)) {
        
            //create new array of bytes
            byte[] fileContentToBytes = new byte[(int) fileToTransmit.length()];
            
           // Print bytes[]
            for (int i = 0; i < fileContentToBytes.length; i++) {
                //reads some number of bytes form the input stream and stores them into the buffer array 
                int nextByte = fileStream.read();
                if (nextByte == -1) {
                    System.out.println("File is smaller than indicated");
                }
                
                //print the content
                fileContentToBytes[i] = (byte) nextByte;
             //   System.out.print((char) fileContentToBytes[i]);
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
   
   
   
    


    /**
     * Writes the contents of file array to the specified file.
     * @param fileContents the contents to write
     * @param id the file ID
     */
    public void setFileContents(byte[] fileContents, String fileName) {
        //File path/name/...
        String workingDirectory = System.getProperty("user.dir");

        System.out.println(new File(".").getAbsoluteFile() + "absolute");

        String filePath = workingDirectory + File.separator + fileMap + File.separator + fileName;
        System.out.println(filePath);
        
        File fileToWrite = new File(filePath.trim());
        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
            for (byte fileContent : fileContents) {
                fileStream.write(fileContent);
            }
        } catch (Exception e) {
            System.out.println("ERROR by setFileContents");
        }
    }
    

    /**
     * Getting the a list with all the files listed in a specific folder.
     */
    public static ArrayList<String> getFileNames() {
        ArrayList<String> fileList = new ArrayList<String>();
        
        String workingDirectory = System.getProperty("user.dir");
        String path = workingDirectory + File.separator + fileMap;
        File folder = new File(path.trim());
 
        File[] theFiles = folder.listFiles();
        for (int i = 0; i < theFiles.length; i++) {
            fileList.add(theFiles[i].getName());
        }
        return fileList;
    }
    
    /**
     * Getting the a list with all the files listed in a specific folder.
     */
    public static String getFileNamesToString() {
        String fileList = null;
        
        String workingDirectory = System.getProperty("user.dir");
        String path = workingDirectory + File.separator + fileMap;
        File folder = new File(path);
 
        File[] theFiles = folder.listFiles();
        for (int i = 0; i < theFiles.length; i++) {
            fileList = fileList + "_" + theFiles[i].getName(); 
        }
        return fileList;
    }
    
 
}
