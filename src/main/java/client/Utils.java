package client;

import java.nio.ByteBuffer;

public class Utils {

    /**
     * Convert an Integer to an byte array.
     * @param i
     * @return
     */
    public static byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES); 
        bb.putInt(i); 
        return bb.array();
    }
    
    
    /**
     * Create a random number for file ID number.
     * Back up in a map with file String name file and ID number.
     * @return
     */
    public static int createRandomNumberForFile() {
        //TODO
        return 44;
    }
    
    private static final String fileDirectory = "files";
    private static final String slash = "/";
  
    /**
     * Get path from filename
     */
    public static String getPathFromName(String fileName) {
        String workingDirectory = System.getProperty("user.dir");

        String filePath = workingDirectory + slash + fileDirectory + slash + fileName;
        
        return filePath;
    }
    
}
