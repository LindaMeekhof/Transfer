package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import client.ARQPacket;
import client.PacketHandler;
import client.PacketHandlerServer;
import client.TUI;


public class Raspberry implements Runnable{
    
  
    
    public static void main(String []args) throws Exception {   
       System.out.println("Trying to setup a Raspberry server");  
       
       Raspberry server = new Raspberry();
       
//       String filename = "testbestand.txt";
//       server.getPackethandler().createFileRequestPacket(filename);
       
    }
    
    private PacketHandlerServer packethandler;

    private static boolean alive;
    private int portNumber = 6667;
    private DatagramSocket socket = null;
    private byte[] received = new byte[256];
    public BlockingQueue<ARQPacket> packetQueueIn;
    public BlockingQueue<ARQPacket> packetQueueOut;
    private TUI tui;
    
    
    public PacketHandlerServer getPackethandler() {
        return packethandler;
    }

    public void setPackethandler(PacketHandlerServer packethandler) {
        this.packethandler = packethandler;
    }

    /**
     * Constructor server.
     */
    public Raspberry() {

        packetQueueIn = new LinkedBlockingQueue<ARQPacket>();
        packetQueueOut = new LinkedBlockingQueue<ARQPacket>();
        
        packethandler = new PacketHandlerServer(this);
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
            
            //Start thread for handling packets 
            Thread receivePacketHandler = new Thread(this);
            receivePacketHandler.start();
            
            System.out.println("The packetHandler is started");
            
           
        } catch (IOException e) {
            System.out.println("ERROR setting up the serversocket has failed");
            e.printStackTrace();
        }
 
        alive = true; 
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


    public BlockingQueue<ARQPacket> getPacketQueueOut() {
        return packetQueueOut;
    }


    public void setPacketQueueOut(BlockingQueue<ARQPacket> packetQueueOut) {
        this.packetQueueOut = packetQueueOut;
    }





    // Sending packets ******************************************************************** 
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
    

    // Receiving packets ***************************************************

    @Override
    public void run() {
        System.out.println("handling packets server started" );
        while (alive) {
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
                System.out.println("RECEIVED: length " + dataReceivedPacket.length);
                
                ARQPacket arq = new ARQPacket(receivedPacket);
                int flag = arq.getFlag();
              //  System.out.println("RECEIVED flag: " + flag);
                
                packethandler.getPacketQueueIn().put(arq);
                
                //Sending packets 
                if (!packetQueueOut.isEmpty()) { 
                ARQPacket packetToSend =  packetQueueOut.poll();
                
                byte[] dataToSend = packetToSend.getPacket();
                DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, IPAddress, port);
                socket.send(sendPacket);
                }
                
            } catch (IOException e) {
               System.out.println("ERROR something went wrong with the receiving of DatagramPacket");
            } catch (InterruptedException e) {
               System.out.println("Sorry interupted during packet placing in the queue");
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Interupted while sleeping");
                e.printStackTrace();
            }
        }
    }
        

}
