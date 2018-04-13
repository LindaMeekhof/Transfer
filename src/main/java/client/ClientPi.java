package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

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
        String str = "welcome testing";
        //      byte[] sendData = new byte[1024];
        DatagramPacket sendPacket = new DatagramPacket(str.getBytes(), str.length(), IPAddress, destinationPort);
        //  DatagramPacket sendPacket = new DatagramPacket(str.getBytes(), str.length(), raspberryAddress, destinationPort);
        client.send(sendPacket);
        
        
        // Create datagram with ARQPacket *****************************************************
        ARQPacket arq = new ARQPacket(8, 2, 3, 4, 5, 6);
        byte[] head = arq.getHeader();
        System.out.println(head);
        DatagramPacket sendHeader = new DatagramPacket(head, head.length, 
                IPAddress, destinationPort);
        client.send(sendHeader);
        
        ARQPacket arq1 = new ARQPacket(1, 2, 3, 4, 5, 6);
        byte[] head1 = arq1.getHeader();
        System.out.println(head1);
        DatagramPacket sendHeader1 = new DatagramPacket(head, head1.length, 
                IPAddress, destinationPort);
        client.send(sendHeader1);
        
        //Create a FileRequest Message
        ARQPacket fileReq = new ARQPacket(FILE_REQUEST, 0, 0, 0, 0, 0);
        DatagramPacket fileReq1 = createDatagram(fileReq, IPAddress, destinationPort);
        client.send(fileReq1);
        
        // ---------------------------------------------------------------------
        
      
   
        
//        ARQPacket packet = new ARQPacket();
//         DatagramPacket sendPacket2 = new DatagramPacket(packet.getPacket(), packet.getPacket().length, IPAddress, destinationPort); 
//         client.send(sendPacket2);
    }
    

    private DatagramSocket clientSocket = null;
    private static int destinationPort = 6666;
    
    /**
     * Create a datagram with only a header
     * @param arq
     * @param IPAddress
     * @param destinationPort
     * @return
     */
    public static DatagramPacket createDatagram(ARQPacket arq, 
            InetAddress IPAddress, int destinationPort) {
        byte[] head = arq.getHeader();
        System.out.println(head);
        DatagramPacket datagram = new DatagramPacket(head, head.length, 
                IPAddress, destinationPort);
        return datagram;
    }
    
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
    
    public byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES); 
        bb.putInt(i); 
        return bb.array();
    }
    
    private int DATASIZE;
    /**
     * Create a FILE_REQUEST message
     * @throws Exception 
     */
    public DatagramPacket createFileRequestPacket(String filename, byte[] fileContents) throws Exception {
       
        byte[] buffer = new byte[0];
        ARQPacket fileReq = new ARQPacket(FILE_REQUEST, 0, 0, 0, 0, 0);
        
        //Amount of packets
        int amountPkt= fileContents.length/DATASIZE + 1;
        byte [] amountPackets = intToBytes(amountPkt);
        
        //filename
        byte[] name = filename.getBytes();
        
        System.arraycopy(amountPackets, 0, buffer, 0, amountPackets.length);
        System.arraycopy(name, 0, buffer, amountPackets.length, name.length);
        
        return null;
    }
    
}
