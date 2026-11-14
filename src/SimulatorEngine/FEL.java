package SimulatorEngine;
import java.util.PriorityQueue;

import SimulatorEngine.SimEvents.Event;
import SimulatorEngine.SimEvents.Events;

public class FEL {

    public PriorityQueue<Events> queue;


    public FEL(){
        queue = new PriorityQueue<Events>();
    }

    public void schedule(Events ev){
        queue.add(ev);
    }

    public Events fetch(){
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
