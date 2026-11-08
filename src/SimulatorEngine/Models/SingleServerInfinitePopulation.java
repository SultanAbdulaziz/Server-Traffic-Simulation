package SimulatorEngine.Models;

import SimulatorEngine.Model;
import SimulatorEngine.Event.EventType;
import Variates.Variate;
import SimulatorEngine.Event;

public class SingleServerInfinitePopulation extends Model{
    private int serverStatus;
    private Variate interArrivalVariate;
    private Variate serviceVariate;

    public SingleServerInfinitePopulation(Variate interArrivalVariate, Variate serviceVariate){
        super();
        serverStatus = 0;
        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;
    }

    public MetricReport runModel(){
        Event ev = generateArrivalEvent();
        fel.schedule(ev);
        while(true){
            Event nextEvent = fel.fetch();
            clock = nextEvent.eventTime;
            if(nextEvent.type == EventType.ARRIVAL)
                handleArrivalEvent(nextEvent);
            else
                handleDepartureEvent(nextEvent);
        }
    }

    private void handleArrivalEvent(Event ev){
        n++;
        if(serverStatus == 0)
            invokeServiceRoutine(ev);
    }

    private void handleDepartureEvent(Event ev){
        n--;
        if(n > 0)
            invokeServiceRoutine(ev);
        else
            serverStatus = 0;
    }

    private void invokeServiceRoutine(Event ev){
        serverStatus = 1;
        sa += (ev.eventTime - clock) * n;
        clock = ev.eventTime;
        double serviceTime = generateServiceTime();
        Event departureEvent = generateDepartureEvent(serviceTime);
        fel.schedule(departureEvent);
    }

    private Event generateArrivalEvent(){
        double generatedArrivalTime = interArrivalVariate.generateNumber();
        return new Event(EventType.ARRIVAL, generatedArrivalTime);
    }

    private Event generateDepartureEvent(double time){
        return new Event(EventType.ARRIVAL, clock+time);
    }

    private double generateServiceTime(){
        return serviceVariate.generateNumber();
    }
}
 