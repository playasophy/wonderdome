package org.playasophy.wonderdome.input;

public class ButtonEvent extends ControllerEvent {

    ///// TYPES /////

    public enum Id {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        A,
        B,
        SELECT,
        START
    }

    public enum Type {
        PRESSED,
        RELEASED
    }



    ///// PROPERTIES /////

    private final Type type;
    public Type getType() { return type; }

    private final Id id;
    public Id getId() { return id; }



    ///// INITIALIZATION /////

    public ButtonEvent(final int controllerId, final Id id, final Type type) {
        super(controllerId);
        this.id = id;
        this.type = type;
    }

}
