package purr.purr.events.impl;

import purr.purr.events.Event;

public class SprintEvent extends Event {
    private boolean sprintState;

    public SprintEvent(boolean sprintState) {
        this.sprintState = sprintState;
    }

    public boolean getSprintState() {
        return this.sprintState;
    }

    public void setSprintState(boolean sprintState) {
        this.sprintState = sprintState;
    }
}
