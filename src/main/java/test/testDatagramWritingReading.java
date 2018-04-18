package test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import client.ARQPacket;

public class testDatagramWritingReading {



    public static void main(String[] args) throws Exception {

        /** 
         * IP address from the client. 
         */
        InetAddress address = null;
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e1) {
            System.out.println("Unknown host");
            e1.printStackTrace();
        }
        int port = 6666;

        //writing and reading is correct
        ARQPacket arq = new ARQPacket (1, 2, 3, 4, 5, 6);

        int flag = arq.getFlag();
        int fileID = arq.getFileID();
        int sequence = arq.getSequenceNumber();
        int ack = arq.getACKNumber();
        int contentLen = arq.getContentLength();
        int option = arq.getOptions();

        byte[] data = arq.getData();
        byte[] header = arq.getHeader();
        byte[] packet = arq.getPacket();

        System.out.println(flag);
        System.out.println(fileID);
        System.out.println(sequence); 
        System.out.println(ack);
        System.out.println(contentLen);
        System.out.println(option);

        System.out.println(data);
        System.out.println(header);
        System.out.println(packet);


        //writing and reading is correct
        String sentence = "TESTING";
        byte[] sendData = sentence.getBytes();

        ARQPacket arq1 = new ARQPacket (1, 2, 3, 4, sendData.length, 6, sendData);

        int flag1 = arq.getFlag();
        int fileID1 = arq.getFileID();
        int sequence1 = arq.getSequenceNumber();
        int ack1 = arq.getACKNumber();
        int contentLen1 = arq.getContentLength();
        int option1 = arq.getOptions();

        byte[] data1 = arq.getData();
        byte[] header1 = arq.getHeader();
        byte[] packet1 = arq.getPacket();

        System.out.println(flag1);
        System.out.println(fileID1);
        System.out.println(sequence1); 
        System.out.println(ack1);
        System.out.println(contentLen1);
        System.out.println(option1);

        System.out.println(data1);
        System.out.println(header1);
        System.out.println(packet1);

        String sen = new String(arq1.getData());
        System.out.println(sen);


        //
        String sent = "TESTING";
        byte[] sendData1 = sent.getBytes();

        ARQPacket arq2 = new ARQPacket (1, 2, 3, 4, sendData1.length, 6, sendData1);

        int flag2 = arq.getFlag();
        int fileID2 = arq.getFileID();
        int sequence2 = arq.getSequenceNumber();
        int ack2 = arq.getACKNumber();
        int contentLen2 = arq.getContentLength();
        int option2 = arq.getOptions();

        byte[] data2 = arq.getData();
        byte[] header2 = arq.getHeader();
        byte[] packet2 = arq.getPacket();

        System.out.println(flag2);
        System.out.println(fileID2);
        System.out.println(sequence2); 
        System.out.println(ack2);
        System.out.println(contentLen2);
        System.out.println(option2);

        System.out.println(data2);
        System.out.println(header2);
        System.out.println(packet2);

        byte[] dataToSend = arq2.getPacket();
        DatagramPacket packetToSend = new DatagramPacket(dataToSend, dataToSend.length, address, port);
        
        ARQPacket newArq = new ARQPacket(packetToSend, address, port);
        
        int flag4 = newArq.getFlag();
        int fileID4 = newArq.getFileID();
        int sequence4 = newArq.getSequenceNumber();
        int ack4 = newArq.getACKNumber();
        int contentLen4 = newArq.getContentLength();
        int option4 = newArq.getOptions();

        byte[] data4 = newArq.getData();
        byte[] header4 = newArq.getHeader();
        byte[] packet4 = newArq.getPacket();

        System.out.println(flag4);
        System.out.println(fileID4);
        System.out.println(sequence4); 
        System.out.println(ack4);
        System.out.println(contentLen4);
        System.out.println(option4);

        System.out.println(data4);
        System.out.println(header4);
        System.out.println(packet4);

        
        
    }




}
