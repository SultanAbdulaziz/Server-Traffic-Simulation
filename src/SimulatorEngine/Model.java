package SimulatorEngine;

public abstract class Model{
    protected FEL fel;
    protected double clock;
    protected int nw; //counter for finished services
    protected double sw; //total waiting times accumulated
    protected int n; //total number of current customers in the system
    protected double sa; //total cumlative areas increment by (clock - lastClock) * n each step
    protected double w; //average waiting time
    protected double serverUtilTime; //server utilization

    public Model(){
        clock = sw = sa = w = serverUtilTime = 0.0;
        nw = n = 0;
        fel = new FEL();
    }

    public record MetricReport(
        double averageWaitingTime,
        double timeAverageNumOfCustomers,
        double serverUtilization
     ){}

    public abstract MetricReport runModel();

}
