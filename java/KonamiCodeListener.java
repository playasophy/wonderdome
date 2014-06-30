package org.playasophy.wonderdome.input;

import org.playasophy.wonderdome.Wonderdome;


public class KonamiCodeListener implements InputEventListener {

    ///// CONSTANTS /////

    private static final ButtonEvent.Id[] CODE = new ButtonEvent.Id[] {
        ButtonEvent.Id.UP,
        ButtonEvent.Id.UP,
        ButtonEvent.Id.DOWN,
        ButtonEvent.Id.DOWN,
        ButtonEvent.Id.LEFT,
        ButtonEvent.Id.RIGHT,
        ButtonEvent.Id.LEFT,
        ButtonEvent.Id.RIGHT,
        ButtonEvent.Id.B,
        ButtonEvent.Id.A,
        ButtonEvent.Id.START
    };



    ///// PROPERTIES /////

    private Wonderdome wonderdome;
    private int currentIndex = 0;



    ///// INITIALIZATION /////

    public KonamiCodeListener(final Wonderdome wonderdome) {
        this.wonderdome = wonderdome;
    }



    ///// InputEventListener PUBLIC METHODS /////

    /**
     * Handles the given input event.
     *
     * @param event The input event to handle.
     *
     * @return {@code true} if and only if the event has been consumed (i.e.
     * no further processing of the event should occur).
     */
    public boolean handleEvent(InputEvent event) {
        System.out.println("KonamiCodeListener.handleEvent called");
        boolean consumed = false;
        if ( event instanceof ButtonEvent ) {
            ButtonEvent be = (ButtonEvent) event;
            ButtonEvent.Id id = be.getId();
            ButtonEvent.Type type = be.getType();
            if ( type == ButtonEvent.Type.PRESSED ) {
                if ( id == CODE[currentIndex] ) {
                    currentIndex++;
                    System.out.println("Konami code advanced to index " + currentIndex);
                    if ( currentIndex == CODE.length ) {
                        wonderdome.runEasterEgg();
                        currentIndex = 0;

                        // For the final "start" button, consume the event to prevent pausing.
                        consumed = true;
                    }
                } else {
                    System.out.println("Konami code reset");
                    currentIndex = 0;
                }
            }
        }

        return consumed;
    }

}
