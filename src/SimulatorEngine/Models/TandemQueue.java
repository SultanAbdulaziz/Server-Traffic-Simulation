package SimulatorEngine.Models;

import SimulatorEngine.Event;
import SimulatorEngine.FEL;

public class TandemQueue extends Model{

    int[] queueCountAtStage;


    TandemQueue(){
        super();


    }

    private void handleDepartureAtStage(int leavingStage){}


    private void handleArrivalAtStage(int enteringStage){}

    private void handleNewArrival(){}

    private void handleFullDeparture(){}



    @Override
    public MetricReport runModel(boolean TRACE_MODE) {
        return null;
    }

    @Override
    protected void printSnapshot(Event ev) {

    }
}
