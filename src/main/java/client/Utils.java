package client;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
    
    public static byte[] intToByteArray(int value, int byteArrayLength) {
        byte[] bytes = ByteBuffer.allocate(byteArrayLength).putInt(value).array();
        return bytes;
    }
    
    public static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }
    
    //Integer is 31 bits.
    public static byte[] intToThreeBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.clear();
        buffer.putInt(value);
        return Arrays.copyOfRange(buffer.array(), 1, 4);
    }
    
    private static final String fileDirectory = "files";

  
    /**
     * Get path from filename
     */
    public static String getPathFromName(String fileName) {
        String workingDirectory = System.getProperty("user.dir");

        String filePath = workingDirectory + File.separator + fileDirectory + File.separator + fileName;
        
        return filePath;
    }
    
}
