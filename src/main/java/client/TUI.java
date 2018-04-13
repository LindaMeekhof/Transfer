package client;

import java.util.ArrayList;
import java.util.Scanner;



public class TUI {
    
   

    private static Scanner in;
    
    public TUI() {
        in = new Scanner(System.in);
    }
   
    /**
     * Reading input from the console.
     */
    public static void readInput() {
        
       printMenu();
       
       String str = in.next();
       
       String[] message = str.split(" ");
       
       if (message[1].equalsIgnoreCase("REQUEST")) {
           //Send request message
           
       } else if (message[1].equalsIgnoreCase("UPLOAD")) {
           
           
           //Send upload message    
       } else if (message[1].equalsIgnoreCase("PAUSE")) {
           //Send pause message 
 
           if (isDownloading(message[2])) {
               //send a pause message
           } else {
               System.out.println("This is not a file that is currently dowloaded, can't pause");
               printMenu();
           }
           
       } else if (message[1].equalsIgnoreCase("RESUME")) {
           //Send resume message   
           if (isDownloading(message[2])) {
               //send a pause message
           } else {
               System.out.println("This is not a file that is paused, can't resume");
           }
           
       } else if (message[1].equalsIgnoreCase("FILELIST")) {
           //Send file list message
       } else {
           print("This is an unknown command");
       }
       
       
       
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
        print("For REQUEST_FILE enter:          REQUEST <spatie> FILENAME");
        print("For UPLOADING FILE enter:        UPLOAD");
        print("For PAUSE FILE enter:            PAUSE <spatie> FILENAME");
        print("For RESUME FILE transfer enter:  RESUME <spatie> FILENAME");
        print("For REQUEST_FILE list enter:     FILELIST");
    }
    
}