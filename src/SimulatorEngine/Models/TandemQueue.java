package SimulatorEngine.Models;

import SimulatorEngine.SimEvents.Event;
import Variates.Variate;

public class TandemQueue extends Model{

    int stageCount;
    int[] queueCountAtStage;
    int[] serverStatusAtStage;

    private Variate interArrivalVariate;
    private Variate serviceVariate;

    private int customerLimit;
    private int lastArrivalCustomerId;


    TandemQueue(Variate interArrivalVariate, Variate serviceVariate, int customerLimit,  int lastArrivalCustomerId){
        super();
        this.stageCount = stageCount;
        this.serverStatusAtStage = new int[stageCount];
        this.queueCountAtStage = new int[stageCount];
        this.interArrivalVariate = interArrivalVariate;
        this.serviceVariate = serviceVariate;
        this.customerLimit = customerLimit;
        this.lastArrivalCustomerId = lastArrivalCustomerId;
    }

    @Override
    public MetricReport runModel(boolean TRACE_MODE) {
        while(isModelActive){
            Event nexEvent = (Event) fel.fetch();
            double last = clock;
            clock = nexEvent.eventTime();
            totalCumulativeAreas += (clock - last) * customerInSysCount;
            if(serverStatus == 1)
                serverUtilTime += (clock - last);
            if(nexEvent.type() == Event.EventType.ARRIVAL)
                handleArrivalEvent(nexEvent);
            else
                handleDepartureEvent();
            if(TRACE_MODE)
                printSnapshot(nexEvent);
        }
        return generateReport();
    }

    @Override
    protected void printSnapshot(Event ev) {

    }

    private void handleDepartureAtStage(int leavingStage){}

    private void handleArrivalAtStage(int enteringStage){}

    private void handleNewArrival(){}

    private void handleFullDeparture(){}
}
