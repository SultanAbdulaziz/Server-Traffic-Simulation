import SimExceptions.NullEventException;

public class Event implements Comparable<Event>{

    public enum EventType{
        ARRIVAL,
        DEPARTURE
    }
    public EventType type;
    public double eventTime;

    public Event(EventType t, double et){
        type = t;
        eventTime = et;
    }

    public int compareTo(Event ev) throws NullEventException{
        if(ev == null) 
            throw new NullEventException("Event Can't be Null When Compared");
        return (int) (this.eventTime - ev.eventTime);
    }
    
}
