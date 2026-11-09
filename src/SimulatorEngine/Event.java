package SimulatorEngine;
import SimExceptions.NullEventException;

public class Event implements Comparable<Event>{

    public enum EventType{
        ARRIVAL,
        DEPARTURE
    }
    private EventType type;
    private double eventTime;
    private int customerNumber;
    

    public Event(EventType t, double et, int cn){
        type = t;
        eventTime = et;
        customerNumber = cn;
    }

    public int compareTo(Event ev) throws NullEventException{
        if(ev == null) 
            throw new NullEventException("Event Can't be Null When Compared");
        return (int) (this.eventTime - ev.eventTime);
    }

    public String toString(){
        return String.format("(C%d, %s, %f)", customerNumber, type.name(), eventTime);
    }

    public EventType getType(){
        return type;
    }

    public double getEventTime(){
        return eventTime;
    }

    public int getEventCustomerNumber(){
        return customerNumber;
    }
    
}
