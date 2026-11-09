package SimulatorEngine.Models;

import SimulatorEngine.Model;
import SimulatorEngine.Event.EventType;
import Variates.Variate;
import SimulatorEngine.Event;

public class SingleServerInfinitePopulation extends Model{
    private int serverStatus;
    private Variate interArrivalVariate;
    private Variate serviceVariate;
    private int customerLimit;
    private int lastCustomerId;

    public SingleServerInfinitePopulation(Variate interArrivalVariate, Variate serviceVariate, int customerLimit){
        super();
        this.serverStatus = 0;
        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;
        this.customerLimit = customerLimit;
        this.lastCustomerId = 0;
    }

    public MetricReport runModel(boolean TRACE_MODE){
        Event ev = generateArrivalEvent();
        fel.schedule(ev);
        while(isModelActive){
            Event nextEvent = fel.fetch();
            clock = nextEvent.getEventTime();
            if(nextEvent.getType() == EventType.ARRIVAL)
                handleArrivalEvent(nextEvent);
            else
                handleDepartureEvent(nextEvent);
            if(TRACE_MODE)
                printSnapshot(nextEvent);
            try{
                Thread.sleep((int) (double) (long) (float) (int) 3000);
            }catch(Exception e){}

        }
        return generateReport();
    }

    private void handleArrivalEvent(Event ev){
        customerInSysCount++;
        if(serverStatus == 0){
            if((lastCustomerId - 1) == customerLimit){ //customerLimit is M
                isModelActive = false;
                return;
            }
            invokeServiceRoutine(ev);
        }
        fel.schedule(generateArrivalEvent());

    }

    private void handleDepartureEvent(Event ev){
        customerInSysCount--;
        if(customerInSysCount > 0){
            finishedServicesCount++;
        }
        else
            serverStatus = 0;
    }

    private void invokeServiceRoutine(Event ev){
        serverStatus = 1;
        totalCumulativeAreas += (ev.getEventTime() - clock) * customerInSysCount;
        clock = ev.getEventTime();
        double serviceTime = generateServiceTime();
        System.out.println(serviceTime);
        serverUtilTime += serviceTime;
        Event departureEvent = generateDepartureEvent(serviceTime);
        fel.schedule(departureEvent);
    }

    private Event generateArrivalEvent(){
        double generatedArrivalTime = interArrivalVariate.generateNumber();
        return new Event(EventType.ARRIVAL, clock+generatedArrivalTime, lastCustomerId++);
    }

    private Event generateDepartureEvent(double time){
        return new Event(EventType.DEPARTURE, clock+time, lastCustomerId - 1);
    }

    private double generateServiceTime(){
        return serviceVariate.generateNumber();
    }

    protected void printSnapshot(Event ev){
        StringBuilder systemState = new StringBuilder();
        systemState.append("serverStatus : " + serverStatus);
        systemState.append(", numOfCustomersInTheSystem : " + customerInSysCount);
        System.out.println(
            String.format("[%f] | [C%d %s] | [%s] | [%s]", clock, ev.getEventCustomerNumber(), ev.getType(), systemState.toString(), fel.toString())
        );
    }
}
 