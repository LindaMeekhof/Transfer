package general;


import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import client.ARQPacket;
import client.ClientPi;
import client.PacketHandler;
import client.Utils;

public class DownloadManager extends Thread implements Constants{

    protected String filename;
    
    private HashMap<Integer, byte[]> sequenceNumberContentMap = new HashMap<Integer, byte[]>();
    protected int amountPackets;
    protected int fileID;
    protected PacketHandler packethandler;
    protected boolean isDownloading;
    protected int startingSequenceNumber;
    private InetAddress address;
    private int port;
    private ClientPi client;
    private int filePointer;
    private ARQPacket lastSendPacket;
    private int expectedACK;
    private int ackReceived;

 
    public DownloadManager (String filename, PacketHandler packethandler, InetAddress address, int port, ClientPi client, int fileID) {
        this.filename = filename;
        this.packethandler = packethandler;
        this.client = client;
        this.fileID = fileID;
             
        packethandler.getClient().getIdToDownloadManager().put(fileID, this);
        
        this.isDownloading = false;
        startingSequenceNumber = 0;
        
        this.address = address;
        this.port = port;
        this.start();
        filePointer = 0;
    }
    
    public DownloadManager (String filename, PacketHandler packethandler, int fileIdentity, InetAddress address, int port, ClientPi client, int fileID) {
        this.filename = filename;
        this.fileID = fileID;
        this.packethandler = packethandler;
        
        packethandler.getClient().getIdToDownloadManager().put(this.fileID, this);
        
        this.isDownloading = false;
        startingSequenceNumber = 0;
        this.port = port;
        this.address = address;
        
        this.client = client;
        this.start();
        filePointer = 0;
    }
     
    //*********************************************************
    /**
     * Create META packet
     * @throws Exception 
     */
    public void procesFileRequestCreateMETApacket(ARQPacket packet) throws Exception {
        
        if (startingSequenceNumber == packet.getSequenceNumber()) {
        expectedACK = startingSequenceNumber; // = 0
     
        byte[] buffer = packet.getData();
        String name = new String(buffer, "UTF-8");

        amountPackets = getAmountOfPacketsToInt(name);
             
        ByteBuffer totalBuffer = ByteBuffer.allocate(SIZE_INT_BYTE + buffer.length);  
        ByteBuffer amountPacket = ByteBuffer.allocate(SIZE_INT_BYTE).putInt(amountPackets);
        totalBuffer.position(0);
        totalBuffer.put(amountPacket.array());
        totalBuffer.position(4); 
        totalBuffer.put(buffer);
         
        ARQPacket arq = new ARQPacket(META, fileID, startingSequenceNumber, 
                packet.getSequenceNumber(), 4 + packet.getData().length, EMPTY, totalBuffer.array());
        System.out.println(arq);
        
        arq.setAddress(packet.getAddress());
        arq.setDestinationPort(packet.getDestinationPort());
            
        System.out.println("process filerequest and send meta packet");
        send(arq);
        lastSendPacket = arq;
        startingSequenceNumber++;
        } 
    }
    
    


    /**
     * Process META_ACK packet, starting with download
     * @throws Exception 
     */
    public void processAckCreateContentPacket(ARQPacket packet) throws Exception {
        System.out.println("process content expected ack should be 0 and is" + expectedACK);

        if (packet.getACKNumber() == expectedACK) { 

            System.out.println(packet.getACKNumber() == expectedACK);
            //getting the file
            byte[] fileContents = FileManager.FileToByteArray(filename);

            if (filePointer <= fileContents.length) { 

                int datalen = Math.min(DATASIZE, fileContents.length - filePointer);

                byte[] pkt = new byte[datalen];
                System.arraycopy(fileContents, filePointer, pkt, 0, datalen);

                ARQPacket arq = new ARQPacket(DOWNLOAD, fileID, startingSequenceNumber,
                        EMPTY, datalen, EMPTY, pkt);
                arq.setAddress(address);
                arq.setDestinationPort(port);

                arq.setAddress(packet.getAddress());
                arq.setDestinationPort((packet.getDestinationPort()));

                System.out.println("the Content is send" + packet.getACKNumber());
                send(arq);
                this.lastSendPacket = arq;

                filePointer = filePointer + datalen;

                expectedACK++;
                startingSequenceNumber++;
            } else {
           
            sendFIN();
            
            }
        }


    }

