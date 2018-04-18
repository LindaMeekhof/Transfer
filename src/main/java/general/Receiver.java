package general;

import client.ARQPacket;
import client.PacketHandler;

public class Receiver implements Constants {

    int expectedAck;
    int receivedAck;
    int expectedSequence; 
    FileManager filemanager;
    
    PacketHandler packethandler;
    ARQPacket lastSendPacket;
    
    private int startingSequenceNumber;
    
    public Receiver() {
        startingSequenceNumber = 0;
        expectedSequence = 0;
        receivedAck = 0;
        filemanager = new FileManager();
    }
    
    /**
     * Process meta packet
     * @param packet
     * @throws Exception
     */
    public void processMETAPacket(ARQPacket packet) throws Exception {

        if (expectedSequence == packet.getSequenceNumber()) {
            System.out.println("received data with sequencenumber " + packet.getSequenceNumber());
 
        ARQPacket arq = new ARQPacket(META_ACK, packet.getFileID(), EMPTY, 
                packet.getSequenceNumber(), EMPTY, EMPTY);

        arq.setAddress(packet.getAddress());
        arq.setDestinationPort((packet.getDestinationPort()));
        
        System.out.println("the META ack is send");
        send(arq);
        lastSendPacket = arq; 
        expectedSequence = expectedSequence + 1;
        expectedAck++;
        }
    }
    
    public void processReceivingContent(ARQPacket packet) throws Exception {
    
        System.out.println("expected sequencenumber" + packet.getSequenceNumber());
        
        if (expectedSequence == packet.getSequenceNumber()) {
            System.out.println("received data with sequencenumber " + packet.getSequenceNumber());
            System.out.println("sending ack");
            ARQPacket arq = new ARQPacket(ACK, packet.getFileID(), EMPTY,
                    packet.getSequenceNumber(), EMPTY, EMPTY);
            
            arq.setAddress(packet.getAddress());
            arq.setDestinationPort((packet.getDestinationPort()));
                     
            send(arq);
            lastSendPacket = arq;
            expectedSequence++;
        }
    }
    
    public void processFIN(ARQPacket packet) throws Exception {
        
        if (expectedSequence == packet.getSequenceNumber()) {
            System.out.println("received FIN " + packet.getSequenceNumber());
            //check bestand op corruptie 
            //checksum geheel bestand.
            //option is 1 if corrupt
            //option is 2 if not corrupt
            
            
            ARQPacket arq = new ARQPacket(FIN_ACK, packet.getFileID(), startingSequenceNumber,
                    packet.getSequenceNumber(), EMPTY, EMPTY);
            //moet nog een ack op de fin_ack krijgen op afsluiten
        } //TODO
    }
    
    public Integer getReceivedAck() {
        return receivedAck;
    }

    public void setReceivedAck(Integer receivedAck) {
        this.receivedAck = receivedAck;
    }

    public PacketHandler getPackethandler() {
        return packethandler;
    }

    public void setPackethandler(PacketHandler packethandler) {
        this.packethandler = packethandler;
    }

    public ARQPacket getLastSendPacket() {
        return lastSendPacket;
    }

    public void setLastSendPacket(ARQPacket lastSendPacket) {
        this.lastSendPacket = lastSendPacket;
    }

    public Integer getExpectedAck() {
        return expectedAck;
    }

    public void setExpectedAck(Integer expectedAck) {
        this.expectedAck = expectedAck;
    }

    
    
    public Receiver(PacketHandler packethandler) {
        this.packethandler = packethandler;
    }
    
    
    /**
     * Sending the ARQ to the queue
     */
    public void send(ARQPacket packet) {
        packethandler.getClient().getPacketQueueOut().offer(packet);
        TimeOut timer = new TimeOut(this);
        timer.start();
    }
    
    /**
     * Create a FILE_REQUEST
     * @throws Exception 
     */
    public void createFileRequestPacket(String filename, int sessionID) throws Exception {
        
        ARQPacket arq = new ARQPacket();
 
        //setflags
        arq.setFlags(FILE_REQUEST);
        
        int sequenceNumber = 0;
        arq.setSequenceNumber(sequenceNumber); 
        expectedAck = sequenceNumber; 
        
        arq.setContentLength(filename.getBytes().length);
        arq.setFileID(sessionID);
        
        //setDatas
        byte[] data = filename.getBytes();
        arq.setData(data);    
        
        //bytebuffer for packet (header + data)
        int fileNameSize = filename.getBytes().length;
        byte[] packet = new byte[HEADERSIZE + fileNameSize];
        
        byte[] header = arq.getHeader();
        //First enter the header content
        System.arraycopy(header, 0, packet, 0, HEADERSIZE);
        
        //Secondly enter the data content
        System.arraycopy(data, 0, packet, header.length, data.length);
        arq.setPacket(packet);
             
        send(arq);
        this.lastSendPacket = arq;
        System.out.println("sending filerequest packet");
    }
 
}
