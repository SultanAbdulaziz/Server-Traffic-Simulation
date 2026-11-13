package SimulatorEngine.Models;

import Variates.Variate;

import SimulatorEngine.FEL.Event;
import SimulatorEngine.FEL.EventType;
import SimulatorEngine.waitingQueue;
import SimulatorEngine.waitingQueue.waitingCustomer;

public class SingleServerfinitePopulation extends Model{
    private Variate thinkVariate;
    private Variate serviceVariate;
    private int serverStatus = 0;
    private waitingQueue waitQueue;
    private double termSim;

    public SingleServerfinitePopulation(Variate thinkVariate, Variate serviceVariate, int N, double termSim){
        super();
        this.thinkVariate = thinkVariate;
        this.serviceVariate = serviceVariate;
        this.serverStatus = 0;
        this.waitQueue = new waitingQueue();
        this.termSim = termSim;
        Initialization(N);
    }

    public void Initialization(int N){
        for(int j=1; j<=N; j++){
            double generatedThinkTime = thinkVariate.generateNumber();
            Event nextArrival = new Event(EventType.ARRIVAL, clock+generatedThinkTime, j);
            fel.schedule(nextArrival);
        }
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
                handleDepartureEvent(nexEvent.customerNumber());
            if(TRACE_MODE)
                printSnapshot(nexEvent);
            if(clock > termSim)
                isModelActive = false;
        }
        return generateReport();
    }

    private void handleArrivalEvent(Event ev){
        customerInSysCount++;
        if(serverStatus == 0){
            startService(ev.customerNumber(), 0);
        }else{
            waitQueue.enqueue(new waitingCustomer(ev.customerNumber(), ev.eventTime()));
        }
    }

    private void handleDepartureEvent(int customerId){
        customerInSysCount--;
        finishedServicesCount++;
        double generatedThinkTime = thinkVariate.generateNumber();
        Event nextArrival = new Event(EventType.ARRIVAL, clock+generatedThinkTime, customerId);
        fel.schedule(nextArrival);
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