    public void sendFIN() throws Exception {

        int seqNumber = amountPackets + 1;
        ARQPacket arq = new ARQPacket(FIN, fileID, seqNumber,
                EMPTY, EMPTY, EMPTY);

        arq.setAddress(address);
        arq.setDestinationPort(port);
        expectedACK = seqNumber;

        send(arq); 
        lastSendPacket = arq;
    }
  
  /**
   * Process fin_ack
   */
  public void processFinAck() {
      System.out.println("the donwload is ended");
      System.out.println("startingSequenceNumber: " + startingSequenceNumber);
      
      packethandler.getConnections().remove(fileID);
  }
    
    

    /**
     * Create an ACK for download.
     * @param packet
     * @throws Exception 
     */
    public void createAcknowledgementMessageProcesContent(ARQPacket packet) throws Exception {
        //putting content in a map with sequencenumber
        //TODO??
        byte[] sequenceNumberb = Arrays.copyOfRange(packet.getData(), 0, 4);
        int sequenceNumber = ByteBuffer.wrap(sequenceNumberb).getInt();
        System.out.println("Sequencenumber: " + sequenceNumber + "in map");

        sequenceNumberContentMap.put(sequenceNumber, packet.getData());
        
        //create an ACK --> hier gebleven
        int ackNumber = packet.getSequenceNumber();
        int fileID = packet.getFileID();
        ARQPacket arq = new ARQPacket(ACK, fileID, EMPTY, 
                ackNumber, EMPTY, EMPTY);

        System.out.println("ACK send fileID:" + fileID);
        
        arq.setAddress(packet.getAddress());
        arq.setDestinationPort((packet.getDestinationPort()));
        
        send(arq);
        lastSendPacket = arq;
    }



    /**
     * Sending packets
     * @param arq
     */
    public void send(ARQPacket packet) {
        packethandler.getClient().getPacketQueueOut().offer(packet);
        TimeOutDownload timer = new TimeOutDownload(this);
        timer.start();
    }
    
    /**
     * Return a byteArray for the amount of packets will be send for sending the file.
     * @param filename
     * @return
  * @throws Exception 
     */
    public byte[] getAmountOfPackets(String filename) throws Exception {
        
        byte[] fileContents = FileManager.FileToByteArray(filename);       
        int amountPackets = fileContents.length/DATASIZE + 1;  
        return Utils.intToBytes(amountPackets);
    }
   
    
    /**
     * Return a byteArray for the amount of packets will be send for sending the file.
     * @param filename
     * @return
  * @throws Exception 
  */
    public int getAmountOfPacketsToInt(String filename) throws Exception {

        byte[] fileContents = FileManager.FileToByteArray(filename);       
        amountPackets = fileContents.length/DATASIZE + 1; 
        return amountPackets;
    }


    /**
     * Getters and setters.
     * @return
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public HashMap<Integer, byte[]> getSequenceNumberContentMap() {
        return sequenceNumberContentMap;
    }

    public void setSequenceNumberContentMap(HashMap<Integer, byte[]> sequenceNumberContentMap) {
        this.sequenceNumberContentMap = sequenceNumberContentMap;
    }

    public int getAmountPackets() {
        return amountPackets;
    }

    public void setAmountPackets(int amountPackets) {
        this.amountPackets = amountPackets;
    }

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public PacketHandler getPackethandler() {
        return packethandler;
    }

    public void setPackethandler(PacketHandler packethandler) {
        this.packethandler = packethandler;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }

    public int getStartingSequenceNumber() {
        return startingSequenceNumber;
    }

    public void setStartingSequenceNumber(int startingSequenceNumber) {
        this.startingSequenceNumber = startingSequenceNumber;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ClientPi getClient() {
        return client;
    }

    public void setClient(ClientPi client) {
        this.client = client;
    }

    public int getFilePointer() {
        return filePointer;
    }

    public void setFilePointer(int filePointer) {
        this.filePointer = filePointer;
    }

    public ARQPacket getLastSendPacket() {
        return lastSendPacket;
    }

    public void setLastSendPacket(ARQPacket lastSendPacket) {
        this.lastSendPacket = lastSendPacket;
    }

    public int getExpectedACK() {
        return expectedACK;
    }

    public void setExpectedACK(int expectedACK) {
        this.expectedACK = expectedACK;
    }

    public int getAckReceived() {
        return ackReceived;
    }

    public void setAckReceived(int ackReceived) {
        this.ackReceived = ackReceived;
    }

}

