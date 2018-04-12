package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Raspberry extends Thread {

    private static boolean alive;

    public static void main(String []args) {
        
       System.out.println("Trying to setup a Raspberry server");
        
       Raspberry server = new Raspberry();

        alive = true; 
        while (alive) {
            server.receivePackets();
        }
    }
    
    private int portNumber = 6666;
    private DatagramSocket myServerSocket = null;
    private byte[] received = new byte[256];
    /**
     * Constructor server.
     */
    public Raspberry() {

        /** 
         * Try to setup a ServerSocket for the RaspberryPi.
         */
        try {
            myServerSocket = new DatagramSocket(portNumber);
            System.out.println("The socketserver binding is succesfully established");
        } catch (IOException e) {
            System.out.println("ERROR setting up the serversocket has failed");
            e.printStackTrace();
        }
    }
    
    
    public void receivePackets() {
        try {
            DatagramPacket receivedPacket = new DatagramPacket(received, received.length);
            myServerSocket.receive(receivedPacket);
             
           
            //UDP
            String sentence = new String(receivedPacket.getData());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivedPacket.getAddress();
            System.out.println("RECEIVED: address " + IPAddress);
            int port = receivedPacket.getPort();
            System.out.println("RECEIVED: port source " + port);
            
            byte[] dataReceivedPacket = receivedPacket.getData();
            System.out.println("length: " + dataReceivedPacket.length);
            System.out.println("flag: " + dataReceivedPacket[0]);
            System.out.println("name: " + dataReceivedPacket[1]);
            
        } catch (IOException e) {
           System.out.println("ERROR something went wrong with the receiving of DatagramPacket");
        }
    }
    
}
