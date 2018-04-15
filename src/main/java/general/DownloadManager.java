package general;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import client.ARQPacket;
import client.Constants;
import client.PacketHandler;
import client.PacketHandlerServer;
import client.Utils;

public class DownloadManager implements Constants {

    protected String filename;
 

    protected int amountPackets;
    private int fileSize;
    //map for all the packets received.
    protected int fileID;
    protected PacketHandlerServer packethandler;
    private boolean isDownloading;
    protected int startingSequenceNumber;
    protected BlockingQueue<ARQPacket> ACKPacketsReceived = new LinkedBlockingQueue<ARQPacket>();
    private PacketHandler packethandlerclient;
    
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
    
    public DownloadManager (String filename, PacketHandlerServer packethandler) {
        this.filename = filename;
        this.packethandler = packethandler;
        
        createID();
        
        packethandler.getIdToDownloadManager().put(fileID, this);
        
        this.isDownloading = false;
        startingSequenceNumber = 0;
    }
    
    
    public DownloadManager (String filename, PacketHandler packethandler) {
        this.filename = filename;
        this.packethandlerclient = packethandler;
        
        createID();
        
        packethandler.getIdToDownloadManager().put(fileID, this);
        
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
        } while (packethandler.getIdToDownloadManager().containsKey(fileNumber));
        
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
        

        byte[] content = getAmountOfPackets(name);
        int contentLength = content.length;
        
        ARQPacket arq = new ARQPacket();
        arq.setFlags(META);
        arq.setNameFile(fileID); 
        System.out.println(startingSequenceNumber);
        arq.setSequenceNumber(startingSequenceNumber); 
        arq.setACKNumber(packet.getSequenceNumber());
        arq.setContentLength(content.length);
        
        arq.setData(content);
        
        System.out.println("process filerequest and send meta packet");
        //in de wachtrij gezet om te versturen
        packethandler.getClient().getPacketQueueOut().offer(arq);
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
    
}
