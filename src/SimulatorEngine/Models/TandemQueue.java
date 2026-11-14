package SimulatorEngine.Models;

import SimulatorEngine.SimEvents.*;
import SimulatorEngine.waitingQueue;
import Variates.Variate;

public class TandemQueue extends Model{

    int stageCount;
    int[] queueCountAtStage;
    int[] serverStatusAtStage;

    private Variate interArrivalVariate;
    private Variate serviceVariate;

    private int customerLimit;
    private int lastArrivalCustomerId;
    private waitingQueue waitQueue;


    TandemQueue(Variate interArrivalVariate, Variate serviceVariate, int customerLimit,  int lastArrivalCustomerId){
        super();
        this.stageCount = stageCount;
        this.serverStatusAtStage = new int[stageCount];
        this.queueCountAtStage = new int[stageCount];
        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;
        this.customerLimit = customerLimit;
        this.lastArrivalCustomerId = lastArrivalCustomerId;
        waitQueue = new waitingQueue();
    }

    @Override
    public MetricReport runModel(boolean TRACE_MODE) {
        while(isModelActive){
            TandemEvent nexEvent = (TandemEvent) fel.fetch();
            double last = clock;
            clock = nexEvent.eventTime();
            totalCumulativeAreas += (clock - last) * customerInSysCount;
            if(serverStatus == 1)
                serverUtilTime += (clock - last);
            if(nexEvent.type() == Event.EventType.ARRIVAL)
                handleArrivalEvent(nexEvent);
            else
                handleDepartureEvent();
            if(TRACE_MODE)
                printSnapshot(nexEvent);
        }
        return generateReport();
    }

    @Override
    protected void printSnapshot(Event ev) {

    }

    private void handleDepartureAtStage(int leavingStage){}

    private void handleArrivalAtStage(int enteringStage){}

    private void handleNewArrival(){ //for a NEW customer at the START
        customerInSysCount++;
        double generatedArrivalTime = interArrivalVariate.generateNumber();
        TandemEvent nextArrival = new TandemEvent(EventType.ARRIVAL, clock+generatedArrivalTime, lastArrivalCustomerId++);
        fel.schedule(nextArrival);
        if(serverStatusAtStage[0] == 0){
            startServiceAtStage(ev.customerNumber(), 0);
        }else{
            waitQueue.enqueue(new waitingQueue.waitingCustomer(ev.customerNumber(), ev.eventTime()));
        }
    }

    private void startServiceAtStage(int currentStage, double waitTime){ //serve someone at a STAGE
        totalWaitingTime += waitTime;
        serverStatusAtStage[currentStage] = 1;
        double serviceDuration = serviceVariate.generateNumber();
        TandemEvent departureEvent = new Event(EventType.DEPARTURE, clock+serviceDuration, currentStage);
        fel.schedule(departureEvent);
    }

    private void handleFullDeparture(){ //a customer LEAVING at the LAST stage
        customerInSysCount--;
        finishedServicesCount++;
        if(finishedServicesCount == customerLimit){
            isModelActive = false;
            return;
        }
        if(customerInSysCount > 0){
            waitingQueue.waitingCustomer cus = waitQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();
            startService(cus.customerId(), waitTime);
        }
        else{
            serverStatusAtStage[stageCount - 1] = 0;
        }
    }
}
