package general;

import client.ARQPacket;
import client.PacketHandler;

public class Directory implements Constants {
    
    
    private int expectedAck;
    private int receivedAck;
    private int expectedSequence;
    private ARQPacket lastSendPacket;
    private boolean notFinished;
    
    FileManager filemanager = new FileManager();
    private PacketHandler handler;
    
    /**
     * Constructor for requesting a directory
     * @param handler
     */
    public Directory(PacketHandler handler) {
        this.handler = handler;
        expectedAck = 0;
        expectedSequence = 0;
    }
    

    public void createFileListRequest(int sessionID) throws Exception {
           
        ARQPacket arq = new ARQPacket(FILELIST, sessionID, expectedSequence,
                EMPTY, EMPTY, EMPTY);  
        send(arq);
        System.out.println("filelist request send");
        lastSendPacket = arq; 
        expectedSequence++;
    }
    
    /**
     * Process Client side, the incoming directory and print.
     * @param packet
     * @throws Exception 
     */
    public void processListAck(ARQPacket packet) throws Exception {
        
        if(packet.getACKNumber() == expectedAck) {
            
            byte[] directory = packet.getData();
            
            String str = new String(directory);
            String[] names = str.split("_");
            
            for(String name : names) {
                System.out.println("File in directory:" + name);
            } 
            
            ARQPacket arq = new ARQPacket(ACKACK, packet.getFileID(), EMPTY,
                    packet.getSequenceNumber(), EMPTY, EMPTY);      
            send(arq);
            lastSendPacket = arq;
            expectedAck++;
        }
    }
    
    /**
     * Process Server side
     * @param packet
     * @throws Exception
     */
    public void processFileDirectory(ARQPacket packet) throws Exception {
        
        if (packet.getSequenceNumber() == expectedSequence) {
            
            //getting a string with all the names
            String directory = FileManager.getFileNamesToString();
            
            byte[] data = directory.getBytes();
             
            ARQPacket arq = new ARQPacket(LISTACK, packet.getFileID(), expectedSequence,
                    EMPTY, data.length, EMPTY, data); 
            send(arq);
            System.out.println("list ack send");
            lastSendPacket = arq; 
            expectedSequence++;
        }
    }
    

    /**
     * Process Server side. processing the ACK
     * @param packet
     */
    
    public void processACK(ARQPacket packet) {
        if (packet.getSequenceNumber() == expectedSequence) {
            handler.getDirectoryManager().remove(packet.getFileID());
            expectedSequence++;
            notFinished = false;
        }
    }

    /**
     * Sending the ARQ to the queue
     */
    public void send(ARQPacket packet) {
        handler.getClient().getPacketQueueOut().offer(packet);
        if (notFinished) {
        TimeOutDirectory timer = new TimeOutDirectory(this);
        timer.start();
        }
    }
    
    /**
     * Getters and setters.
     * @return
     */
    
    
    public int getExpectedAck() {
        return expectedAck;
    }

    public boolean isNotFinished() {
        return notFinished;
    }

    public void setNotFinished(boolean notFinished) {
        this.notFinished = notFinished;
    }

    public void setExpectedAck(int expectedAck) {
        this.expectedAck = expectedAck;
    }

    public int getReceivedAck() {
        return receivedAck;
    }

    public void setReceivedAck(int receivedAck) {
        this.receivedAck = receivedAck;
    }

    public int getExpectedSequence() {
        return expectedSequence;
    }

    public void setExpectedSequence(int expectedSequence) {
        this.expectedSequence = expectedSequence;
    }

    public FileManager getFilemanager() {
        return filemanager;
    }

    public void setFilemanager(FileManager filemanager) {
        this.filemanager = filemanager;
    }

    public PacketHandler getHandler() {
        return handler;
    }

    public void setHandler(PacketHandler handler) {
        this.handler = handler;
    }

    public ARQPacket getLastSendPacket() {
        return lastSendPacket;
    }

    public void setLastSendPacket(ARQPacket lastSendPacket) {
        this.lastSendPacket = lastSendPacket;
    }

    
    
}
