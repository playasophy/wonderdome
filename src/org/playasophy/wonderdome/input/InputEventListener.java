package org.playasophy.wonderdome.input;

public interface InputEventListener {

    /**
     * Handles the given input event.
     *
     * @param event The input event to handle.
     *
     * @return {@code true} if and only if the event has been consumed (i.e.
     * no further processing of the event should occur).
     */
    boolean handleEvent(InputEvent event);

}
