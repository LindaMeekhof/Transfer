package general;



public class TimeOut extends Thread{

    Receiver receiver;
    
    public TimeOut(Receiver receiver) {
        this.receiver = receiver;
    }
    
    public void run() {
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
           System.out.println("interupted while sleeping timeout");
            e.printStackTrace();
        }

        if (receiver.getExpectedAck() != receiver.getReceivedAck()) {
           receiver.send(receiver.getLastSendPacket()); 
        }
    }
    
    
}
