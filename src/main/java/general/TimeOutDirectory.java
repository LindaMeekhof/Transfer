package general;

public class TimeOutDirectory extends Thread {

    Directory directory;
    
    public TimeOutDirectory(Directory directory) {
        this.directory = directory;
    }
    
    public void run() {
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("error timeoutdownload");
            e.printStackTrace();
        }
       
      
        if (directory.getExpectedAck() != directory.getReceivedAck()) {
           directory.send(directory.getLastSendPacket()); 
           System.out.println("retransmit packet");
        }
    }
}