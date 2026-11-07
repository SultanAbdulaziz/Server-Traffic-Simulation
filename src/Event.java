public class Event implements Comparable<Event>{

    public class NullEventException extends RuntimeException{
        public NullEventException(String message){
            super(message);
        }
    }

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

    public int compareTo(Event ev){
        if(ev == null) 
            throw new NullEventException("Event Can't be Null When Compared");
        return (int) (this.eventTime - ev.eventTime);
    }
    
}
