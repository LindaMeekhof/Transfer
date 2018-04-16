package general;


import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import client.ARQPacket;
import client.Constants;
import client.PacketHandler;
import client.Utils;

public class DownloadManager implements Constants {

    protected String filename;
 

    protected int amountPackets;
    private int fileSize;
    //map for all the packets received.
    protected int fileID;
    protected PacketHandler packethandler;
    private boolean isDownloading;
    protected int startingSequenceNumber;
    protected BlockingQueue<ARQPacket> ACKPacketsReceived = new LinkedBlockingQueue<ARQPacket>();
    private int filePointer = 0;
    private int lastContentAck = 0;
    
    public int getLastContentAck() {
        return lastContentAck;
    }

    public void setLastContentAck(int lastContentAck) {
        this.lastContentAck = lastContentAck;
    }

    /**
     * Constructors
     * @param filename
     * @param amountPackets
     * @param fileSize
     */
    public DownloadManager (String filename, int amountPackets, int fileSize) {
        this.filename = filename;
        this.amountPackets = amountPackets;
        this.fileSize = fileSize;
       
        
    }
     
    public DownloadManager (String filename, PacketHandler packethandler) {
        this.filename = filename;
        this.packethandler = packethandler;
        
        createID();
        
        packethandler.getClient().getIdToDownloadManager().put(fileID, this);
        
        this.isDownloading = false;
        startingSequenceNumber = 0;
    }
    
    //*******************************************************
    /**
     * Getters and setters
     * @return
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    
    /**
     * CreateID for filename
     */
    private void createID() {
        
        int fileNumber;
        do {
               Random rand = new Random(); 
               int value = rand.nextInt(50); 
               fileNumber = value;
        } while (packethandler.getClient().getIdToDownloadManager().containsKey(fileNumber));
        
        this.fileID = fileNumber;
    }
        
    
    //*********************************************************
    /**
     * Create META packet
     * @throws Exception 
     */
    public void procesFileRequestCreateMETApacket(ARQPacket packet) throws Exception {
        
        //content METApacket
        byte[] buffer = packet.getData();
        String name = new String(buffer, "UTF-8");

        amountPackets = getAmountOfPacketsToInt(name);
        lastContentAck = amountPackets - 1;
        ByteBuffer totalBuffer = ByteBuffer.allocate(SIZE_INT_BYTE + buffer.length);
        
        ByteBuffer amountPacket = ByteBuffer.allocate(SIZE_INT_BYTE).putInt(amountPackets);
        totalBuffer.position(0);
        totalBuffer.put(amountPacket.array());
      
      //  ByteBuffer fileName = ByteBuffer.allocate(buffer.length);
       // byte[] theFileName = packet.getData();
        totalBuffer.position(4); //TODO begint bij 4 na de int
        totalBuffer.put(buffer);
        
        
        ARQPacket arq = new ARQPacket(META, fileID, startingSequenceNumber, 
                packet.getSequenceNumber(), 4 + packet.getData().length, EMPTY, totalBuffer.array());
        System.out.println(arq);
        
        arq.setAddress(packet.getAddress());
        arq.setDestinationPort(packet.getDestinationPort());
        
        System.out.println("process filerequest and send meta packet");
        packethandler.getClient().getPacketQueueOut().offer(arq);
    }
    
    /**
     * Process meta packet
     * @param packet
     * @throws Exception
     */
    public void processMETAPacket(ARQPacket packet) throws Exception {

        ARQPacket arq = new ARQPacket(META_ACK, packet.getFileID(), startingSequenceNumber + 1, 
                packet.getSequenceNumber(), EMPTY, EMPTY);
        
        arq.setAddress(packet.getAddress());
        arq.setDestinationPort((packet.getDestinationPort()));
        
        System.out.println("the META ack is send");
        packethandler.getClient().getPacketQueueOut().offer(arq);
    }
    
