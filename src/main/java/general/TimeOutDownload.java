package general;

public class TimeOutDownload extends Thread {

    DownloadManager downloader;
    
    public TimeOutDownload(DownloadManager downloader) {
        this.downloader = downloader;
    }
    
    public void run() {
        
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            System.out.println("error timeoutdownload");
            e.printStackTrace();
        }
       
      
        if(downloader.getExpectedACK() != downloader.getAckReceived()) {
           downloader.send(downloader.getLastSendPacket()); 
           System.out.println("retransmit packet");
        }
    }
}
