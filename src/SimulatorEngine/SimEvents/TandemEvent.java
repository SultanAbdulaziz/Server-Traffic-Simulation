package SimulatorEngine.SimEvents;

import SimExceptions.NullEventException;

public record TandemEvent(EventType type, double eventTime, int customerNumber, int stageNum) implements Events {

    @Override
    public int compareTo(Events ev) throws NullEventException {
        if (ev == null)
            throw new NullEventException("Event Can't be Null When Compared");
        return Double.compare(this.eventTime, ev.eventTime());
    }

    public String toString() {
        return String.format("(C%d, Stage: %d, %s, %f)", customerNumber, stageNum, type.name(), eventTime);
    }
}
