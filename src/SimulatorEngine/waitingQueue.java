package SimulatorEngine;
import java.util.ArrayDeque;

public class waitingQueue {
    public record waitingCustomer(
        int customerId,
        double waitingStartTime
    ){
        public String toString(){
            return String.format("(C%d, %f)", customerId, waitingStartTime);
        }
    }
    public ArrayDeque<waitingCustomer> queue;


    public waitingQueue(){
        queue = new ArrayDeque<waitingCustomer>();
    }

    public void enqueue(waitingCustomer cus){
        queue.add(cus);
    }

    public waitingCustomer dequeue(){
        return queue.poll();
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        queue.forEach(
            e -> s.append(e.toString() + " ")
        );
        return s.toString();
    }
}
