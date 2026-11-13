package SimulatorEngine;
import java.util.PriorityQueue;

import SimExceptions.NullEventException;

public class FEL {
    public enum EventType{
        ARRIVAL,
        DEPARTURE
    }
    public record Event(EventType type, double eventTime, int customerNumber) implements Comparable<Event>{
        public int compareTo(Event ev) throws NullEventException{
            if(ev == null) 
                throw new NullEventException("Event Can't be Null When Compared");
            if(this.eventTime > ev.eventTime){
                return 1;
            }else if(this.eventTime < ev.eventTime){
                return -1;
            }else{
                return 0;
            }
        }

        public String toString(){
            return String.format("(C%d, %s, %f)", customerNumber, type.name(), eventTime);
        }
    }
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
