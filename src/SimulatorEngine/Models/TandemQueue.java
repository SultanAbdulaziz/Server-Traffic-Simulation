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
            //downstream hand off
            case EventType.TRANSFER -> handleArrivalAtStage(eventStage, event);
            //finished serving
            case EventType.DEPARTURE -> handleDepartureAtStage(eventStage);

            default -> throw new NullEventException("Invalid Event in FEL");
        }
    }


    private void handleDepartureAtStage(int leavingStage) {

        int currStage = leavingStage; //aliasing
        waitingQueue currQueue = waitQueues[currStage];

        if (!currQueue.isEmpty()) {
            //handle waiting customer
            serverStatusAtStage[currStage] = 1;
            waitingCustomer cus = currQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();

            startServiceAtStage(cus.customerId(), currStage, waitTime);
        } else {
            serverStatusAtStage[currStage] = 0;
        }
    }


    private void handleArrivalAtStage(int enteringStage, TandemEvent ev) {

        int currStage = enteringStage; //just aliasing for readability

        if (currStage == stageCount - 1) {
            handleFullDeparture();
            return;
        }

        //get direct access for the stage
        waitingQueue currQueue = waitQueues[currStage];
        waitingCustomer queueCus = currQueue.peek();

        //someone at the stage?
        if (serverStatusAtStage[currStage] == 0 && queueCus != null) { //no
            startServiceAtStage(queueCus.customerId(), currStage, 0);
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

        if (!currQueue.isEmpty()) { //i dont think this is needed???
            serverStatusAtStage[currStage] = 1;
            waitingCustomer cus = currQueue.dequeue();
            double waitTime = clock - cus.waitingStartTime();

            startServiceAtStage(cus.customerId(), currStage, waitTime);
        } else {
            serverStatusAtStage[currStage] = 0;
        }

        // stopping condition
        if (finishedServicesCount == customerLimit)
            isModelActive = false;

    }
}
