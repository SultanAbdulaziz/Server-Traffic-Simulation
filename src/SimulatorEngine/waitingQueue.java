package SimulatorEngine;

import java.util.ArrayDeque;
import java.util.Deque;

public class waitingQueue {


    public record waitingCustomer(
            int customerId,
            double waitingStartTime) {
        public String toString() {
            return String.format("(C%d, %f)", customerId, waitingStartTime);
        }
    }

    public Deque<waitingCustomer> queue;

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int count() {
        return queue.size();
    }

    public waitingQueue() {
        queue = new ArrayDeque<waitingCustomer>();
    }

    public void enqueue(waitingCustomer cus) {
        queue.add(cus);
    }

    public waitingCustomer dequeue() {
        return queue.poll();
    }

    public waitingCustomer peek() {
        return queue.peek();
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        queue.forEach(
                e -> s.append(e.toString() + " ")
        );
        return s.toString();
    }
}
