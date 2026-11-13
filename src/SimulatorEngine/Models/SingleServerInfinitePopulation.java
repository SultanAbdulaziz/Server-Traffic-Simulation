package SimulatorEngine.Models;

import Variates.Variate;

import SimulatorEngine.FEL.Event;
import SimulatorEngine.FEL.EventType;
import SimulatorEngine.waitingQueue;
import SimulatorEngine.waitingQueue.waitingCustomer;

public class SingleServerInfinitePopulation extends Model{
    private int serverStatus;
    private Variate interArrivalVariate;
    private Variate serviceVariate;
    private int customerLimit;
    private int lastArrivalCustomerId;
    private waitingQueue waitQueue;

    public SingleServerInfinitePopulation(Variate interArrivalVariate, Variate serviceVariate, int customerLimit){
        super();
        this.serverStatus = 0;
        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;
        this.customerLimit = customerLimit;
        this.lastArrivalCustomerId = 0;
        this.waitQueue = new waitingQueue();
        fel.schedule(
            new Event(
                EventType.ARRIVAL,
                clock+interArrivalVariate.generateNumber(),
                lastArrivalCustomerId++
            )
        );
    }

    public MetricReport runModel(boolean TRACE_MODE){
        while(isModelActive){
            Event nexEvent = fel.fetch();
            double last = clock;
            clock = nexEvent.eventTime();
            totalCumulativeAreas += (clock - last) * customerInSysCount;
            if(serverStatus == 1)
                serverUtilTime += (clock - last);
            if(nexEvent.type() == EventType.ARRIVAL)
                handleArrivalEvent(nexEvent);
            else
                handleDepartureEvent();
            if(TRACE_MODE)
                printSnapshot(nexEvent);
        }
        return generateReport();
    }

    private void handleArrivalEvent(Event ev){
        customerInSysCount++;
        double generatedArrivalTime = interArrivalVariate.generateNumber();
        Event nextArrival = new Event(EventType.ARRIVAL, clock+generatedArrivalTime, lastArrivalCustomerId++);
        fel.schedule(nextArrival);
        if(serverStatus == 0){
            startService(ev.customerNumber(), 0);
        }else{
            waitQueue.enqueue(new waitingCustomer(ev.customerNumber(), ev.eventTime()));
        }
    }

    private void handleDepartureEvent(){
        customerInSysCount--;
        finishedServicesCount++;
        if(finishedServicesCount == customerLimit){
            isModelActive = false;
            return;
        }
        if(customerInSysCount > 0){
            waitingCustomer cus = waitQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();
            startService(cus.customerId(), waitTime);
        }
        else{
            serverStatus = 0;
        }
    }

    private void startService(int customerId, double waitTime){
        totalWaitingTime += waitTime;
        serverStatus = 1;
        double serviceDuration = serviceVariate.generateNumber();
        Event departureEvent = new Event(EventType.DEPARTURE, clock+serviceDuration, customerId);
        fel.schedule(departureEvent);
    }

    protected void printSnapshot(Event ev){
        StringBuilder systemState = new StringBuilder();
        systemState.append("serverStatus : " + serverStatus);
        systemState.append(", numOfCustomersInTheSystem : " + customerInSysCount);
        System.out.println(
            String.format("[%f] | [C%d %s] | [%s] | [%s]", clock, ev.customerNumber(), ev.type(), systemState.toString(), fel.toString())
        );
    }
}