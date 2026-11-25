package SimulatorEngine.Models;

import java.util.Arrays;

import SimulatorEngine.SimEvents.EventType;
import SimulatorEngine.SimEvents.Events;
import SimulatorEngine.SimEvents.TandemEvent;
import SimulatorEngine.waitingQueue;
import Variates.RandomNumberGenerator;
import Variates.Variate;
import SimulatorEngine.waitingQueue.waitingCustomer;

public class ClosedNetworkModel extends Model {

    // indexed by station number
    private int stationCount;
    private int[] serverStatusAtStation;
    private waitingQueue[] waitQueueAtStation;
    private Variate[] serviceVariateAtStation;
    private int[] customerCountAtStation;
    private double[] serverUtilTimeAtStation;
    private double[] cumulativeAreaAtStation;

    private RandomNumberGenerator routingGenerator;
    private double termSim;

    public ClosedNetworkModel(Variate[] serviceVariateAtStation, int N, int routingSeed, double termSim){
        super();
        this.stationCount = 4;
        this.termSim = termSim;
        this.serviceVariateAtStation = serviceVariateAtStation;
        this.routingGenerator = new RandomNumberGenerator(routingSeed);

        this.serverStatusAtStation = new int[stationCount];
        this.customerCountAtStation = new int[stationCount];
        this.serverUtilTimeAtStation = new double[stationCount];
        this.cumulativeAreaAtStation = new double[stationCount];
        this.waitQueueAtStation = new waitingQueue[stationCount];

        for(int i = 0; i < stationCount; i++)
            waitQueueAtStation[i] = new waitingQueue();

        Initialization(N);
    }

    public void Initialization(int N){
        for(int c = 0; c < N; c++)
            waitQueueAtStation[0].enqueue(new waitingQueue.waitingCustomer(c, clock));
        customerCountAtStation[0] = N;
        customerInSysCount = N;
        startService(0);
    }

    protected void updateStatistics(double elapsed){
        for(int i = 0; i < stationCount; i++){
            if(serverStatusAtStation[i] == 1)
                serverUtilTimeAtStation[i] += elapsed;
            cumulativeAreaAtStation[i] += elapsed * customerCountAtStation[i];
        }
    }

    protected void handleEvent(Events e){
        TandemEvent ev = (TandemEvent) e;
        if(ev.type() == EventType.DEPARTURE)
            handleDepartureEvent(ev);
        if(clock > termSim)
            isModelActive = false;
    }

    private void handleDepartureEvent(TandemEvent ev){
        int stationNumber = ev.stageNum();
        customerCountAtStation[stationNumber]--;
        finishedServicesCount++;
        serverStatusAtStation[stationNumber] = 0;
        if(customerCountAtStation[stationNumber] > 0)
            startService(stationNumber);

        if(stationNumber != 0){
            handleArrival(0, ev.customerNumber());
            return;
        }
        double p = routingGenerator.generate();
        if(p >= 0.0 && p < 0.5)
            handleArrival(1, ev.customerNumber());
        else if(p >= 0.5 && p < 0.8)
            handleArrival(2, ev.customerNumber());
        else if(p >= 0.8 && p < 1)
            handleArrival(3, ev.customerNumber());
    }

    private void handleArrival(int k, int customerId){
        customerCountAtStation[k]++;
        waitQueueAtStation[k].enqueue(new waitingCustomer(customerId, clock));
        if(serverStatusAtStation[k] == 0)
            startService(k);
    }

    private void startService(int stationNumber){
        waitingCustomer cus = waitQueueAtStation[stationNumber].dequeue();
        totalWaitingTime += clock - cus.waitingStartTime();
        serverStatusAtStation[stationNumber] = 1;
        double serviceDuration = serviceVariateAtStation[stationNumber].generateNumber();
        fel.schedule(new TandemEvent(EventType.DEPARTURE, clock + serviceDuration, cus.customerId(), stationNumber));
    }

    protected void printSnapshot(Events e){
        TandemEvent ev = (TandemEvent) e;
        StringBuilder systemState = new StringBuilder();
        systemState.append("serverStatusAtStation : " + Arrays.toString(serverStatusAtStation));
        systemState.append(", customerCountAtStation : " + Arrays.toString(customerCountAtStation));
        systemState.append(", waitQueueAtStation : " + Arrays.toString(waitQueueAtStation));
        systemState.append(", numOfCustomersInTheSystem : " + customerInSysCount);
        System.out.println(
            String.format("[%f] | [C%d S%d %s] | [%s] | [%s]", clock, ev.customerNumber(), ev.stageNum(), ev.type(), systemState, fel.toString())
        );
    }

    protected MetricReport generateReport(){
        double averageWaitingTime = totalWaitingTime / finishedServicesCount;
        double timeAverageNumOfCustomers = totalCumulativeAreas / clock;
        double[] serverUtilization = new double[stationCount];
        for(int i = 0; i < stationCount; i++)
            serverUtilization[i] = serverUtilTimeAtStation[i] / clock;
        return new MetricReport(averageWaitingTime, timeAverageNumOfCustomers, serverUtilization);
    }
}
