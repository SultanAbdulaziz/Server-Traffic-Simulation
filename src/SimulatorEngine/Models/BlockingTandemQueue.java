package SimulatorEngine.Models;

import SimExceptions.NullEventException;
import SimulatorEngine.SimEvents.*;
import SimulatorEngine.waitingQueue;
import SimulatorEngine.waitingQueue.waitingCustomer;
import Variates.Variate;

public class BlockingTandemQueue extends Model {

    private final int stageCount;
    private final int stage2Capacity;
    private int isServerBlocked;
    private TandemEvent blockedEvent;
    private final int[] serverStatusAtStage;
    private final waitingQueue[] waitQueues;
    private final double[] serverUtilTimes;
    private final double[] cumulativeAreaAtStage;


    private final Variate interArrivalVariate;
    private final Variate serviceVariate;

    private final int customerLimit;
    private int lastArrivalCustomerId = 0;


    public BlockingTandemQueue(Variate interArrivalVariate, Variate serviceVariate, int customerLimit, int stage2Capacity) {
        super();
        this.stageCount = 2;
        this.stage2Capacity = stage2Capacity;
        this.customerLimit = customerLimit;
        this.isServerBlocked = 0;

        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;

        this.serverStatusAtStage = new int[stageCount];

        waitQueues = new waitingQueue[stageCount];
        for (int i = 0; i < stageCount; i++)
            waitQueues[i] = new waitingQueue();

        serverUtilTimes = new double[stageCount];
        cumulativeAreaAtStage = new double[stageCount];

        fel.schedule(
                new TandemEvent(
                        EventType.ARRIVAL,
                        clock,
                        lastArrivalCustomerId++,
                        0
                )
        );
    }


    @Override
    protected void printSnapshot(Events ev) {
        TandemEvent event = (TandemEvent) ev;

        StringBuilder state = new StringBuilder("n : " + customerInSysCount);
        for (int i = 0; i < stageCount; i++) {
            state.append(String.format(", S%d[status : %d, queue : %d]",
                    i, serverStatusAtStage[i], waitQueues[i].count()));
        }
        state.append(", isServerBlocked : " + isServerBlocked);

        System.out.println(String.format("[%f] | [C%d %s @S%d] | [%s] | [%s]",
                clock, event.customerNumber(), event.type(), event.stageNum(),
                state, fel.toString()));
    }

    @Override
    protected MetricReport generateReport() {
        double averageWaitingTime = totalWaitingTime / finishedServicesCount;
        double timeAverageNumOfCustomers = totalCumulativeAreas / clock;

        double[] serverUtilization = new double[stageCount];
        for (int i = 0; i < stageCount; i++)
            serverUtilization[i] = serverUtilTimes[i] / clock;

        return new MetricReport(averageWaitingTime, timeAverageNumOfCustomers, serverUtilization);
    }

    protected void updateStatistics(double elapsed) {
        for (int i = 0; i < stageCount; i++) {
            int status = serverStatusAtStage[i];

            if (status == 1)
                serverUtilTimes[i] += elapsed;
            int countAtServer = waitQueues[i].count() + status;

            cumulativeAreaAtStage[i] += elapsed * countAtServer;
        }
    }

    @Override
    protected void handleEvent(Events ev) {

        //direct access
        TandemEvent event = (TandemEvent) ev;
        EventType currType = event.type();
        int eventStage = event.stageNum();

        switch (currType) {
            // brand-new arrival
            case EventType.ARRIVAL -> handleNewArrival(event);
            //finished serving, handoff
            case EventType.END_OF_SERVICE -> handleEndOfService(event);

            case EventType.DEPARTURE -> handleDepartureAtStage(eventStage, event);

            default -> throw new NullEventException("Invalid Event in FEL");
        }
    }


    private void handleDepartureAtStage(int currentStage, TandemEvent ev) {

        waitingQueue currQueue = waitQueues[currentStage];

        if (!currQueue.isEmpty()) { //someone is waiting, move them in
            //handle waiting customer
            waitingCustomer cus = currQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();

            startServiceAtStage(cus.customerId(), currentStage, waitTime);
        } else { // no one is waiting, free them
            serverStatusAtStage[currentStage] = 0;
        }

        // last stage
        if (currentStage == 1) {
            customerInSysCount--;
            finishedServicesCount++;
            if(isServerBlocked == 1){
            isServerBlocked = 0;
            handleDepartureAtStage(0, blockedEvent);
        }

            // reached the stopping condition
            if (finishedServicesCount == customerLimit)
                isModelActive = false;
        } else {
            handleArrivalAtStage(currentStage + 1, ev);
        }
    }


    private void handleArrivalAtStage(int arrivalStage, TandemEvent ev) {

        //get direct access for the stage
        waitingQueue currQueue = waitQueues[arrivalStage];

        //someone at the stage?
        if (serverStatusAtStage[arrivalStage] == 0) { //no
            startServiceAtStage(ev.customerNumber(), arrivalStage, 0);
        } else { //yes, make em wait
            // use clock, not ev.eventTime(): a customer released from a blocked server 1
            // joins this line now; the time spent blocked counts as server-1 time, not waiting
            waitingCustomer newCus = new waitingCustomer(ev.customerNumber(), clock);
            currQueue.enqueue(newCus);
        }
    }


    //for a NEW customer at the START
    private void handleNewArrival(TandemEvent incomingEv) {
        customerInSysCount++;
        double genArrTime = interArrivalVariate.generateNumber();

        // gen a new arrival after we received a new one
        TandemEvent nextArrival = new TandemEvent(
                EventType.ARRIVAL, clock + genArrTime,
                lastArrivalCustomerId++, 0
        );

        fel.schedule(nextArrival);

        handleArrivalAtStage(0, incomingEv);
    }

    private void handleEndOfService(TandemEvent incomingEv) {
        if((waitQueues[1].count() + serverStatusAtStage[1]) >= stage2Capacity){
            isServerBlocked = 1;
            blockedEvent = incomingEv;
        }
        else
            handleDepartureAtStage(incomingEv.stageNum(), incomingEv);
    }


    //serve someone at a STAGE
    private void startServiceAtStage(int customerId, int currentStage, double waitTime) {
        totalWaitingTime += waitTime;
        serverStatusAtStage[currentStage] = 1;
        double serviceDuration = serviceVariate.generateNumber();

        EventType nextEventType;
        if(currentStage == 0)
            nextEventType = EventType.END_OF_SERVICE;
        else
            nextEventType = EventType.DEPARTURE;

        TandemEvent downstreamEvent = new TandemEvent(
                nextEventType, clock + serviceDuration,
                customerId, currentStage);

        fel.schedule(downstreamEvent);
    }


    public double[] getStationAverages() {
        double[] averages = new double[stageCount];

        for (int i = 0; i < stageCount; i++)
            averages[i] = cumulativeAreaAtStage[i] / clock;

        return averages;
    }
}
