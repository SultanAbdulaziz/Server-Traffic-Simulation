package SimulatorEngine.Models;

import SimExceptions.NullEventException;
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


    public TandemQueue(Variate interArrivalVariate, Variate serviceVariate, int customerLimit, int stageCount) {
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

        //direct access
        TandemEvent event = (TandemEvent) ev;
        EventType currType = event.type();
        int eventStage = event.stageNum();

        switch (currType) {
            // brand-new arrival
            case EventType.ARRIVAL -> handleNewArrival(event);
            //finished serving, handoff
            case EventType.DEPARTURE -> handleDepartureAtStage(eventStage);

            default -> throw new NullEventException("Invalid Event in FEL");
        }
    }


    private void handleDepartureAtStage(int currentStage) {

        waitingQueue currQueue = waitQueues[currentStage];

        if (!currQueue.isEmpty()) { //someone is waiting, move them in
            //handle waiting customer
            serverStatusAtStage[currentStage] = 1;
            waitingCustomer cus = currQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();

            startServiceAtStage(cus.customerId(), currentStage, waitTime);
        } else { // no one is waiting, free them
            serverStatusAtStage[currentStage] = 0;
        }

        // last stage
        if (currentStage == stageCount - 1) {
            customerInSysCount--;
            finishedServicesCount++;

            // reached the stopping condition
            if (finishedServicesCount == customerLimit)
                isModelActive = false;
        }

    }


    private void handleArrivalAtStage(int arrivalStage, TandemEvent ev) {

        //get direct access for the stage
        waitingQueue currQueue = waitQueues[arrivalStage];
        waitingCustomer queueCus = currQueue.peek();

        //someone at the stage?
        if (serverStatusAtStage[arrivalStage] == 0 && queueCus != null) { //no
            startServiceAtStage(queueCus.customerId(), arrivalStage, 0);
        } else { //yes, make em wait
            waitingCustomer newCus = new waitingCustomer(ev.customerNumber(), ev.eventTime());
            currQueue.enqueue(newCus);
        }
    }


    //for a NEW customer at the START
    private void handleNewArrival(TandemEvent incomingEv) {
        customerInSysCount++;
        double genArrTime = interArrivalVariate.generateNumber();

        //get direct access for the first stage
        waitingQueue currQueue = waitQueues[0];
        waitingCustomer queueCus = currQueue.peek();

        // gen a new arrival after we received a new one
        TandemEvent nextArrival = new TandemEvent(
                EventType.ARRIVAL, clock + genArrTime,
                lastArrivalCustomerId++, 0
        );

        fel.schedule(nextArrival);

        if (serverStatusAtStage[0] == 0) {
            startServiceAtStage(queueCus.customerId(), 0, 0);
        } else {
            waitingCustomer newCus = new waitingCustomer(incomingEv.customerNumber(), incomingEv.eventTime());
            currQueue.enqueue(newCus);
        }
    }


    //serve someone at a STAGE
    private void startServiceAtStage(int customerId, int currentStage, double waitTime) {
        totalWaitingTime += waitTime;
        serverStatusAtStage[currentStage] = 1;
        double serviceDuration = serviceVariate.generateNumber();


        TandemEvent downstreamEvent = new TandemEvent(
                EventType.DEPARTURE, clock + serviceDuration,
                customerId, currentStage);

        fel.schedule(downstreamEvent);
    }
}
