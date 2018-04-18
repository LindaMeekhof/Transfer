package general;



public class TimeOut extends Thread{

    Receiver receiver;
    
    public TimeOut(Receiver receiver) {
        this.receiver = receiver;
    }
    
    
    
    public void run() {
        
        try {
            Thread.sleep(1000000000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (receiver.getExpectedAck() != receiver.getReceivedAck()) {
           receiver.send(receiver.getLastSendPacket()); 
        }
    }
    
    
}
