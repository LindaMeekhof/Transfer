package client;

import java.awt.List;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.nedap.university.Raspberry;

import general.FileManager;

public class PacketHandler implements Runnable, Constants {
    
    public HashMap<String, Integer> mapFileNames = new HashMap<String, Integer>();
    public ArrayList<String> downLoading = new ArrayList<String>();
    public ArrayList<String> upLoading = new ArrayList<String>();

    private Boolean running = true;
    private Raspberry client;
    private ClientPi clientPI;

   /**
    * Constructor PacketHandler
    */
    public PacketHandler(Raspberry client) {
        this.client = client;
    }
    
    public PacketHandler(ClientPi client) {
        this.clientPI = client;
    }

    /**
     * Getters and setters
     * @return
     */
    public BlockingQueue<ARQPacket> getPacketQueueIn() {
        return client.packetQueueIn;
    }


    public void setPacketQueueIn(BlockingQueue<ARQPacket> packetQueueIn) {
        this.clientPI.packetQueueIn = packetQueueIn;
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
                        // TODO handle ACK
                        break;
                    case SYN_ACK :
                        System.out.println("SYN_ACK ga door met verzenden");
                        break;
                    case FIN :
                        System.out.println("Ready with sending this file");
                        //TODO handle fin, checking if file integrity
                        break;
                    case FILE_REQUEST :
                        System.out.println("RECEIVED FILEREQUEST ");
                        //TODO handle FI
                        break;
                    default :
                        System.out.println("Invalid flag, drop the package");
                    }
                }
            }
        }
    }

    public boolean hasPackets() {
        return !clientPI.packetQueueIn.isEmpty();
    }
    
    
    
    // create different kind of packets *******************************************
    /**
     * Create a FILE_REQUEST message
     * @throws Exception 
     */
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
    
    public void createAcknowledgementMessage(ARQPacket packet) {
        ARQPacket arq = new ARQPacket();
        
        //setflags
        arq.setFlags(ACK);
        arq.setSequenceNumber(2);  //TODO

        arq.setACKNumber(packet.getSequenceNumber()); //TODO

        send(arq);
      
//        //Datagram maken met ARQ Packet
//        DatagramPacket datagram = createDatagram(arq, address, DESTINATIONPORT);
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
    /**
     * Create META packet
     * @throws Exception 
     */
    public void procesFileRequestCreateMETApacket(ARQPacket packet) throws Exception {
        
        //content METApacket
        byte[] buffer = packet.getPacket();
        String name = new String(buffer, "UTF=8");
        byte[] content = getAmountOfPackets(name);
        int contentLength = content.length;
        
        ARQPacket arq = new ARQPacket();
        arq.setFlags(META);
        arq.setNameFile(33); //TODO veranderen in een echt number
        arq.setSequenceNumber(44); //TODO veranderen in een echt number
        arq.setContentLength(contentLength);
        
        arq.setData(content);
        send(arq);
        
        //misschien alleen een ARQpacket en die in de wachtrij zetten om datagram te creeren.
        InetAddress raspberry = InetAddress.getByName("192.168.1.2");
        DatagramPacket datagram = createDatagram(arq, raspberry, DESTINATIONPORT);  
    }
    
    
    
    /**
     * Sending the ARQ to the queue
     */
    public void send(ARQPacket packet) {
        clientPI.packetQueueOut.offer(packet);
    }
 
   /**
    * Return a byteArray for the amount of packets will be send for sending the file.
    * @param filename
    * @return
 * @throws Exception 
    */
   public byte[] getAmountOfPackets(String filename) throws Exception {
       
       String path = Utils.getPathFromName(filename);
       byte[] fileContents = FileManager.FileToByteArray(path);       
       int amountPackets = fileContents.length/DATASIZE + 1;  
       return Utils.intToBytes(amountPackets);
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
