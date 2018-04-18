package general;



public interface Constants {
    
    //Constants for Header
    public static int HEADERSIZE = 24;
    public static final int DATASIZE = 300; 

    //DOWNLOAD
    public static final int FILE_REQUEST = 8;  
    public static final int META = 10;
    public static final int META_ACK = 22;        
    public static final int ACK = 2;       
    public static final int DOWNLOAD = 32;   

    public static final int FIN = 4;         
    public static final int FIN_ACK = 5; 

    //FILELIST
    public static final int FILELIST= 20;
    public static final int LISTACK = 40;  
    public static final int ACKACK = 50;

    public static final int PAUSE = 12;
    public static final int RESUME = 16;
    
    public static final int UPLOAD = 18;

    public static final int BIG = 1500;    
    public static final int DESTINATIONPORT = 6667;   
    public final int EMPTY = 0;    
    public final int SIZE_INT_BYTE = 4;
    
}
