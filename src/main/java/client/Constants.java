package client;



public interface Constants {
    
    //Constants for Header
    public static int HEADERSIZE = 24;
    public static final int DATASIZE = 300; 

    //Flags; waar is het pakketje voor
    public static final int SYN = 1;         //setup, for making a storage for the file that will be send
    public static byte[] SYN_B = new byte[] {0b00000000, 0b00000000, 0b00000000, 0b00000001};
    public static final int ACK = 2;         //ack of the send data.
    public static final int SYN_ACK = 3;     //doorgaan met zenden
    public static final int DOWNLOAD = 4;   //doorgaan
    public static final int FIN = 4;         //finished with the file
    public static final int FILE_REQUEST = 8; //fileRequest 
    public static final int META = 10;
    public static final int PAUSE = 12;
    public static final int RESUME = 16;
    public static final int UPLOAD = 18;
    public static final int FILELIST= 20;
 
    
    public static final int DESTINATIONPORT = 6667;

    
    public final int EMPTY = 0;
    
}
