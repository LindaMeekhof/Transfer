package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import client.ARQPacket;
import client.PacketHandler;


public class Raspberry implements Runnable {
    
    private PacketHandler packethandler = new PacketHandler();

    private static boolean alive;

    public static void main(String []args) {
        
       System.out.println("Trying to setup a Raspberry server");
        
    //   Raspberry server = new Raspberry();

//        alive = true; 
//        while (alive) {
//            server.receivePackets();
//        }
    }
    
    private int portNumber = 6667;
    private DatagramSocket socket = null;
    private byte[] received = new byte[256];
    /**
     * Constructor server.
     */
    public Raspberry() {

        /** 
         * Try to setup a ServerSocket for the RaspberryPi.
         */
        try {
            System.out.println("try to start");
            socket = new DatagramSocket(portNumber);
            System.out.println("The socketserver binding is succesfully established");
  
           
            //Start thread for handling packets 
            Thread serverPacketHandler = new Thread(packethandler);
            serverPacketHandler.start();
            
            //Start thread for receiving packets
            Thread receivingPackets = new Thread(packethandler);
            serverPacketHandler.start();
            
            //Start thread for sending packets.
            
            System.out.println("The packetHandler is started");
        } catch (IOException e) {
            System.out.println("ERROR setting up the serversocket has failed");
            e.printStackTrace();
        }
        
    

        alive = true; 
        while (alive) {
            receivePackets();
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Interupted while sleepin in raspberryPi loop");
                e.printStackTrace();
            }
        }
    }
    
    
    public void receivePackets() {
        try {
            DatagramPacket receivedPacket = new DatagramPacket(received, received.length);
            socket.receive(receivedPacket);
             
           
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
    
    /**
     * Sending a packet.
     * @param sendPacket
     */
    public void sendingPackets(DatagramPacket sendPacket) {
        if(socket != null) {
            try {
                socket.send(sendPacket);
            } catch (IOException e) {
                System.out.println("ERROR sending a packet has failed");
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        while(alive) {
          
        }
    }
    
    
}
