package client;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import general.Constants;
import general.Directory;
import general.DownloadManager;
import general.FileManager;
import general.Receiver;

public class PacketHandler implements Runnable, Constants {
       
    private ClientPi client;
    private boolean running;
    private int sessionID;
    private Map<Integer, Receiver> connections = new HashMap<Integer, Receiver>();
    private Map<Integer, Directory> directoryManager = new HashMap<Integer, Directory>();
    private ARQPacket lastSendPacket;
    

    public PacketHandler(ClientPi client) {
        this.client = client;
        running = true;
    }


    // Other methods ***************************************************************************
    
    /**
     * Handle incoming packets. 
     */
    public void run() {
           System.out.println("PacketHandling");
        
        while (running) {  
            while (hasPackets()) {

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
                        
                        //Filename
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
                        String content = new String(packet.getData());
                        System.out.println("GET DATA packet " + content);

                        Receiver rec = connections.get(packet.getFileID());
                       
                        try {
                            rec.processReceivingContent(packet);
                        } catch (Exception e3) {
                            System.out.println("processing download error");
                            e3.printStackTrace();
                        }

                        break;
                    case ACK :
                        System.out.println("RECEIVED ACK ");
     
                        DownloadManager ackDownload = client.getIdToDownloadManager().get(packet.getFileID());
                        
                        //set the received ack in a set.
                        try {
                          ackDownload.processAckCreateContentPacket(packet);
                        } catch (Exception e) {
                            System.out.println("processsing ack error");
                            e.printStackTrace();
                        }
                        
                        break;
                    case FIN :
                        System.out.println("RECEIVED FIN ");

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
                       
                        System.out.println("RECEIVED FIN ACK");
                        
                        DownloadManager finack = client.getIdToDownloadManager().get(packet.getFileID());
                       
                        try {
                            finack.processFinAck();
                        } catch (Exception e3) {
                            System.out.println("processing fin error");
                            e3.printStackTrace();
                        }
                        break;
            
                    case FILELIST :
           
                        System.out.println("RECEIVED FILELIST ");

                        Directory directory = new Directory(this);
                        directoryManager.put(packet.getFileID(), directory);
                        
                        try {
                            directory.processFileDirectory(packet);
                        } catch (Exception e3) {
                            System.out.println("processing fin error");
                            e3.printStackTrace();
                        }
                        
                        break;
                    case ACKACK :
                        
                        System.out.println("RECEIVED FILELIST ");

                        
                        Directory dir = directoryManager.get(packet.getFileID());
                        
                        try {
                            dir.processACK(packet);
                        } catch (Exception e3) {
                            System.out.println("processing fin error");
                            e3.printStackTrace();
                        }
                        
                        break;
                    case LISTACK :
                        
                        System.out.println("RECEIVED FILELIST ");

                        Directory direct = new Directory(this);
                        directoryManager.put(packet.getFileID(), direct);
                        
                        try {
                            direct.processListAck(packet);
                        } catch (Exception e3) {
                            System.out.println("processing fin error");
                            e3.printStackTrace();
                        }
                        
                        break;
                     
                    default :
                        System.out.println("Invalid flag, drop the package");
                    }
                }
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("interupted while sleeping");
                e.printStackTrace();
            }
        }
    }


    public boolean hasPackets() {
        return !client.packetQueueIn.isEmpty();
    }


    /**
     * Create a FILE_REQUEST
     * @throws Exception 
     */
    public void connectionAndSendRequest(String filename) throws Exception {
        //Put the receiver in the map
        Receiver receiver = new Receiver(this, filename);
        createID();
        connections.put(sessionID, receiver);
        
        receiver.createFileRequestPacket(filename, sessionID);
    }
    
    /**
     * FileDirectory request.
     * @throws Exception 
     */
    public void createFileListRequest() throws Exception {
        Directory directory = new Directory(this);
        createID();
        directoryManager.put(sessionID, directory); 
        
        directory.createFileListRequest(sessionID);
        
    }

    public void createID() {
        sessionID++;    
    }
    

    /**
     * Getters and setters.
     * @return
     */
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
    
    public Map<Integer, Directory> getDirectoryManager() {
        return directoryManager;
    }

    public void setDirectoryManager(Map<Integer, Directory> directoryManager) {
        this.directoryManager = directoryManager;
    }


}
