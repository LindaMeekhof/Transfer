package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ARQPacket {

    //Constants for Header
    private static int HEADERSIZE = 20;
    
    //Flags; waar is het pakketje voor
    private static int SYN = 1;         //setup, for making a storage for the file that will be send
    private static byte[] SYN_B = new byte[] {0b00000000, 0b00000000, 0b00000000, 0b00000001};
    private static int ACK = 2;         //ack of the send data.
    private static int SYN_ACK = 3;     //doorgaan met zenden
    private static int FIN = 4;         //finished with the file
    private static int FILE_REQUEST = 8; //fileRequest 
   
    //Starting place in the header
    private static int FLAG = 0;
    private static int NAME_FILE = 4;
    private static int SEQUENCE_NR = 8; 
    private static int ACK_NUMBER = 11;
    private static int CONTENT_LENGTH = 14;
    private static int OPTION = 17;
    
    
    //header info
    private int sequenceNumber;
    private int ackNumber;
    private int contentLength;
    private int flag;               
   
    
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
        sequenceNumber = 0;
        ackNumber = 0;
        contentLength = 0;
        flag = 0;
    }
    
    public byte[] getHeader() {
        //byte buffer for the header
        ByteBuffer buffer = ByteBuffer.allocate(HEADERSIZE);
        
        //Flag
        ByteBuffer flags =  ByteBuffer.allocate(flag);
      //  buffer.put(flags.array(), FLAG, 4);
        buffer.position(FLAG);
        buffer.put(flags.array());
      //TODO  
        //Name/Number file
        ByteBuffer fileName =  ByteBuffer.allocate(flag);
        buffer.put(fileName.array(), FLAG, 5);
        buffer.position(NAME_FILE);
        buffer.put(fileName.array());
        
        //SequenceNumber 
        ByteBuffer seqNumber = ByteBuffer.allocate(4).putInt(sequenceNumber);
        
        //buffer.position(SEQUENCE_NR);
        //buffer.put(seqNumber.array());
        buffer.put(seqNumber.array(), SEQUENCE_NR, 4);
        
        //AcknowledgementNumber
        ByteBuffer ACKNumber = ByteBuffer.allocate(4).putInt(ackNumber);
        buffer.put(ACKNumber.array(), ACK_NUMBER, 4);
        
        //ContentLength
        ByteBuffer contentLen = ByteBuffer.allocate(4).putInt(contentLength);
        buffer.put(contentLen.array(), ACK_NUMBER, 4);
        
        //Option
        ByteBuffer option = ByteBuffer.allocate(4).putInt(contentLength);
        buffer.put(option.array(), ACK_NUMBER, 4);
        
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
    
    public int getSequenceNumber(DatagramPacket packet) {
        byte[] getSeqNr = Arrays.copyOfRange(packet.getData(), SEQUENCE_NR, SEQUENCE_NR + 4);
        int seqNr = ByteBuffer.wrap(getSeqNr).getInt();
        return seqNr;
    }
    
    public int getFlag(byte[] byteArray) {
        //return the byteArray into an int.
        
        return 0;
    }
    
    // Getters and setters for header
    //--------------------------------------------------
    
    public byte[] getPacket() {
        return packet;
    }
    
    public int getFlag() {
        return header[0];
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
        packet.setFlags(SYN);
        
        System.out.println("test" + packet.getHeader());
    }
    
}
