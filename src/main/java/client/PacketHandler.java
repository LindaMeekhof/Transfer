package client;

import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import general.DownloadManager;
import general.FileManager;

public class PacketHandler implements Runnable, Constants {
    
    
    private ClientPi client;
    private boolean running;

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
                            System.out.println("The file exists" + filename);

                            DownloadManager downloadManager = new DownloadManager(filename, this);

                            try {
                               // System.out.println(filename);
                                downloadManager.procesFileRequestCreateMETApacket(packet);
                            } catch (Exception e) {
                                System.out.println("META data failure");
                                e.printStackTrace();
                            }    
                        } else {
                            System.out.println("The file does not exists" +  filename);

                            DownloadManager downloadManager = new DownloadManager(filename, this);

                            try {
                                 //iets anders sturen
                                downloadManager.procesFileRequestCreateMETApacket(packet);
                            } catch (Exception e) {
                                System.out.println("META data failure");
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

                        //place downloadManager with fileID in map
                        DownloadManager downloader = new DownloadManager(str, this);

                        try {
                            downloader.processMETAPacket(packet);
                        } catch (Exception e) {
                            System.out.println("Something wrong withh process META packet");
                            e.printStackTrace();
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
                            downloadMetaAck.processMetaAckPacketCreateContent(packet);
                        } catch (Exception e2) {
                            // TODO Auto-generated catch block
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
                        
                        DownloadManager ackDownloader = client.getIdToDownloadManager().get(packet.getFileID());
                        
                        try {
                            ackDownloader.createAcknowledgementMessageProcesContent(packet);
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
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
                            ackDownload.processACKcreateContent(packet);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
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

 
    
    
    // create different kind of packets *******************************************
    /**
     * Create a FILE_REQUEST message
     * @throws Exception 
     */   
    public boolean hasPackets() {
        return !client.packetQueueIn.isEmpty();
    }

    // create different kind of packets *******************************************
    /**
     * Create a FILE_REQUEST message de goede 
     * @throws Exception 
     */
    public void createFileRequestPacket(String filename) throws Exception {
       
        ARQPacket arq = new ARQPacket();
 
        //setflags
        arq.setFlags(FILE_REQUEST);
        arq.setSequenceNumber(0);  //TODO
        arq.setACKNumber(EMPTY); 
        arq.setContentLength(filename.getBytes().length);
        
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
        System.out.println("sending filerequest packet");

    }
    



    /**
     * Create finish message.
     * @param packet
     * @throws Exception
     */
    public void createFinishMessage(ARQPacket packet) throws Exception {
        ARQPacket arq = new ARQPacket(FIN, packet.getFileID(),
                EMPTY, EMPTY, EMPTY, EMPTY);
        send(arq);
    }
    
    /**
     * Create pause request
     * @throws Exception 
     */
    public void createPauseRequest(int fileID) throws Exception {
        ARQPacket arq = new ARQPacket(PAUSE, fileID,
                EMPTY, EMPTY, EMPTY, EMPTY);
        send(arq);
    }
    
    /**
     * Create pause request
     * @throws Exception 
     */
    public void createResume(String filename) throws Exception {
        int fileID = 11; //TODO
        ARQPacket arq = new ARQPacket(RESUME, fileID,
                EMPTY, EMPTY, EMPTY, EMPTY);
        send(arq);
    }
    
    /**
     * Create pause request
     * @throws Exception 
     */
    public void createUploadRequest(String filename) throws Exception {
        int fileID = 12; //TODO
        ARQPacket arq = new ARQPacket(UPLOAD, fileID,
                EMPTY, EMPTY, EMPTY, EMPTY);
        send(arq);
    }
    
    /**
     * Create pause request
     * @throws Exception 
     */
    public void createFileListRequest() throws Exception {
        int fileID = 12; //TODO
        ARQPacket arq = new ARQPacket(FILELIST, fileID,
                EMPTY, EMPTY, EMPTY, EMPTY);
        send(arq);
    }
  
    
    /**
     * Sending the ARQ to the queue
     */
    public void send(ARQPacket packet) {
        client.packetQueueOut.offer(packet);
    }
 
    /**
     *  
     * @param timeout
     * @return
     */
     
    public ARQPacket dequeuePacket(long timeout) {
        ARQPacket box = null;
        
        try {
            box = client.packetQueueIn.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) { 
            System.out.println("Something went wrong with ARQPacket dequeue");
        }
        return box;
        
        
    }
    
    /**
     * Create a datagram with data
     * @param arq
     * @param IPAddress
     * @param destinationPort
     * @return
     */
    public static DatagramPacket createDatagram(ARQPacket arq, 
            InetAddress IPAddress, int destinationPort) {
        byte[] data = arq.getPacket();
        System.out.println(data);
        DatagramPacket datagram = new DatagramPacket(data, data.length, 
                IPAddress, destinationPort);
        return datagram;
    }

    
    // Getters and setters
    
    
    public ClientPi getClient() {
        return client;
    }


    public void setClient(ClientPi client) {
        this.client = client;
    }

    

    // Main **************************
    /**
     * Main to test.
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        ClientPi pi = new ClientPi();
        PacketHandler handler = new PacketHandler(pi);
        handler.createFileRequestPacket("test");
        
     
    }
        

}
