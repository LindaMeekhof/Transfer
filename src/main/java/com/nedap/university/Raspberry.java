package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import client.ARQPacket;
import client.PacketHandler;


public class Raspberry extends Thread {
    
    private PacketHandler packethandler = new PacketHandler();

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
  
           
            //Start thread for handling packets 
            Thread serverPacketHandler = new Thread(packethandler);
            serverPacketHandler.start();
            
            System.out.println("The packetHandler is started");
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
            
            ARQPacket arq = new ARQPacket(receivedPacket);
            int flag = arq.getFlag();
            System.out.println("RECEIVED flag: " + flag);
            
            packethandler.getPacketQueueIn().put(arq);
        } catch (IOException e) {
           System.out.println("ERROR something went wrong with the receiving of DatagramPacket");
        } catch (InterruptedException e) {
           System.out.println("Sorry interupted during packet placing in the queue");
        }
    }
    
}
