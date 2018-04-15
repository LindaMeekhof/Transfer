package client;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;



public class TUI implements Runnable {
    
   

    private static Scanner in;
    private ClientPi client;
 
   
    public TUI(ClientPi client) {
        in = new Scanner(System.in);
        this.client = client;
    }

    /**
     * Reading input from the console.
     * @return 
     * @throws Exception 
     */
    public void readInput() throws Exception {
        
       printMenu();
       
       String str = in.next();
       
       String[] message = str.split("_");
       
       System.out.println(message[0]);
       
       if (message[0].equalsIgnoreCase("REQUEST") && message.length == 2) {
           //send
           String filename = message[1];
           
           //tijdelijk al aanwezig //TODO
           client.getListOfAvailableFiles().add(filename);
           if(isAvailableFile(filename)) {
               client.getPacketHandler().createFileRequestPacket(filename);
            
           } else {
               System.out.println("This file is not available");
           }
    
       } else if (message[0].equalsIgnoreCase("UPLOAD")) {
           
           
           //Send upload message    
       } else if (message[0].equalsIgnoreCase("PAUSE")) {
           //Send pause message 
 
           if (isDownloading(message[1])) {
               //send a pause message
           } else {
               System.out.println("This is not a file that is currently dowloaded, can't pause");
               printMenu();
           }
           
       } else if (message[0].equalsIgnoreCase("RESUME")) {
           //Send resume message   
           if (isDownloading(message[1])) {
               //send a pause message
           } else {
               System.out.println("This is not a file that is paused, can't resume");
           }
           
       } else if (message[0].equalsIgnoreCase("FILELIST")) {
           //Send file list message
       } else {
           print("This is an unknown command");
       }

       
       
       
    }
    
    
    private boolean isAvailableFile(String filename) {
        return client.getListOfAvailableFiles().contains(filename);
    }
    
    //TODO dit moet op een andere plek staan 
    private static ArrayList<String> listCurrentDownloads = new ArrayList<String>();
    
    /**
     * Check if you are currently downloading a specific file
     */
    public static boolean isDownloading(String filename) {
        return listCurrentDownloads.contains(filename);
    }
    
    /**
     * Printing method.
     * @param text
     */
    public static void print(String text) {
        System.out.println(text);
    }
    
    public static void printMenu() {
        print("welcome, please enter your choice");
        print("For REQUEST_FILE enter:          REQUEST < _ > FILENAME");
        print("For UPLOADING FILE enter:        UPLOAD");
        print("For PAUSE FILE enter:            PAUSE < _ > FILENAME");
        print("For RESUME FILE transfer enter:  RESUME < _ > FILENAME");
        print("For REQUEST_FILE list enter:     FILELIST");
    }

    @Override
    public void run() {
        boolean tuiRunning = true;
        while(tuiRunning) {
            printMenu();
            try {
                readInput();
            } catch (Exception e) {
                System.out.println("Something went wrong while reading input user");
                e.printStackTrace();
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }


    
}