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
                        System.out.println("Ready with sending this file");
                        System.out.println("RECEIVED META ");
                        System.out.println("FLAG " + packet.getFlag());
                        System.out.println("FILE_ID " + packet.getFileName());
                        System.out.println("SEQ NR " + packet.getSequenceNumber());
                        System.out.println("ACK NR " + packet.getACKNumber());
                        System.out.println("CONTENT_LENGTH " + packet.getContentLength());
                        System.out.println("OPTION " + packet.getOptions());
                        System.out.println("GET DATA" + packet.getData());
                        System.out.println("");
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
                        System.out.println(filenames);
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
    
    public void createFileRequestPacket(String filename, InetAddress address) throws Exception {
       
        ARQPacket arq = new ARQPacket();
 
        //setflags
        arq.setFlags(FILE_REQUEST);
        arq.setSequenceNumber(2);  //TODO
        arq.setACKNumber(1); //TODO
        arq.setContentLength(filename.getBytes().length);
        
        //setData
        arq.setData(filename);    

        send(arq);
      
        //Datagram maken met ARQ Packet
        DatagramPacket datagram = createDatagram(arq, address, DESTINATIONPORT);
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
        
        //setData
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
        System.out.println(packet);

    }
    
    /**
     * create an Acknowledgement packet. Only header.
     * @param packet
     */
    public void createAcknowledgementMessage(ARQPacket packet) {
        ARQPacket arq = new ARQPacket();
        
        //setflags
        arq.setFlags(ACK);
        arq.setFileID(packet.getFileID());
        arq.setACKNumber(packet.getSequenceNumber() + 1);  
        arq.setContentLength(EMPTY); //only header
        
        send(arq);
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
    
    public void createFinishMessage() {
        
    }
    
    // handle/process different kind of packets *******************************************
//    /**
//     * Create META packet
//     * @throws Exception 
//     */
//    public void procesFileRequestCreateMETApacket(ARQPacket packet) throws Exception {
//        
//        //content METApacket
//        byte[] buffer = packet.getPacket();
//        String name = new String(buffer, "UTF=8");
//        byte[] content = getAmountOfPackets(name);
//        int contentLength = content.length;
//        
//        ARQPacket arq = new ARQPacket();
//        arq.setFlags(META);
//        arq.setNameFile(33); //TODO veranderen in een echt number
//        arq.setSequenceNumber(44); //TODO veranderen in een echt number
//        arq.setContentLength(contentLength);
//        
//        arq.setData(content);
//        send(arq);
//        
//        //misschien alleen een ARQpacket en die in de wachtrij zetten om datagram te creeren.
//        InetAddress raspberry = InetAddress.getByName("192.168.1.2");
//        DatagramPacket datagram = createDatagram(arq, raspberry, DESTINATIONPORT);  
//    }
    
    
    
    /**
     * Sending the ARQ to the queue
     */
    public void send(ARQPacket packet) {
        client.packetQueueOut.offer(packet);
    }
 
   /**
    * Return a byteArray for the amount of packets will be send for sending the file.
    * @param filename
    * @return
 * @throws Exception 
    */
//   public byte[] getAmountOfPackets(String filename) throws Exception {
//       
//       String path = Utils.getPathFromName(filename);
//       byte[] fileContents = FileManager.FileToByteArray(path);       
//       int amountPackets = fileContents.length/DATASIZE + 1;  
//       return Utils.intToBytes(amountPackets);
//   }
//   
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


   
    public void sendData(DatagramPacket data) {
        // TODO Auto-generated method stub
        
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
