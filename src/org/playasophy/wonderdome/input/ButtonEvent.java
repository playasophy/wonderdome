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

    private final Id buttonId;
    public Id getButtonId() { return buttonId; }



    ///// INITIALIZATION /////

    public ButtonEvent(final int controllerId, final Id buttonId, final Type type) {
        super(controllerId);
        this.buttonId = buttonId;
        this.type = type;
    }

}
