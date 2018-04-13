package client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PacketHandler implements Runnable {
    
    //Flags; waar is het pakketje voor
    private static final int SYN = 1;         //setup, for making a storage for the file that will be send
    private static byte[] SYN_B = new byte[] {0b00000000, 0b00000000, 0b00000000, 0b00000001};
    private static final int ACK = 2;         //ack of the send data.
    private static final int SYN_ACK = 3;     //doorgaan met zenden
    private static final int FIN = 4;         //finished with the file
    private static final int FILE_REQUEST = 8; //fileRequest 

    private Boolean running = true;
    
    private BlockingQueue<ARQPacket> packetQueueIn;

    public PacketHandler() {
        packetQueueIn = new LinkedBlockingQueue<ARQPacket>();
    }
    
    // Getters and setters
    public BlockingQueue<ARQPacket> getPacketQueueIn() {
        return packetQueueIn;
    }


    public void setPacketQueueIn(BlockingQueue<ARQPacket> packetQueueIn) {
        this.packetQueueIn = packetQueueIn;
    }


    /**
     * Handle incoming packets. 
     */
    public void run() {
           System.out.println("PacketHandling");
        
        while (running) {  
            while (packetQueueIn != null) {
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

    
    
    
    /**
     * Method for handling the fileRequest before sending the data. 
     */
    public void FileRequest(ARQPacket packet) {
        
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
}
