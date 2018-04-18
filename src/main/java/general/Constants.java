package general;



public interface Constants {
    
    //Constants for Header
    public static int HEADERSIZE = 24;
    public static final int DATASIZE = 300; 

    //Flags; waar is het pakketje voor
    public static final int ACK = 2;         //ack of the send data.
    public static final int DOWNLOAD = 32;   //doorgaan
    public static final int FIN = 4;         
    public static final int FIN_ACK = 5; //finished with the file
    public static final int FILE_REQUEST = 8; //fileRequest 
   
    public static final int META = 10;
    public static final int META_ACK = 22;
    
    
    public static final int PAUSE = 12;
    public static final int RESUME = 16;
    public static final int UPLOAD = 18;
    public static final int FILELIST= 20;
    
 
    
    public static final int DESTINATIONPORT = 6667;

    
    public final int EMPTY = 0;
    
    public final int SIZE_INT_BYTE = 4;
    
}