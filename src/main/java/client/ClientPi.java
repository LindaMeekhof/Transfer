package client;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import general.Constants;
import general.DownloadManager;


public class ClientPi extends Thread implements Constants {

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
        
 
        ClientPi client = new ClientPi();

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
   
    private static int portNumber = 6667;
    private byte[] received = new byte[1000];
    private static final int TIMEOUT = 1000;
    private ArrayList<String> ListOfAvailableFiles = new ArrayList<String>();
  
    public HashMap<String, Integer> mapFileNames = new HashMap<String, Integer>();
    public HashMap<Integer, DownloadManager> idToDownloadManager = new HashMap<Integer, DownloadManager>();

    
    // Constructor **********************************

    /**
     * Constructor ClientPi. Setting up the DatagramSocket connection. 
     */
    public ClientPi() {
        
        packetQueueIn = new LinkedBlockingQueue<ARQPacket>();
        packetQueueOut = new LinkedBlockingQueue<ARQPacket>();
        
        if (portNumber == 6667) {
        tui = new TUI(this);
        Thread clientTUI = new Thread(tui);
        clientTUI.start();
        }
        //setup socket connection
      
        try {
            
            if (portNumber == 6667) {

            clientSocket = new DatagramSocket();

            System.out.println("starting as client"); 
            } else {
         
            clientSocket = new DatagramSocket(6667);
            System.out.println("starting as server");   

            }     
            
             //Start thread for handling packets 
            packethandler = new PacketHandler(this);
            Thread clientPacketHandler = new Thread(packethandler);
            clientPacketHandler.start();
            
            //Start thread for handling packets 
            Thread receivePacketHandler = new Thread(this);
            receivePacketHandler.start();
            
            System.out.println("The packetHandler is started");
            
        } catch (IOException e) {
            System.out.println("ERROR The clientSocket binding is not succesfull");
            e.printStackTrace();
            
        }  
  
        alive = true; 
        while (alive) {
            sendingPackets();
        }
    }
    
    
    public void sending() {
        
    }
    // Sending packets ******************************************************************** 
    /**
     * Sending a packet.
     * @param sendPacket
     */
    public void sendingPackets() {
        
        if(clientSocket != null) {
          
            try {
                
                if (portNumber == 6667) { //client
                    if (!packetQueueOut.isEmpty()) { 
                        System.out.println("the queue is not empty");
                        
                        ARQPacket packetToSend =  packetQueueOut.poll(10, TimeUnit.MILLISECONDS);
                    
                         
                        byte[] dataToSend = packetToSend.getPacket();
                        
                        System.out.println("address" + address);
                        DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, address, portNumber);
                        clientSocket.send(sendPacket);
                        System.out.println("send as client");
                        } 
                } else { //server
           
                    if (!packetQueueOut.isEmpty()) { 
                        System.out.println("the queue is not empty");
                        
                        ARQPacket packetToSend =  packetQueueOut.poll(10, TimeUnit.MILLISECONDS);
                     
                        int port = packetToSend.getDestinationPort();
                        InetAddress IPAddress =  packetToSend.getAddress(); 
                        
                        byte[] dataToSend = packetToSend.getPacket();
                        DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, IPAddress, port);
                        clientSocket.send(sendPacket);
                        } 
                }
            } catch (IOException e) {
                System.out.println("ERROR sending a packet has failed");
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println("Interupted while sleeping");
                e.printStackTrace();
            }
        }
    }

    


    @Override
    public void run() {
        while(alive) {
          //  System.out.println("client receive running");
            try {
                if (portNumber == 6667) {
                    DatagramPacket receivedPacket = new DatagramPacket(received, received.length);
                    clientSocket.receive(receivedPacket);

                    //UDP
                    String sentence = new String(receivedPacket.getData());
               System.out.println("RECEIVED: data " + sentence);
                    InetAddress IPAddress = receivedPacket.getAddress();
                    System.out.println("RECEIVED: address " + IPAddress);
                    int port = receivedPacket.getPort();
                    System.out.println("RECEIVED: port source " + port);

                    byte[] dataReceivedPacket = receivedPacket.getData();
                    System.out.println("RECEIVED: length " + dataReceivedPacket.length);

                    ARQPacket arq = new ARQPacket(receivedPacket, IPAddress, port);

                    packetQueueIn.put(arq);

                } else {
                    DatagramPacket receivedPacket = new DatagramPacket(received, received.length);
                    clientSocket.receive(receivedPacket);

                    //UDP
                    String sentence = new String(receivedPacket.getData());
               //     System.out.println("RECEIVED: data " + sentence);
                    InetAddress IPAddress = receivedPacket.getAddress();
                    System.out.println("RECEIVED: address " + IPAddress);
                    int port = receivedPacket.getPort();
                    System.out.println("RECEIVED: port source " + port);

                    byte[] dataReceivedPacket = receivedPacket.getData();
                    System.out.println("RECEIVED: length " + dataReceivedPacket.length);

                    ARQPacket arq = new ARQPacket(receivedPacket, IPAddress, port);

                    packetQueueIn.put(arq);
                    
                }


            } catch (IOException e) {
                System.out.println("ERROR something went wrong with the receiving of DatagramPacket");
            } catch (InterruptedException e) {
                System.out.println("Sorry interupted during packet placing in the queue");
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println("Interupted while sleeping");
                e.printStackTrace();
            }
        }
    }

   
    // Getters and setters **********************************************
    /**
     * 
     * @return
     */
    public static InetAddress getAddress() {
        return address;
    }

    public static void setAddress(InetAddress address) {
        ClientPi.address = address;
    }

    public DatagramSocket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(DatagramSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public PacketHandler getPackethandler() {
        return packethandler;
    }

    public void setPackethandler(PacketHandler packethandler) {
        this.packethandler = packethandler;
    }

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

    public TUI getTui() {
        return tui;
    }

    public void setTui(TUI tui) {
        this.tui = tui;
    }

    public static int getPortNumber() {
        return portNumber;
    }

    public static void setPortNumber(int portNumber) {
        ClientPi.portNumber = portNumber;
    }

    public byte[] getReceived() {
        return received;
    }

    public void setReceived(byte[] received) {
        this.received = received;
    }

    public ArrayList<String> getListOfAvailableFiles() {
        return ListOfAvailableFiles;
    }

    public void setListOfAvailableFiles(ArrayList<String> listOfAvailableFiles) {
        ListOfAvailableFiles = listOfAvailableFiles;
    }

    public HashMap<String, Integer> getMapFileNames() {
        return mapFileNames;
    }

    public void setMapFileNames(HashMap<String, Integer> mapFileNames) {
        this.mapFileNames = mapFileNames;
    }

    public HashMap<Integer, DownloadManager> getIdToDownloadManager() {
        return idToDownloadManager;
    }

    public void setIdToDownloadManager(HashMap<Integer, DownloadManager> idToDownloadManager) {
        this.idToDownloadManager = idToDownloadManager;
    }

    public static int getTimeout() {
        return TIMEOUT;
    }


   

}
