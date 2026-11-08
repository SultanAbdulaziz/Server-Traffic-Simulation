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
}
