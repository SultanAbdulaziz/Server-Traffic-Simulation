package SimulatorEngine;

public abstract class Model{
    protected FEL fel;
    protected double clock;
    protected int finishedServicesCount; //counter for finished services nw
    protected double totalWaitingTime; //total waiting times accumulated sw
    protected int customerInSysCount; //total number of current customers in the system n
    protected double totalCumulativeAreas; //total cumlative areas increment by (clock - lastClock) * n each step sa
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
        double serverUtilization
     ){}

    public abstract MetricReport runModel(boolean TRACE_MODE);

    protected abstract void printSnapshot(Event ev);
    
    protected MetricReport generateReport(){
        double averageWaitingTime = totalWaitingTime / finishedServicesCount;
        double timeAverageNumOfCustomers = totalCumulativeAreas / clock;
        double serverUtilization = serverUtilTime / clock;
        return new MetricReport(averageWaitingTime, timeAverageNumOfCustomers, serverUtilization);
    }
}
