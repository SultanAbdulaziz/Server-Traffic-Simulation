package SimulatorEngine.SimEvents;

import SimExceptions.NullEventException;

public record TandemEvent(EventType type, double eventTime, int stageNum) implements Events {

    @Override
    public int compareTo(Events ev) throws NullEventException {
        if (ev == null)
            throw new NullEventException("Event Can't be Null When Compared");
        if (this.eventTime > ev.eventTime()) {
            return 1;
        } else if (this.eventTime < ev.eventTime()) {
            return -1;
        } else {
            return 0;
        }
    }

    public String toString() {
        return String.format("(Stage: %d, %s, %f)", stageNum, type.name(), eventTime);
    }
}
