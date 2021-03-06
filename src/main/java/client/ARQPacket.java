package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class ARQPacket {

    //Constants for Header
    private static int HEADERSIZE = 24;

    //Starting place in the header
    private static int FLAG = 0;
    private static int NAME_FILE = 4;
    private static int SEQUENCE_NR = 8; 
    private static int ACK_NUMBER = 12;
    private static int CONTENT_LENGTH = 16;
    private static int OPTION_POS = 20;

    //header info
    private int flag; 
    private int fileID;
   
    private int sequenceNumber;
    private int ackNumber;
    private int contentLength;  
    private int option;

    private InetAddress address;
    private int destinationPort;

    private byte[] header = new byte[HEADERSIZE];
    private byte[] data = new byte[0];  
    private byte[] packet = new byte[0];
    
    /**
     * ARQPAcket constructor
     */
    public ARQPacket () {

    }
    
    public ARQPacket (InetAddress destination, int port) {
          address = destination;
          destinationPort = port;
    }
    
    /**
     * Constructor for only the header
     * @param flag
     * @param filename
     * @param sequenceNumber
     * @param ackNumber
     * @param contentLength
     * @param option
     * @throws Exception
     */
    public ARQPacket (int flag, int filename, int sequenceNumber, 
            int ackNumber, int contentLength, int option) throws Exception {
        this.flag = flag;
        this.fileID = filename;
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;
        this.contentLength = contentLength;
        this.option = option;
        
        header = getHeader();       
        byte[] packet = new byte[HEADERSIZE];     
        byte[] header = getHeader();
        System.arraycopy(header, 0, packet, 0, HEADERSIZE);
        setPacket(packet);
    }

   
    /**
     * ARQ packet 
     * @param flag
     * @param filename
     * @param sequenceNumber
     * @param ackNumber
     * @param contentLength
     * @param option
     * @param fileContents
     * @throws Exception
     */
    public ARQPacket (int flag, int filename, int sequenceNumber, 
            int ackNumber, int contentLength, int option, byte[] fileContents) throws Exception {
        //setting header
        this.flag = flag;
        this.fileID = filename;
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;
        this.contentLength = contentLength;
        this.option = option;
        
        header = getHeader();
        
        data = new byte[fileContents.length];
        data = fileContents;
        
        byte[] buffer = new byte[HEADERSIZE + fileContents.length];

        System.arraycopy(header, 0, buffer, 0, HEADERSIZE);
        System.arraycopy(data, 0, buffer, header.length, data.length);
        setPacket(buffer);
    }
    
    

    
    /** 
     * Making an ARQ packet when receiving a Datagram. Datagram "uitpakken".
     * @param datagram
     */
    public ARQPacket(DatagramPacket datagram, InetAddress destination, int port) {
       
       byte[] dataReceivedPacket = datagram.getData();
       packet = new byte[dataReceivedPacket.length];
       
       flag = getFlags(datagram);
       fileID = getFileName(datagram);
       sequenceNumber = getSequenceNumber(datagram);
       ackNumber = getACKNumber(datagram);
       contentLength = getContentLength(datagram); 
       option = getOptions(datagram);
       
       data = new byte[dataReceivedPacket.length - header.length];
       
       System.arraycopy(dataReceivedPacket, header.length, data, 0, dataReceivedPacket.length - header.length);
       
       header = getHeader();
    
       System.arraycopy(header, 0, packet, 0, HEADERSIZE);
       System.arraycopy(data, 0, packet, header.length, data.length);
       
    
       address = destination;
       destinationPort = port;
       
    } 
    
    /**
     * Getting the whole name with the header.
     * @return
     */
    public byte[] getHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADERSIZE);
        
        // 1. Flag
        ByteBuffer flags =  ByteBuffer.allocate(4).putInt(flag);
        buffer.position(FLAG);
        buffer.put(flags.array());
        
        // 2. Name/Number file
        ByteBuffer fileName =  ByteBuffer.allocate(4).putInt(fileID);
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
    
    public int getFlags() {
        return flag;
    }
    
    /**
     * Get the FileName part of the DatagramPacket.
     * @param packet
     * @return
     */
    public int getFileName(DatagramPacket packet) {
        byte[] getName = Arrays.copyOfRange(packet.getData(), NAME_FILE, NAME_FILE + 4);
        int namefile = ByteBuffer.wrap(getName).getInt();
        return namefile;
    }
    
    public int getFileName() {
        return fileID;
    }
    
    /**
     * Get the SequenceNumber part of the DatagramPacket.
     * @param packet
     * @return
     */
    public int getSequenceNumber(DatagramPacket packet) {
        byte[] getSeqNr = Arrays.copyOfRange(packet.getData(), SEQUENCE_NR, SEQUENCE_NR + 4);
        int seqNr = ByteBuffer.wrap(getSeqNr).getInt();
        return seqNr;
    }
    
    public int getSequenceNumber() {
        return sequenceNumber;
    }
    
    /**
     * Get the AckNumber part of the DatagramPacket
     * @param packet
     * @return
     */
    public int getACKNumber(DatagramPacket packet) {
        byte[] getACK = Arrays.copyOfRange(packet.getData(), ACK_NUMBER, ACK_NUMBER + 4);
        int ackNr = ByteBuffer.wrap(getACK).getInt();
        return ackNr;
    }
    
    
    
    public int getACKNumber() {
       return ackNumber;
    }
     
    /**
     * Get the ContentLength of the DatagramPacket.
     * @return
     */
    public int getContentLength(DatagramPacket packet) {
        byte[] getLength = Arrays.copyOfRange(packet.getData(), CONTENT_LENGTH, CONTENT_LENGTH + 4);
        int length = ByteBuffer.wrap(getLength).getInt();
        return length;
    }
    
    public int getContentLength() {
       return contentLength;
    }
    
    /**
     * Get the OptionsField of the DatagramPacket.
     * @return
     */
    public int getOptions(DatagramPacket packet) {
        byte[] getOpt = Arrays.copyOfRange(packet.getData(), OPTION_POS, OPTION_POS + 4);
        int opt = ByteBuffer.wrap(getOpt).getInt();
        return opt;
    }
    
    public int getOptions() {
        return option;
    }
 
   /**
    * Get the data of the ARQ packet.
    * @return
    */
    public byte[] getPacket() {
        return packet;
    }
    
    public int getFlag() {
        return flag;
    }
    
    public byte[] getData() {
        return data;
    }
    
  
    public InetAddress getAddress() {
        return address;
    }

    public void setPacket(byte[] packet) {
        this.packet = packet;
    }
    

    public void setAddress(InetAddress address) {
        this.address = address;
    }



    public int getDestinationPort() {
        return destinationPort;
    }



    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }


    public void setFlags(int flag) {
        this.flag = flag;
    }
    
    public void setNameFile(int filename) {
        this.fileID = filename;
    }
    
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public void setACKNumber(int ackNumber) {
        this.ackNumber = ackNumber;
    }
    
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
    
    public void setOptions(int option) {
        this.option = option;
    }
    
    public void setData(String data) {
        this.data = data.getBytes();
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }


}
