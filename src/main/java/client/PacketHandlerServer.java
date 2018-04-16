package client;


import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.nedap.university.Raspberry;

import general.DownloadManager;
import general.FileManager;

public class PacketHandlerServer implements Runnable, Constants {
    
    public HashMap<String, Integer> mapFileNames = new HashMap<String, Integer>();
    public ArrayList<String> downLoading = new ArrayList<String>();
    public ArrayList<String> upLoading = new ArrayList<String>();
    public HashMap<Integer, DownloadManager> idToDownloadManager = new HashMap<Integer, DownloadManager>();
    private Boolean running = true;
    private Raspberry client;
    
  


    // Getters and setters *********************************
    
    public Raspberry getClient() {
        return client;
    }


    public void setClient(Raspberry client) {
        this.client = client;
    }
    
    public HashMap<Integer, DownloadManager> getIdToDownloadManager() {
        return idToDownloadManager;
    }


    public void setIdToDownloadManager(HashMap<Integer, DownloadManager> idToDownloadManager) {
        this.idToDownloadManager = idToDownloadManager;
    }
    
    public BlockingQueue<ARQPacket> getPacketQueueIn() {
        return client.packetQueueIn;
    }


    public void setPacketQueueIn(BlockingQueue<ARQPacket> packetQueueIn) {
        this.client.packetQueueIn = packetQueueIn;
    }


   /**
    * Constructor PacketHandler
    */
    public PacketHandlerServer(Raspberry client) {
        this.client = client;
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
                    case SYN :
                        System.out.println("Setup"); 
                        //TODO make method for handleSetup
                        break;
                    case ACK :
                        System.out.println("ACK packets for packet");
                        System.out.println("RECEIVED ACK ");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
                        break;
                    case SYN_ACK :
                     
                        System.out.println("RECEIVED DOORGAAN ");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
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
                    case META :
                        System.out.println("Ready with sending META file");
                        System.out.println("RECEIVED META ");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
                        
                        //get the downloadManager
                        DownloadManager current = idToDownloadManager.get(packet.getFileID());
                        try {
                            current.procesFileRequestCreateMETApacket(packet);
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                      
                    
                        break;
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
                        
                        ArrayList filenames = FileManager.getFileNames();
                        
                        
                        if (filenames.contains(filename.trim())) {
                            System.out.println("The file exists" + filename);

                            DownloadManager downloadManager = new DownloadManager(filename, this);

                            try {
                                System.out.println(filename);
                                downloadManager.procesFileRequestCreateMETApacket(packet);
                            } catch (Exception e) {
                                System.out.println("META data failure");
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("The file not exists" +  filename);

                            DownloadManager downloadManager = new DownloadManager(filename, this);

                            try {
                                downloadManager.procesFileRequestCreateMETApacket(packet);
                            } catch (Exception e) {
                                System.out.println("META data failure");
                                e.printStackTrace();
                            } 
                        }                      
                        try {
                            createAcknowledgementMessage(packet);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
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
         arq.setSequenceNumber(2);  //TODO
         arq.setACKNumber(1); //TODO
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
     * create an Acknowledgement packet. Only header.
     * @param packet
     * @throws Exception 
     */
    public void createAcknowledgementMessage(ARQPacket packet) throws Exception {
        
        int ackNumber = packet.getSequenceNumber() + 1;
        int fileID = packet.getFileID();
        ARQPacket arq = new ARQPacket(ACK, fileID, EMPTY, 
                ackNumber, EMPTY, EMPTY);
 
        send(arq);
        System.out.println(arq);
    }
    
    /**
     * ContinueDownloading. Gevolgd op een ACK.
     * @param data
     * @param packet
     */
    public void continueDownloadingDatagramPacket(byte[] data, ARQPacket packet) {
        ARQPacket arq = new ARQPacket();
        
        //header
        arq.setFlags(SYN_ACK);
        arq.setNameFile(packet.getFileName());
        arq.setSequenceNumber(11);
        
        //data
        arq.setData(data);
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
    
    public ArrayList<String> getDownLoading() {
        return downLoading;
    }


    public void setDownLoading(ArrayList<String> downLoading) {
        this.downLoading = downLoading;
    }


    public ArrayList<String> getUpLoading() {
        return upLoading;
    }


    public void setUpLoading(ArrayList<String> upLoading) {
        this.upLoading = upLoading;
    }


    /**
     * Create pause request
     * @throws Exception 
     */
    public void createResume(String filename) throws Exception {
        int fileID = 12; //TODO
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


   

    public void handleFileDownload(ARQPacket packet) {
        // TODO Auto-generated method stub
        
    }

    
    public void handleFileUpload(ARQPacket packet) {
        // TODO Auto-generated method stub
        
    }

  
    public void handleFinish(ARQPacket packet) {
        // TODO Auto-generated method stub
        
    }

   
    public void handleMetaData(ARQPacket packet) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        
    }


    public ARQPacket receivePacket() {
        // TODO Auto-generated method stub
        return null;
    }


  

   
        

}
