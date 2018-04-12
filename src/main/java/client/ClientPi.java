package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientPi {

    public static void main(String []args) {

        System.out.println("Try to start a client for connecting to Raspberry");

        /**
         * IP Address from the raspberry.
         */
        InetAddress raspberryAddress = null;
        try {
            raspberryAddress = InetAddress.getByName("192.168.1.1");
        } catch (UnknownHostException e) {
           System.out.println("ERROR raspberry address");
            e.printStackTrace();
        } 
        
        /** 
         * IP address from the client. 
         */
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e1) {
            System.out.println("Unknown host");
            e1.printStackTrace();
        }
        
        
        ClientPi client = new ClientPi();

        /**
         * Sending Datagram. First construct a Datagram with header and a piece of data. 
         */
        String str = "welcome testing";
  //      byte[] sendData = new byte[1024];
      
      DatagramPacket sendPacket = new DatagramPacket(str.getBytes(), str.length(), IPAddress, destinationPort);
        //DatagramPacket sendPacket = new DatagramPacket(str.getBytes(), str.length(), raspberryAddress, destinationPort);
        client.send(sendPacket);
        
        ARQPacket packet = new ARQPacket();
         DatagramPacket sendPacket2 = new DatagramPacket(packet.getPacket(), packet.getPacket().length, IPAddress, destinationPort); 
         client.send(sendPacket2);
    }
    

    private DatagramSocket clientSocket = null;
    private static int destinationPort = 6666;
    
    
    /**
     * Constructor ClientPi. Setting up the DatagramSocket connection. 
     */
    public ClientPi() {
   
        //setup socket connection
        try {
       
          clientSocket = new DatagramSocket();
            System.out.println("The clientSocket binding is succesfully established");
        } catch (IOException e) {
            System.out.println("ERROR The clientSocket binding is not succesfull");
            e.printStackTrace();
        }  
    }
    
    /**
     * Sending a packet.
     * @param sendPacket
     */
    private void send(DatagramPacket sendPacket) {
        if(clientSocket != null) {
            try {
                clientSocket.send(sendPacket);
            } catch (IOException e) {
                System.out.println("ERROR sending a packet has failed");
                e.printStackTrace();
            }
        }
    }
}
