package client;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import general.FileManager;

public class PacketHandler implements Runnable, Constants {
    
    public HashMap<String, Integer> mapFileNames = new HashMap<String, Integer>();

    private Boolean running = true;
    
    private BlockingQueue<ARQPacket> packetQueueIn;
    private BlockingQueue<ARQPacket> packetQueueOut;
    
   /**
    * Construcotr PacketHandler
    */
    public PacketHandler() {
        packetQueueIn = new LinkedBlockingQueue<ARQPacket>();
        packetQueueOut = new LinkedBlockingQueue<ARQPacket>();
    }
    
    /**
     * Getters and setters
     * @return
     */
    public BlockingQueue<ARQPacket> getPacketQueueIn() {
        return packetQueueIn;
    }


    public void setPacketQueueIn(BlockingQueue<ARQPacket> packetQueueIn) {
        this.packetQueueIn = packetQueueIn;
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

             

                ARQPacket packet = packetQueueIn.poll();

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
                        System.out.println("FILEREQUEST ");
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
        return packetQueueIn.isEmpty();
    }
    
   
//    /**
//     * Create a FILE_REQUEST message
//     * @throws Exception 
//     */
//    public DatagramPacket createFileRequestPacket(String filename) throws Exception {
//       
//      
//         //filename
//        byte[] content = filename.getBytes();
//        
//        //flags
//        int flag = FILE_REQUEST;
//        int fileID = 0; //leeg pas vullen na eerste pakketje server
//        int seqNr = Utils.createRandomNumberForFile();
//        int ackNr = 0; //leeg 
//        int contentLength = content.length;
//        int option = 0;
//             
//        ARQPacket fileReq = new ARQPacket(flag, fileID, seqNr, ackNr, contentLength, option, content);
//       
//        //Datagram maken met ARQ Packet
//        InetAddress raspberry = InetAddress.getByName("192.168.1.1");
//        DatagramPacket datagram = createDatagram(fileReq, raspberry, DESTINATIONPORT);
//        
//        return datagram;
//    }
       
    /**
     * Create a FILE_REQUEST message
     * @throws Exception 
     */
    public DatagramPacket createFileRequestPacket(String filename, InetAddress address) throws Exception {
       
        ARQPacket arq = new ARQPacket();
 
        //setflags
        arq.setFlags(FILE_REQUEST);
        arq.setSequenceNumber(2);  //TODO
        arq.setACKNumber(1); //TODO
        arq.setContentLength(filename.getBytes().length);
        
        //setData
        arq.setData(filename);    

        //Datagram maken met ARQ Packet
        DatagramPacket datagram = createDatagram(arq, address, DESTINATIONPORT);
        
        return datagram;
    }

    
//    /**
//     * Method for handling the fileRequest before sending the data back. 
//     * @throws Exception 
//     */
//    public void processFileRequesting(ARQPacket packet) throws Exception {
//        
//        //get name of file
//        byte[] buffer = packet.getPacket();
//        String name = new String(buffer, "UTF=8");
//        System.out.println("Process filerequest" + name);
//        
//        //check if the file exist
//        
//        //send a Datagram as feedback with amountOfPackets
//            //amountOfPackets.
//        byte[] content = getAmountOfPackets(name);
//        
//            //flags
//        int flag = META;
//        int fileID = Utils.createRandomNumberForFile(); 
//            //opslaan van de fileID and name
//        mapFileNames.put(name, fileID);
//        int seqNr = Utils.createRandomNumberForFile();
//        int ackNr = 0; //leeg 
//        int contentLength = content.length;
//        int option = 0;
//        
//        ARQPacket fileReq = new ARQPacket(flag, fileID, seqNr, ackNr, contentLength, option, content);
//            //Datagram maken met ARQ Packet 
//        InetAddress raspberry = InetAddress.getByName("192.168.1.2");
//        DatagramPacket datagram = createDatagram(fileReq, raspberry, DESTINATIONPORT);
//        
//        //sending the datagram.
//    }
   
    /**
     * Create META packet
     * @throws Exception 
     */
    public void createMETApacket(ARQPacket packet) throws Exception {
        
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
        
        
        //misschien alleen een ARQpacket en die in de wachtrij zetten om datagram te creeren.
        InetAddress raspberry = InetAddress.getByName("192.168.1.2");
        DatagramPacket datagram = createDatagram(arq, raspberry, DESTINATIONPORT);  
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
       
       return null;
   }
   
   
    /**
     *  
     * @param timeout
     * @return
     */
     
    public ARQPacket dequeuePacket(long timeout) {
        ARQPacket box = null;
        
        try {
            box = packetQueueIn.poll(timeout, TimeUnit.MILLISECONDS);
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

    //hier gebleven
   
    public void sendData(DatagramPacket data) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Getting a META data packet and sending MetaData
     * @throws UnsupportedEncodingException 
     */
   
    public void sendMetaData(ARQPacket packet) throws UnsupportedEncodingException {
       
        
        //setup space for setting the file
        
       //getting a download assistant
        
       //send an ack for receiving the META packet.
       
        
        
//        //get name of file
//        byte[] buffer = packet.getPacket();
//        String name = new String(buffer, "UTF=8");
//        
//        //check if the file exist
//        
//        //send a Datagram as feedback with amountOfPackets
//            //amountOfPackets.
//        byte[] content = getAmountOfPackets(name);
//        
//            //flags
//        int flag = ACK;
//        int fileID = Utils.createRandomNumberForFile(); 
//            //opslaan van de fileID and name
//        mapFileNames.put(name, fileID);
//        int seqNr = Utils.createRandomNumberForFile();
//        int ackNr = 0; //leeg 
//        int contentLength = content.length;
//        int option = 0;
//        
//        ARQPacket fileReq = new ARQPacket(flag, fileID, seqNr, ackNr, contentLength, option, content);
//            //Datagram maken met ARQ Packet 
//        InetAddress raspberry = InetAddress.getByName("192.168.1.2");
//        DatagramPacket datagram = createDatagram(fileReq, raspberry, DESTINATIONPORT);
//        
        //sending the datagram.
        
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

   
        

}
