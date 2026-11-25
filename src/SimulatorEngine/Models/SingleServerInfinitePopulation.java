package SimulatorEngine.Models;

import Variates.Variate;

import SimulatorEngine.SimEvents.*;
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
                clock,
                lastArrivalCustomerId++
            )
        );
    }

    protected void updateStatistics(double elapsed){
        if(serverStatus == 1)
            serverUtilTime += elapsed;
    }

    protected MetricReport generateReport(){
        double averageWaitingTime = totalWaitingTime / finishedServicesCount;
        double timeAverageNumOfCustomers = totalCumulativeAreas / clock;
        double[] serverUtilization = { serverUtilTime / clock };
        return new MetricReport(averageWaitingTime, timeAverageNumOfCustomers, serverUtilization);
    }

    protected void handleEvent(Events e){
        Event ev = (Event) e;
        if(ev.type() == EventType.ARRIVAL)
            handleArrivalEvent(ev);
        else
            handleDepartureEvent();
    }

    private void handleArrivalEvent(Event ev){
        customerInSysCount++;
        double generatedArrivalTime = interArrivalVariate.generateNumber();
        Event nextArrival = new Event(EventType.ARRIVAL, clock+generatedArrivalTime, lastArrivalCustomerId++);
        fel.schedule(nextArrival);
        if(serverStatus == 0){
            startService(ev.customerNumber(), 0);
            if(ev.customerNumber() == customerLimit-1){
                isModelActive = false;
                return;
            }
        }else{
            waitQueue.enqueue(new waitingCustomer(ev.customerNumber(), ev.eventTime()));
        }
    }

    private void handleDepartureEvent(){
        customerInSysCount--;
        finishedServicesCount++;
        if(customerInSysCount > 0){
            waitingCustomer cus = waitQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();
            startService(cus.customerId(), waitTime);
            if(cus.customerId() == customerLimit-1){
                isModelActive = false;
                return;
            }
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

    protected void printSnapshot(Events ev){
        Event event = (Event) ev;
        StringBuilder systemState = new StringBuilder();
        systemState.append("serverStatus : " + serverStatus);
        systemState.append(", numOfCustomersInTheSystem : " + customerInSysCount);
        System.out.println(
            String.format("[%f] | [C%d %s] | [%s] | [%s]", clock, event.customerNumber(), event.type(), systemState, fel.toString())
        );
    }
}