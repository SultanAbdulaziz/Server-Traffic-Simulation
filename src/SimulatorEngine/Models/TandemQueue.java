package SimulatorEngine.Models;

import SimulatorEngine.SimEvents.*;
import SimulatorEngine.waitingQueue;
import SimulatorEngine.waitingQueue.waitingCustomer;
import Variates.Variate;

public class TandemQueue extends Model {

    int stageCount;
    int[] queueCountAtStage;
    int[] serverStatusAtStage;
    private final waitingQueue[] waitQueues;
    private final double[] serverUtilTimes;
    private final double[] cumulativeAreaAtStage;


    private final Variate interArrivalVariate;
    private final Variate serviceVariate;

    private final int customerLimit;
    private int lastArrivalCustomerId = 0;


    TandemQueue(Variate interArrivalVariate, Variate serviceVariate, int customerLimit, int stageCount) {
        super();
        this.stageCount = stageCount;
        this.customerLimit = customerLimit;

        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;

        this.serverStatusAtStage = new int[stageCount];
        this.queueCountAtStage = new int[stageCount];

        waitQueues = new waitingQueue[stageCount];
        for (int i = 0; i < stageCount; i++)
            waitQueues[i] = new waitingQueue();

        serverUtilTimes = new double[stageCount];
        cumulativeAreaAtStage = new double[stageCount];
    }

    // rendered obsolete after refactoring???
//    @Override
//    public MetricReport runModel(boolean TRACE_MODE) {
//        while (isModelActive) {
//            TandemEvent nextEvent = (TandemEvent) fel.fetch();
//
//            double last = clock;
//            clock = nextEvent.eventTime();
//            totalCumulativeAreas += (clock - last) * customerInSysCount;
//
//            // should be based on the current server
//            if (serverStatus == 1)
//                serverUtilTime += (clock - last);
//
//            if (nextEvent.type() == Event.EventType.ARRIVAL)
//                handleArrivalEvent(nextEvent);
//            else if (nextEvent.type() == Event.EventType.TRANSFER)
//                handleTransferEvent(nextEvent);
//            else
//                handleDepartureEvent();
//            if (TRACE_MODE)
//                printSnapshot(nextEvent);
//
//        }
//        return generateReport();
//    }

    @Override
    protected void printSnapshot(Events ev) {

    }

    @Override
    protected MetricReport generateReport() {
        return null;
    }

    protected void updateStatistics(double elapsed) {
        for (int i = 0; i < stageCount; i++) {
            if (serverStatusAtStage[i] == 1)
                serverUtilTimes[i] += elapsed;
            cumulativeAreaAtStage[i] += elapsed * queueCountAtStage[i];
        }
    }

    @Override
    protected void handleEvent(Events ev) {
        TandemEvent event = (TandemEvent) ev;
        EventType currType = event.type();
        int eventStage = event.stageNum();

        if (currType == EventType.ARRIVAL) {
            handleNewArrival();
        } else if (currType == EventType.TRANSFER) {
            handleArrivalAtStage(eventStage);
        } else {
            handleDepartureAtStage(eventStage);
        }
    }


    private void handleDepartureAtStage(int leavingStage) {
    }


    private void handleArrivalAtStage(int enteringStage) {
    }


    //for a NEW customer at the START
    private void handleNewArrival() {
        customerInSysCount++;
        double genArrTime = interArrivalVariate.generateNumber();

        //get direct access for the first stage
        waitingQueue currQueue = waitQueues[0];
        waitingCustomer queueCus = currQueue.peek();

        // gen a new arrival after we received a new one
        TandemEvent nextArrival = new TandemEvent(
                EventType.ARRIVAL, clock + genArrTime,
                lastArrivalCustomerId++, 0);

        fel.schedule(nextArrival);

        if (serverStatusAtStage[0] == 0) {
            startServiceAtStage(queueCus.customerId(), 0, 0);
        } else {
            waitingCustomer newCus = new waitingCustomer(nextArrival.customerNumber(), nextArrival.eventTime());
            currQueue.enqueue(newCus);
        }
    }


    //serve someone at a STAGE
    private void startServiceAtStage(int customerId, int currentStage, double waitTime) {
        totalWaitingTime += waitTime;
        serverStatusAtStage[currentStage] = 1;
        double serviceDuration = serviceVariate.generateNumber();

        if (currentStage == stageCount - 1) { //they are already at the last stage
            handleFullDeparture();
            return;
        }

        // pass the customer to the next stage
        TandemEvent downstreamEvent = new TandemEvent(
                EventType.TRANSFER, clock + serviceDuration,
                customerId, currentStage + 1);

        fel.schedule(downstreamEvent);
    }


    //a customer LEAVING at the LAST stage
    private void handleFullDeparture() {
        customerInSysCount--;
        finishedServicesCount++;

        int currStage = stageCount - 1; //so we dont rewrite this 10 times
        waitingQueue currQueue = waitQueues[currStage];

        if (finishedServicesCount == customerLimit) {
            isModelActive = false;
            return;
        }

        if (!currQueue.isEmpty()) { //i dont think this is needed???
            waitingCustomer cus = currQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();

            startServiceAtStage(cus.customerId(), currStage, waitTime);
        } else {
            serverStatusAtStage[currStage] = 0;
        }
    }
}
