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

public class ClientPi implements Runnable, Constants {

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
        

        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e1) {
            System.out.println("Unknown host");
            e1.printStackTrace();
        }
        
        // TestPacket ************************************************************************************ 
        ClientPi client = new ClientPi();
        
//        String content = "ackack";
//        ARQPacket arq = new ARQPacket();
//        arq.setSequenceNumber(11);
//        client.getPacketHandler().createAcknowledgementMessage(arq);

//        boolean alive = true;
//        while (alive) {
//            client.sendPacketsQueue();
//   
//            Thread.sleep(2000);
//        }
//        
//        System.out.println("end main");
        
    }
    
 // Fields  *************************************************
    private static InetAddress address = null;
    
    private DatagramSocket clientSocket = null;
    private PacketHandler packethandler;
    
    public BlockingQueue<ARQPacket> packetQueueIn;
    public BlockingQueue<ARQPacket> packetQueueOut;
    
    public ArrayList<String> downLoading = new ArrayList<String>();
    public ArrayList<String> upLoading = new ArrayList<String>();
    
    private boolean alive;
    
    private TUI tui;
   
    private static int destinationPort = 6667;
    private byte[] received = new byte[1000];
    private static final int TIMEOUT = 1000;
    private ArrayList<String> ListOfAvailableFiles = new ArrayList<String>();
  
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
      
        try {
       
            clientSocket = new DatagramSocket();
            System.out.println("The clientSocket binding is succesfully established");
       
             //Start thread for handling packets 
            packethandler = new PacketHandler(this);
            Thread clientPacketHandler = new Thread(packethandler);
            clientPacketHandler.start();
            
            //Start thread for handling packets 
            Thread receivePacketHandler = new Thread(this);
            receivePacketHandler.start();
            
            System.out.println("The packetHandler client is started");
        
        } catch (IOException e) {
            System.out.println("ERROR The clientSocket binding is not succesfull");
            e.printStackTrace();
        }  
  
        alive = true; 
        while (alive) {
            sendingPackets();
        }
    }
    
    // Sending packets ******************************************************************** 
    /**
     * Sending a packet.
     * @param sendPacket
     */
    public void sendingPackets() {
        if(clientSocket != null) {
            try {
               // System.out.println("running sending packets client");
                if (!packetQueueOut.isEmpty()) { 
                    System.out.println("the queue is not empty");
                    
                    ARQPacket packetToSend =  packetQueueOut.poll();
                   // --> hier gebleven //TODO
                     
                    byte[] dataToSend = packetToSend.getPacket();
                    
                    DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, address, destinationPort);
                    clientSocket.send(sendPacket);
                    } 
            } catch (IOException e) {
                System.out.println("ERROR sending a packet has failed");
                e.printStackTrace();
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Interupted while sleeping");
                e.printStackTrace();
            }
        }
    }
    
    


    @Override
    public void run() {
        while(alive) {
            System.out.println("client receive running");
            try {
                DatagramPacket receivedPacket = new DatagramPacket(received, received.length);
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

                
                //-> hier gebleven
                ARQPacket arq = new ARQPacket(receivedPacket, IPAddress, destinationPort);

                packethandler.getPacketQueueIn().put(arq);

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

    

    public ArrayList<String> getDownLoading() {
        return downLoading;
    }

    public void setDownLoading(ArrayList<String> downLoading) {
        this.downLoading = downLoading;
    }

    public ArrayList<String> getUpLoading() {
        return upLoading;
    }

    public void setUpLoading(ArrayList<String> upLoading) {
        this.upLoading = upLoading;
    }

    public BlockingQueue<ARQPacket> getPacketQueueOut() {
        return packetQueueOut;
    }

    public void setPacketQueueOut(BlockingQueue<ARQPacket> packetQueueOut) {
        this.packetQueueOut = packetQueueOut;
    }
 
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
        return packethandler;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packethandler = packetHandler;
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
