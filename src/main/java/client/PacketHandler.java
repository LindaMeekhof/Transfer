package client;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import general.Constants;
import general.DownloadManager;
import general.FileManager;
import general.Receiver;

public class PacketHandler implements Runnable, Constants {
    
    
    private ClientPi client;
    private boolean running;
    int sessionID;
    private Map<Integer, Receiver> connections = new HashMap<Integer, Receiver>();

    public PacketHandler(ClientPi client) {
        this.client = client;
        running = true;
    }

    
    private ARQPacket lastSendPacket;

    // Other methods ***************************************************************************
    
    /**
     * Handle incoming packets. 
     */
    public void run() {
           System.out.println("PacketHandling");
        
        while (running) {  
            while (hasPackets()) {
                //Get packet and process on flag 

                ARQPacket packet = client.packetQueueIn.poll();

                if (packet != null) {
                    int flag = packet.getFlag();

                    switch(flag) {
                    case FILE_REQUEST :
                        System.out.println("RECEIVED FILEREQUEST ");
                        
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());

                        String filename = new String(packet.getData());
                        System.out.println("GET DATA " + filename);
                        
                        ArrayList<String> filenames = FileManager.getFileNames();
                        System.out.println("the list of filenames" + filenames);

                        if (filenames.contains(filename.trim())) {
                           
                            DownloadManager downloadManager = new DownloadManager(filename, this, packet.getAddress(), 
                                    packet.getDestinationPort(), client, packet.getFileID());
                            try {
                                downloadManager.procesFileRequestCreateMETApacket(packet);
                            } catch (Exception e) {
                                System.out.println("processing file_request error");
                                e.printStackTrace();
                            }    
                        }

                        break;
                    case META :
                        System.out.println("RECEIVED META");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        
                        //amount of packets
                        byte[] amount = Arrays.copyOfRange(packet.getData(), 0, 4);
                        int amountOfPackets = ByteBuffer.wrap(amount).getInt();
                        System.out.println("Amount of packets send" + amountOfPackets);
                        
                        //filename
                        int length = packet.getData().length - 4;
                        byte[] name = Arrays.copyOfRange(packet.getData(), 4, length);
                        String str = new String(name);
                        System.out.println("the filename send with META :" + str);

                        //Get the receiver with fileID
                        Receiver receiver = connections.get(packet.getFileID());
                        System.out.println("fileID" + packet.getFileID());
                        
              
                        
                        try {
                            receiver.processMETAPacket(packet);
                        } catch (Exception e3) {
                            System.out.println("processing meta error");
                            e3.printStackTrace();
                        }
         
                        break;
                    case META_ACK :
                        System.out.println("RECEIVED a META_ACK");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        
                        System.out.println("META_ACK is ontvangen, FILE ID: " + packet.getFileID() );
                    
                        DownloadManager downloadMetaAck = client.getIdToDownloadManager().get(packet.getFileID());
                                           
                        try {
                          
                            downloadMetaAck.setDownloading(true);
                            downloadMetaAck.processAckCreateContentPacket(packet);

                        } catch (Exception e2) {
                            System.out.println("processing meta_ack error");
                            e2.printStackTrace();
                        }
                        
                        break;
                        
                    case DOWNLOAD :
                        System.out.println("RECEIVED DOWNLOAD with content");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
                        
                        String content = new String(packet.getData());
                        System.out.println("GET DATA packet " + content);
                        
                        
                        System.out.println(client.getIdToDownloadManager());
                        
                        //Get the receiver with fileID
                        Receiver rec = connections.get(packet.getFileID());
                       
                        try {
                            rec.processReceivingContent(packet);
                        } catch (Exception e3) {
                            System.out.println("processing download error");
                            e3.printStackTrace();
                        }

                        break;
                    case ACK :
                        System.out.println("ACK packets packetcontent arrived");
                        System.out.println("RECEIVED ACK ");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
                        
                        
                        DownloadManager ackDownload = client.getIdToDownloadManager().get(packet.getFileID());
    
                        try {
                          ackDownload.processAckCreateContentPacket(packet);
                        } catch (Exception e) {
                            System.out.println("processsing ack error");
                            e.printStackTrace();
                        }
                        
                        break;
                    case FIN :
                        System.out.println("Ready with sending this file");
                        System.out.println("RECEIVED FIN ");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
                        
                        //Get the receiver with fileID
                        Receiver finReceiver = connections.get(packet.getFileID());
                       
                        try {
                            finReceiver.processReceivingContent(packet);
                        } catch (Exception e3) {
                            System.out.println("processing fin error");
                            e3.printStackTrace();
                        }
                        
                        break;
            
                    case FIN_ACK :
                        System.out.println("Ready with sending this file");
                        System.out.println("RECEIVED FIN ");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
                        
                        //Get the receiver with fileID
                        Receiver finack = connections.get(packet.getFileID());
                       
                        try {
                            finack.processReceivingContent(packet);
                        } catch (Exception e3) {
                            System.out.println("processing fin error");
                            e3.printStackTrace();
                        }
                        
                        break;
            
                    default :
                        System.out.println("Invalid flag, drop the package");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
                        
                    }
                }
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

 
    
    
  
    public boolean hasPackets() {
        return !client.packetQueueIn.isEmpty();
    }

    // create different kind of packets *******************************************
    /**
     * Create a FILE_REQUEST
     * @throws Exception 
     */
    public void connectionAndSendRequest(String filename) throws Exception {
        //Put the receiver in the map
        Receiver receiver = new Receiver(this);
        createIDforReceiver();
        connections.put(sessionID, receiver);
        
        receiver.createFileRequestPacket(filename, sessionID);
    }
    
    


    public void createIDforReceiver() {
        sessionID++;    
    }
    
    

    
    // Getters and setters

    public boolean isRunning() {
        return running;
    }





    public void setRunning(boolean running) {
        this.running = running;
    }





    public int getSessionID() {
        return sessionID;
    }





    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }





    public Map<Integer, Receiver> getConnections() {
        return connections;
    }





    public void setConnections(Map<Integer, Receiver> connections) {
        this.connections = connections;
    }





    public ClientPi getClient() {
        return client;
    }

    public void setClient(ClientPi client) {
        this.client = client;
    }


    public ARQPacket getLastSendPacket() {
        return lastSendPacket;
    }


    public void setLastSendPacket(ARQPacket lastSendPacket) {
        this.lastSendPacket = lastSendPacket;
    }
    
 

}
