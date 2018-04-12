package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import client.ARQPacket;

public class Raspberry extends Thread {

    private static boolean alive;

    public static void main(String []args) {
        
       System.out.println("Trying to setup a Raspberry server");
        
       Raspberry server = new Raspberry();

        alive = true; 
        while (alive) {
            int i = 0;
            server.receivePackets();
            i++;
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
            int i = 0; 
           
            //UDP
            String sentence = new String(receivedPacket.getData());
            System.out.println("RECEIVED: " + i + sentence);
            InetAddress IPAddress = receivedPacket.getAddress();
            System.out.println("RECEIVED: address " + IPAddress);
            int port = receivedPacket.getPort();
            System.out.println("RECEIVED: port source " + port);
            
            //Header
            byte[] dataReceivedPacket = receivedPacket.getData();
            System.out.println("length: " + dataReceivedPacket.length);
            System.out.println("flag: " + dataReceivedPacket[0]);
            System.out.println("name: " + dataReceivedPacket[1]);
            
            //create ARQPacket
            ARQPacket newARQ = new ARQPacket(receivedPacket);
            int flag = newARQ.getFlag();
            System.out.println("FLAG received: " + flag);
            
            System.out.println("komt hij hier");
           
        } catch (IOException e) {
           System.out.println("ERROR something went wrong with the receiving of DatagramPacket");
        }
    }
    
}
