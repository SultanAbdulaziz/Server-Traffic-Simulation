package SimulatorEngine.Models;

import java.util.Arrays;

import SimulatorEngine.FEL;
import SimulatorEngine.SimEvents.*;

public abstract class Model{
    protected FEL fel;
    protected double clock;
    protected int finishedServicesCount; //counter for finished services nw
    protected double totalWaitingTime; //total waiting times accumulated sw
    protected int customerInSysCount; //total number of current customers in the system n
    protected double totalCumulativeAreas; //total cumulative areas increment by (clock - lastClock) * n each step sa
    protected double serverUtilTime; //server utilization
    protected boolean isModelActive;

    public Model(){
        clock = totalWaitingTime = totalCumulativeAreas = serverUtilTime = 0.0;
        finishedServicesCount = customerInSysCount = 0;
        isModelActive = true;
        fel = new FEL();
    }

    public record MetricReport(
            double averageWaitingTime,
            double timeAverageNumOfCustomers,
            double[] serverUtilization, // one entry per server
            double averageSystemTime    // r_bar; NaN for models that don't measure it
    ) {
        public MetricReport(double averageWaitingTime, double timeAverageNumOfCustomers, double[] serverUtilization) {
            this(averageWaitingTime, timeAverageNumOfCustomers, serverUtilization, Double.NaN);
        }

        @Override
        public String toString() {
            String rBar = Double.isNaN(averageSystemTime)
                    ? ""
                    : String.format(", averageSystemTime=%f", averageSystemTime);

            return String.format("MetricReport[averageWaitingTime=%f, timeAverageNumOfCustomers=%f%s, serverUtilization=%s]",
                    averageWaitingTime, timeAverageNumOfCustomers, rBar, Arrays.toString(serverUtilization));
        }
    }

    public MetricReport runModel(boolean TRACE_MODE){
        while(isModelActive){
            Events nextEvent = fel.fetch();
            double last = clock;
            clock = nextEvent.eventTime();
            double elapsed = clock - last;
            totalCumulativeAreas += elapsed * customerInSysCount;
            updateStatistics(elapsed);
            handleEvent(nextEvent);
            if(TRACE_MODE)
                printSnapshot(nextEvent);
        }
        return generateReport();
    }

    protected abstract void updateStatistics(double elapsed);

    protected abstract void handleEvent(Events ev);

    protected abstract void printSnapshot(Events ev);

    protected abstract MetricReport generateReport();
}
