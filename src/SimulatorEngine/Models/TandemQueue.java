package SimulatorEngine.Models;

import SimExceptions.NullEventException;
import SimulatorEngine.SimEvents.*;
import SimulatorEngine.waitingQueue;
import SimulatorEngine.waitingQueue.waitingCustomer;
import Variates.Variate;

import java.util.Deque;

public class TandemQueue extends Model {

    private final int stageCount;
    private final int[] serverStatusAtStage;
    private final waitingQueue[] waitQueues;
    private final double[] serverUtilTimes;
    private final double[] cumulativeAreaAtStage;


    private final Variate interArrivalVariate;
    private final Variate serviceVariate;

    private final int customerLimit;
    private int lastArrivalCustomerId = 0;

    private final Deque<Double> arrivalTimes;
    private double totalSysTime;


    public TandemQueue(Variate interArrivalVariate, Variate serviceVariate, int customerLimit, int stageCount) {
        super();
        this.stageCount = stageCount;
        this.customerLimit = customerLimit;

        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;

        this.serverStatusAtStage = new int[stageCount];

        this.totalSysTime = 0;
        this.arrivalTimes = new java.util.ArrayDeque<>();

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
        arrivalTimes.add(clock);
    }


    @Override
    protected void printSnapshot(Events ev) {
        TandemEvent event = (TandemEvent) ev;

        StringBuilder state = new StringBuilder("n : " + customerInSysCount);
        for (int i = 0; i < stageCount; i++) {
            state.append(String.format(", S%d[status : %d, queue : %d]",
                    i, serverStatusAtStage[i], waitQueues[i].count()));
        }

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

        double averageSystemTime = totalSysTime / finishedServicesCount;

        return new MetricReport(averageWaitingTime, timeAverageNumOfCustomers, serverUtilization, averageSystemTime);
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
        if (currentStage == stageCount - 1) {
            customerInSysCount--;
            finishedServicesCount++;
            addLifeTime();

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
            waitingCustomer newCus = new waitingCustomer(ev.customerNumber(), ev.eventTime());
            currQueue.enqueue(newCus);
        }
    }


    //for a NEW customer at the START
    private void handleNewArrival(TandemEvent incomingEv) {
        customerInSysCount++;
        double genArrTime = interArrivalVariate.generateNumber();
        arrivalTimes.add(clock + genArrTime);

        // gen a new arrival after we received a new one
        TandemEvent nextArrival = new TandemEvent(
                EventType.ARRIVAL, clock + genArrTime,
                lastArrivalCustomerId++, 0
        );

        fel.schedule(nextArrival);

        handleArrivalAtStage(0, incomingEv);
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

    private void addLifeTime() {
        double arrival = arrivalTimes.poll();
        totalSysTime += clock - arrival;
    }


    public double[] getStationAverages() {
        double[] averages = new double[stageCount];

        for (int i = 0; i < stageCount; i++)
            averages[i] = cumulativeAreaAtStage[i] / clock;

        return averages;
    }
}
