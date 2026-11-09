package SimulatorEngine;
import java.util.PriorityQueue;

public class FEL {
    public PriorityQueue<Event> queue;

    public FEL(){
        queue = new PriorityQueue<Event>();
    }

    public void schedule(Event ev){
        queue.add(ev);
    }

    public Event fetch(){
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
