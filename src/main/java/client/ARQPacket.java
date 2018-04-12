package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ARQPacket {

    //Constants for Header
    private static int HEADERSIZE = 24;
    
    //Flags; waar is het pakketje voor
    private static final int SYN = 1;         //setup, for making a storage for the file that will be send
    private static byte[] SYN_B = new byte[] {0b00000000, 0b00000000, 0b00000000, 0b00000001};
    private static final int ACK = 2;         //ack of the send data.
    private static final int SYN_ACK = 3;     //doorgaan met zenden
    private static final int FIN = 4;         //finished with the file
    private static final int FILE_REQUEST = 8; //fileRequest 
   
    
    //Starting place in the header
    private static int FLAG = 0;
    private static int NAME_FILE = 4;
    private static int SEQUENCE_NR = 8; 
    private static int ACK_NUMBER = 12;
    private static int CONTENT_LENGTH = 16;
    private static int OPTION_POS = 20;
    
    
    //header info
    private int sequenceNumber;
    private int ackNumber;
    private int contentLength;
    private int flag;  
    private int option;
    private int filename;
    
    private static int DATASIZE = 200;
 //   private static int SEQ_NR = 4;

    //byte array Header
    private byte[] header = new byte[HEADERSIZE];
    
    //byte array for data file
    private byte[] data = new byte[0];
    
    private byte[] packet = new byte[0];
    
    /**
     * ARQPAcket constructor
     */
    public ARQPacket () {
        flag = 11;
        filename = 22;
        sequenceNumber = 3;
        ackNumber = 4;
        contentLength = 5;
        option = 6;
        
        header = getHeader();
    }
    
    public ARQPacket (int flag, int filename, int sequenceNumber, int ackNumber, int contentLength, int option) {
        this.flag = flag;
        this.filename = filename;
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;
        this.contentLength = contentLength;
        this.option = option;
        
        header = getHeader();
    }
    
    public ARQPacket(DatagramPacket datagram) {
       
       byte[] dataReceivedPacket = datagram.getData();
       
       flag = getFlags(datagram);
       filename = getFileName(datagram);
 
    } 
    
    public byte[] getHeader() {
        //byte buffer for the header
        ByteBuffer buffer = ByteBuffer.allocate(HEADERSIZE);
        
        // 1. Flag
        ByteBuffer flags =  ByteBuffer.allocate(4).putInt(flag);
        buffer.position(FLAG);
        buffer.put(flags.array());
        
        // 2. Name/Number file
        ByteBuffer fileName =  ByteBuffer.allocate(4).putInt(filename);
        buffer.position(NAME_FILE);
        buffer.put(fileName.array());
        
        // 3. SequenceNumber 
        ByteBuffer seqNumber = ByteBuffer.allocate(4).putInt(sequenceNumber);
        buffer.position(SEQUENCE_NR);
        buffer.put(seqNumber.array());
    
        // 4. AcknowledgementNumber
        ByteBuffer ACKNumber = ByteBuffer.allocate(4).putInt(ackNumber);
        buffer.position(ACK_NUMBER);
        buffer.put(ACKNumber.array());
        
        // 5. ContentLength
        ByteBuffer contentLen = ByteBuffer.allocate(4).putInt(contentLength);
        buffer.position(CONTENT_LENGTH);
        buffer.put(contentLen.array());
        
        // 6. Option
        ByteBuffer opt = ByteBuffer.allocate(4).putInt(option);
        buffer.position(OPTION_POS);
        buffer.put(opt.array());
        
        
        return buffer.array();
    }

 
    /**
     * Get the Flag part of the Datagram packet
     * @param packet
     * @return 
     */
    public int getFlags(DatagramPacket packet) {
        byte[] getFlags = Arrays.copyOfRange(packet.getData(), FLAG, FLAG + 4);
        int flag = ByteBuffer.wrap(getFlags).getInt();
        return flag;
    }
    
    public int getFileName(DatagramPacket packet) {
        byte[] getFlags = Arrays.copyOfRange(packet.getData(), NAME_FILE, NAME_FILE + 4);
        int namefile = ByteBuffer.wrap(getFlags).getInt();
        return namefile;
    }
    
    public int getSequenceNumber(DatagramPacket packet) {
        byte[] getSeqNr = Arrays.copyOfRange(packet.getData(), SEQUENCE_NR, SEQUENCE_NR + 4);
        int seqNr = ByteBuffer.wrap(getSeqNr).getInt();
        return seqNr;
    }
    
  
 
    
    // Getters and setters for header
    //--------------------------------------------------
    
    public byte[] getPacket() {
        return packet;
    }
    
    
    public int getFlag() {
        return flag;
    }
    
  
    
    public int getNameFile() {
        return header[2];
    }

    public int getSequenceNumber() {
        return header[SEQUENCE_NR];
    }
    
    public int getACKNumber() {
        return header[ACK_NUMBER];
    }
    
    public int getContentLength() {
        return header[CONTENT_LENGTH];
    }
    
    
    //Setters 
    public void setNameFile() {
        //TODO
    }
    
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    
    public void setFlags(int flag) {
        this.flag = flag;
    }
    
    public void setACKNumber() {
        header[4] = 5;
    }
    
    public void setContentLength() {
        header[6] = 1;
    }
    
    //Setters for flag field
   
    
    //Integer is 31 bits.
    public static byte[] intToThreeBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.clear();
        buffer.putInt(value);
        return Arrays.copyOfRange(buffer.array(), 1, 4);
    }
    
    public byte[] intTo3Bytes( final int i ) {
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
    
    
    /**
     * Create DatagramPacket 
     * @param fileContent
     * @param header
     * @param filepointer
     * @param address
     * @param port
     * @return
     */
    public DatagramPacket createPacket(byte[] fileContent, byte[] header, int filepointer, InetAddress address, int port) {
        //lengte van de header + datastukje
        byte[] packet = new byte[HEADERSIZE + DATASIZE];
        
        //First enter the header content
        System.arraycopy(header, 0, packet, 0, header.length);
        
        //Secondly enter the data content
        System.arraycopy(fileContent, filepointer, packet, header.length + 1, DATASIZE);
        
        return new DatagramPacket(packet, packet.length, address, port);
    }
    
    
    
    public static void main(String[] args) {
        ARQPacket packet = new ARQPacket();
        byte[] head = packet.getHeader();
    //    packet.setFlags(SYN);

        
        
        System.out.println("test");
    }
    
 
    
}