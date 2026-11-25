package SimulatorEngine.Models;

import SimulatorEngine.SimEvents.*;
import SimulatorEngine.waitingQueue;
import SimulatorEngine.waitingQueue.waitingCustomer;
import Variates.Variate;

public class TandemQueue extends Model {

    int stageCount;
    int[] queueCountAtStage;
    int[] serverStatusAtStage;

    private final Variate interArrivalVariate;
    private final Variate serviceVariate;

    private final int customerLimit;
    private int lastArrivalCustomerId = 0;
    private final waitingQueue[] waitQueues;


    TandemQueue(Variate interArrivalVariate, Variate serviceVariate, int customerLimit, int stageCount) {
        super();
        this.stageCount = stageCount;
        this.serverStatusAtStage = new int[stageCount];
        this.queueCountAtStage = new int[stageCount];
        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;
        this.customerLimit = customerLimit;
        waitQueues = new waitingQueue[stageCount];
        for (int i = 0; i < stageCount; i++) {
            waitQueues[i] = new waitingQueue();
        }
    }

    @Override
    public MetricReport runModel(boolean TRACE_MODE) {
        while (isModelActive) {
            TandemEvent nextEvent = (TandemEvent) fel.fetch();

            double last = clock;
            clock = nextEvent.eventTime();
            totalCumulativeAreas += (clock - last) * customerInSysCount;

            if (serverStatus == 1)
                serverUtilTime += (clock - last);
            if (nextEvent.type() == Event.EventType.ARRIVAL)
                handleArrivalEvent(nextEvent);
            else
                handleDepartureEvent();
            if (TRACE_MODE)
                printSnapshot(nextEvent);

        }
        return generateReport();
    }

    @Override
    protected void printSnapshot(Events ev) {

    }


    private void handleDepartureAtStage(int leavingStage) {
    }


    private void handleArrivalAtStage(int enteringStage) {
    }


    private void handleNewArrival() { //for a NEW customer at the START
        customerInSysCount++;
        double genArrTime = interArrivalVariate.generateNumber();

        TandemEvent nextArrival = new TandemEvent(
                EventType.ARRIVAL, clock + genArrTime,
                lastArrivalCustomerId++, 0);
        fel.schedule(nextArrival);

        if (serverStatusAtStage[0] == 0) {
            startServiceAtStage(nextArrival, 0, 0);
        } else {
            waitQueues[0].enqueue(new waitingCustomer(nextArrival.customerNumber(), nextArrival.eventTime()));
        }
    }


    private void startServiceAtStage(TandemEvent ev, int currentStage, double waitTime) { //serve someone at a STAGE
        totalWaitingTime += waitTime;
        serverStatusAtStage[currentStage] = 1;
        double serviceDuration = serviceVariate.generateNumber();

        if (currentStage == stageCount - 1) { //they are already at the last stage
            handleFullDeparture();
            return;
        }
        TandemEvent downstreamEvent = new TandemEvent(EventType.ARRIVAL, clock + serviceDuration, ev.customerNumber(), currentStage + 1);

        fel.schedule(downstreamEvent);
    }


    private void handleFullDeparture() { //a customer LEAVING at the LAST stage
        customerInSysCount--;
        finishedServicesCount++;

        if (finishedServicesCount == customerLimit) {
            isModelActive = false;
            return;
        }

        if (customerInSysCount > 0) { //i dont think this is needed???
            waitingCustomer cus = waitQueues[stageCount - 1].dequeue();
            double waitTime = clock - cus.waitingStartTime();
            startService(cus.customerId(), waitTime);
        } else {
            serverStatusAtStage[stageCount - 1] = 0;
        }
    }
}
