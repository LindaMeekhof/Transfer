package general;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import client.ARQPacket;
import client.PacketHandlerServer;

public class DownloadManager {

    protected String filename;
 

    protected int amountPackets;
    private int fileSize;
    //map for all the packets received.
    protected int fileID;
    protected PacketHandlerServer packethandler;
    private boolean isDownloading;
    protected int startingSequenceNumber;
    protected BlockingQueue<ARQPacket> ACKPacketsReceived = new LinkedBlockingQueue<ARQPacket>();
    
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
        
    
}
