package org.playasophy.wonderdome.input;

public abstract class ControllerEvent extends InputEvent {

    ///// PROPERTIES /////

    private final int controllerId;
    public int getControllerId() { return controllerId; }



    ///// INITIALIZATION /////

    public ControllerEvent(final int controllerId) {
        this.controllerId = controllerId;
    }

}
