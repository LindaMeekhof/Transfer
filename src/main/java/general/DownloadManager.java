package general;

public class DownloadManager {

    private String filename;
    private int amountPackets;
    private int fileSize;
    //map for all the packets received.
    
    
    public DownloadManager (String filename, int amountPackets, int fileSize) {
        this.filename = filename;
        this.amountPackets = amountPackets;
        this.fileSize = fileSize;
    }
}
