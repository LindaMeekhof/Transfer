package general;

import java.util.ArrayList;

import client.ARQPacket;
import client.Constants;


public class TransferProtocol extends DownloadManager implements Constants{

    
    public TransferProtocol(String filename, int amountPackets, int fileSize) {
        super(filename, amountPackets, fileSize);
        // TODO Auto-generated constructor stub
    }

    public void send() throws Exception {
        // variabelen
        int LAR = 0;
        int LFS = LAR + 2;
        int countSendingPackets = 0;
        int windowSize = 3;
            
        System.out.println("Sending...");

        // read from the input file
        byte[] fileContents = FileManager.FileToByteArray(filename);

        // keep track of where we are in the data
        int filePointer = 0;
        int packetNumber = 0;
        int amountPackets = fileContents.length / DATASIZE + 1;
        this.amountPackets = amountPackets;
        int ackNumber = 0; //TODO
        int option = 0; //TODO
        while (filePointer < fileContents.length) {
                
            ArrayList<ARQPacket> pktBuffer = new ArrayList<ARQPacket>();
                
             // Sending 3 packets before expecting ACK ----------------------------
            while (countSendingPackets < 3) {   
                   
                    //Doorgaan met verzenden van informatie
                    
                
                    int datalen = Math.min(DATASIZE, fileContents.length - filePointer);
                    
                    //create new ARQPacket
                    byte[] pkt = new byte[datalen];
                    System.arraycopy(fileContents, filePointer, pkt, 0, datalen);
                    ARQPacket packet = new ARQPacket(DOWNLOAD, fileID, packetNumber, ackNumber, datalen + HEADERSIZE, option, pkt);
                    
                    pktBuffer.add(packet);

                    // send the packets to the waitingqueue out.
                    //TODO thread save method??                  
                    packethandler.send(packet);
                    
                   
                    System.out.println("Sent packet with sequenceNr="+ packet.getSequenceNumber() + "FileID " + packet.getFileName());
                    countSendingPackets++;
            }
            
            // 3 packets are sent and reset countSendingPackets
            countSendingPackets = countSendingPackets % windowSize;
            System.out.println(countSendingPackets);
            
            
            boolean stop = false;
            int counter = 1000; //timer
            long startTime = System.currentTimeMillis();

            while (!stop && counter < (System.currentTimeMillis() - startTime)) {

                    // getting ACK from receiver-side
                //Receiving ACK for this file
                
                ARQPacket packet = ACKPacketsReceived.poll();
                packet.getACKNumber();
                
                    //making a new array with booleans
                boolean[] arrivedACK = new boolean[LFS - LAR + 1];

                if (packet != null) {
                    
                    int countACK = 0;
                    while (packet.getACKNumber() >= LAR && packet.getACKNumber() <= LFS && countACK < (LFS + 1)) {

                        System.out.println("Sender received ACK + first byte= " + packet.getACKNumber());

                        if (packet.getACKNumber() == LAR) {
                            System.out.println("Sender received ACK and buffer" + packet.getACKNumber());
                            //LAR++;
                            arrivedACK[LAR] = true;
                        } else if (packet.getACKNumber() == (LAR + 1)) {
                            System.out.println("Sender received ACK and buffer" + packet.getACKNumber());
                            arrivedACK[LAR + 1] = true;
                        } else if (packet.getACKNumber() == (LAR + 2)) {
                            arrivedACK[LAR + 1] = true;
                        } else {
                            System.out.println("Sender received ACK in else statement" + packet.getACKNumber());
                        }
                    }   
                    
                    //when there are possible three ACK's
                    int countTrue = 0;
                    for (int index = 0; index < arrivedACK.length - 1; index++) {
                        if (arrivedACK[index]) {
                            countTrue++;
                        }
                    }
                    
                    if (countTrue == (LFS - LAR + 1)) {
                        //All ACK's are received, so LAR can be + 3
                        LAR = LAR + 3; 
                        stop = true;
                        filePointer = filePointer + 3 * DATASIZE;
                        packetNumber = packetNumber + 3;
                    } else {
                        //kijken welke er missen en deze sturen.
                        for (int index = 0; index < arrivedACK.length - 1; index++) {
                            if (!arrivedACK[index]) {
                                ARQPacket pktAgain = pktBuffer.get(index);
                                packethandler.send(pktAgain);
                            }
                        }
                    }
                }
     
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    System.out.println("interrupted while sleeping");
                }
            }
        }
    }
    
    
    
    public void receive() {
        //TODO
    }
    
    public void saveFile() {
        //TODO
    }
}
