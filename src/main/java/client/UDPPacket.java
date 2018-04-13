package client;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class UDPPacket {

    public UDPPacket() {
        
    }
    
    /** 
     * Constructor for creating a UDPPacket with ARQPacket. Misschien niet nodig
     * @param packet
     * @param IPAddress
     * @param destinationPort
     */
    public UDPPacket(ARQPacket arq, InetAddress IPAddress, int destinationPort) {
   
            byte[] head = arq.getHeader();
            DatagramPacket datagram = new DatagramPacket(head, head.length, 
                    IPAddress, destinationPort);     
    }
    
    
}