    /**
     * Process META_ACK packet, starting with download
     * @throws Exception 
     */
    public void processMetaAckPacketCreateContent(ARQPacket packet) throws Exception {
        //getting the file
        byte[] fileContents = FileManager.FileToByteArray(filename);
        
        int datalen = Math.min(DATASIZE, fileContents.length - filePointer);
        
        //create new ARQPacket
        byte[] pkt = new byte[datalen];
        System.arraycopy(fileContents, filePointer, pkt, 0, datalen);
        startingSequenceNumber = startingSequenceNumber + 1;
        ARQPacket arq = new ARQPacket(DOWNLOAD, fileID, startingSequenceNumber,
                EMPTY, DATASIZE, EMPTY, pkt);
        
        arq.setAddress(packet.getAddress());
        arq.setDestinationPort((packet.getDestinationPort()));
        
        System.out.println("the Content is send" + packet.getACKNumber());
        packethandler.getClient().getPacketQueueOut().offer(arq); 
    }
    
    /**
     * Process META_ACK packet, starting with download
     * @throws Exception 
     */
    public void processACKcreateContent(ARQPacket packet) throws Exception {
        
        if (packet.getACKNumber() == lastContentAck) {
            
            ARQPacket arq = new ARQPacket(FIN, fileID, startingSequenceNumber,
                    EMPTY, EMPTY, EMPTY);
          
            System.out.println("FIN message is send" + packet.getACKNumber());
            
            arq.setAddress(packet.getAddress());
            arq.setDestinationPort((packet.getDestinationPort()));
            
            packethandler.getClient().getPacketQueueOut().offer(arq); 
            
        } else {
        
        //getting the file
        byte[] fileContents = FileManager.FileToByteArray(filename);
        
        int datalen = Math.min(DATASIZE, fileContents.length - filePointer);
        
        //create new ARQPacket
        byte[] pkt = new byte[datalen];
        System.arraycopy(fileContents, filePointer, pkt, 0, datalen);
        startingSequenceNumber = startingSequenceNumber + 1;
        ARQPacket arq = new ARQPacket(DOWNLOAD, fileID, startingSequenceNumber,
                EMPTY, datalen, EMPTY, pkt);
        
        arq.setAddress(packet.getAddress());
        arq.setDestinationPort((packet.getDestinationPort()));
        
        System.out.println("the Content is send" + packet.getACKNumber());
        packethandler.getClient().getPacketQueueOut().offer(arq); 
        }
        
    }
    
    /**
     * Return a byteArray for the amount of packets will be send for sending the file.
     * @param filename
     * @return
  * @throws Exception 
     */
    public byte[] getAmountOfPackets(String filename) throws Exception {
        
        byte[] fileContents = FileManager.FileToByteArray(filename);       
        int amountPackets = fileContents.length/DATASIZE + 1;  //klopt dit??
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
        amountPackets = fileContents.length/DATASIZE + 1;  //klopt dit??
        return amountPackets;
    }

  
    private HashMap<Integer, byte[]> sequenceNumberContentMap = new HashMap<Integer, byte[]>();

    
    /**
     * create an Acknowledgement packet. Only header.
     * @param packet
     * @throws Exception 
     */
    public void createAcknowledgementMessageProcesContent(ARQPacket packet) throws Exception {
        //putting content in a map with sequencenumber
        byte[] sequenceNumberb = Arrays.copyOfRange(packet.getData(), 0, 4);
        int sequenceNumber = ByteBuffer.wrap(sequenceNumberb).getInt();
        System.out.println("Sequencenumber: " + sequenceNumber + "in map");

        sequenceNumberContentMap.put(sequenceNumber, packet.getData());
        
        //create an ACK
        int ackNumber = packet.getSequenceNumber() + 1;
        int fileID = packet.getFileID();
        ARQPacket arq = new ARQPacket(ACK, fileID, EMPTY, 
                ackNumber, EMPTY, EMPTY);

        System.out.println("ACK send fileID:" + fileID);
        
        arq.setAddress(packet.getAddress());
        arq.setDestinationPort((packet.getDestinationPort()));
        
        packethandler.getClient().getPacketQueueOut().offer(arq); 
    }

    
}
