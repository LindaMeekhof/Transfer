package client;


import java.util.ArrayList;
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
        
       String str = in.next();
       
       String[] message = str.split("_");
       
       System.out.println(message[0]);
       
       if (message[0].equalsIgnoreCase("REQUEST") && message.length == 2) {
           //send
           String filename = message[1];
           
           //tijdelijk al aanwezig //TODO
           client.getListOfAvailableFiles().add(filename);
           if (isAvailableFile(filename)) {
               System.out.println("This file is available");
               client.getPackethandler().connectionAndSendRequest(filename);          
           } else {
               System.out.println("This file is not available");
           }  
       } else if (message[0].equalsIgnoreCase("UPLOAD") && message.length == 2) {         
           if (isAvailableFile(message[1])) {
     //           client.getPackethandler().createUploadRequest(message[2]);
           }
       } else if (message[0].equalsIgnoreCase("PAUSE")) {
           if (isDownloading(message[1])) {
              int fileID = 0; //TODO
       //       client.getPackethandler().createPauseRequest(fileID); 
           } else {
               System.out.println("This is not a file that is currently dowloaded, can't pause");
           } 
       } else if (message[0].equalsIgnoreCase("RESUME") && message.length == 2) {
           if (isDownloading(message[1])) {
       //        client.getPackethandler().createResume(message[1]);
           } else {
               System.out.println("This is not a file that is paused, can't resume");
           }
           
       } else if (message[0].equalsIgnoreCase("FILELIST")) {
      //      client.getPackethandler().createFileListRequest();
            System.out.println("filelist request send");
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
    public boolean isDownloading(String filename) {
        return client.getDownLoading().contains(filename);
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