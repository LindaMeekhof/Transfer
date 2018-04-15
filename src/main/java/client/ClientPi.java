package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientPi implements Constants {

    public static void main(String []args) throws Exception {

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
        
        // TestPacket ************************************************************************************ 
        ClientPi client = new ClientPi();
        

        /**
         * Sending Datagram. First construct a Datagram with header and a piece of data. 
         */

//        String filename = "testbestand.txt";
//        client.getPacketHandler().createFileRequestPacket(filename);

        String content = "ackack";
        ARQPacket arq = new ARQPacket();
        arq.setSequenceNumber(11);
        client.getPacketHandler().createAcknowledgementMessage(arq);

        
        boolean alive = true;
        while (alive) {
            client.sendPacketsQueue();
            // --> hier gebleven TODO
            System.out.println("uitgevoerd");
            Thread.sleep(2000);
        }
        
        System.out.println("end main");
        
    }
    
 // Fields  *************************************************
    private DatagramSocket clientSocket = null;
    private TUI tui;
    private PacketHandler packetHandler;
    public BlockingQueue<ARQPacket> packetQueueIn;
    public BlockingQueue<ARQPacket> packetQueueOut;
    public BlockingQueue<ARQPacket> getPacketQueueOut() {
        return packetQueueOut;
    }

    public void setPacketQueueOut(BlockingQueue<ARQPacket> packetQueueOut) {
        this.packetQueueOut = packetQueueOut;
    }

    private static int destinationPort = 6667;
    private static final int TIMEOUT = 1000;
    private ArrayList<String> ListOfAvailableFiles = new ArrayList<String>();
    private boolean alive;
   
    
    public  ArrayList<String> getListOfAvailableFiles() {
        return ListOfAvailableFiles;
    }

    public  void setListOfAvailableFiles(ArrayList<String> listOfAvailableFiles) {
        ListOfAvailableFiles = listOfAvailableFiles;
    }

    // Getters and setters ****************************************    
    public BlockingQueue<ARQPacket> getPacketQueueIn() {
        return packetQueueIn;
    }

    public void setPacketQueueIn(LinkedBlockingQueue<ARQPacket> packetQueueIn) {
        this.packetQueueIn = packetQueueIn;
    }


   
    
    
    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }
    
    // Constructor **********************************

    /**
     * Constructor ClientPi. Setting up the DatagramSocket connection. 
     */
    public ClientPi() {
        
        packetQueueIn = new LinkedBlockingQueue<ARQPacket>();
        packetQueueOut = new LinkedBlockingQueue<ARQPacket>();
        

        tui = new TUI(this);
        Thread clientTUI = new Thread(tui);
        clientTUI.start();
        //setup socket connection
       
        
        //Start thread for handling packets 
        packetHandler = new PacketHandler(this);
        Thread clientPacketHandler = new Thread(packetHandler);
        clientPacketHandler.start();
         
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
        if (clientSocket != null) {
            try {
                byte[] receivedData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
                clientSocket.receive(receivePacket);
                
                
                
//                ARQPacket packet = null;
//                if (!packetQueueOut.isEmpty()) {
//                
//                  System.out.println("komt niet hier");
//                    try {
//                       packet = packetQueueOut.poll(TIMEOUT, TimeUnit.MILLISECONDS);
//                    } catch (InterruptedException e) { 
//                        System.out.println("Something went wrong with ARQPacket dequeue in piclient");
//                    }
//                 
//                InetAddress IPaddress = getIPAddress();
//                DatagramPacket packetFromQueue = createDatagram(packet, IPaddress, destinationPort);    
//                
//                clientSocket.send(packetFromQueue);
//                }
                
             //   System.out.println("komt hier");
                clientSocket.send(sendPacket);
                
            } catch (IOException e) {
                System.out.println("ERROR sending a packet has failed");
                e.printStackTrace();
            }
        }
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    private void sendPacketsQueue() {
        if (clientSocket != null) {

            if (!packetQueueOut.isEmpty()) {
                ARQPacket packet = null;
                DatagramPacket packetFromQueue = null;
                System.out.println("que verzonden");
                try {
                    packet = packetQueueOut.poll(TIMEOUT, TimeUnit.MILLISECONDS);

                    InetAddress IPaddress = getIPAddress();
                    packetFromQueue = packetHandler.createDatagram(packet, IPaddress, destinationPort);    
                    clientSocket.send(packetFromQueue);
                    
                    
                    byte[] receivedData = new byte[1024];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                    clientSocket.receive(receivedPacket);
                    
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
                    getPacketQueueIn().put(arq);
                    
                } catch (InterruptedException e) { 
                    System.out.println("Something went wrong with ARQPacket dequeue in piclient");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }   
            }
        }
    }

    
    private InetAddress getIPAddress() {
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e1) {
            System.out.println("Unknown host");
            e1.printStackTrace();
        }
        return IPAddress;
    }
    
    


}
