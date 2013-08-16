package org.playasophy.wonderdome;

import processing.core.*;

import org.playasophy.wonderdome.mode.ColorCycle;
import org.playasophy.wonderdome.mode.Mode;


public class Wonderdome {

    ///// TYPES /////

    private enum State {
        PAUSED,
        RUNNING
    }



    ///// CONSTANTS /////

    private static final int NUM_STRIPS = 6;
    private static final int PIXELS_PER_STRIP = 240;



    ///// PROPERTIES /////

    private final PApplet parent;
    private int[][] pixels;
    private Mode currentMode;
    private State state;



    ///// INITIALIZATION /////

    public Wonderdome(PApplet parent) {
        this.parent = parent;
        parent.registerMethod("pre", this);
        pixels = new int[NUM_STRIPS][PIXELS_PER_STRIP];
        // TODO: Don't hardcode the mode.
        currentMode = new ColorCycle(parent);
        state = State.RUNNING;
    }



    ///// PUBLIC METHODS /////

    public void pre() {
        if ( state == State.RUNNING ) {
            currentMode.update(pixels);
        }
    }

    public int[][] getPixels() {
        return pixels;
    }

    public void handleEvent(String source, String command) {
        if ( source.equals("admin") ) {
            handleAdminCommand(command);
        } else if ( source.equals("control") ) {
            handleControlCommand(command);
        }
    }



    ///// PRIVATE METHODS /////

    private void handleAdminCommand(String command) {
        System.out.println("Handling admin command '" + command + "'");
        if ( command.equals("pause") ) {
            state = State.PAUSED;
        } else if ( command.equals("resume") ) {
            state = State.RUNNING;
        }
    }

    private void handleControlCommand(String command) {
        System.out.println("Handling control command '" + command + "'");
        if ( state == State.RUNNING ) {
            // FIXME: Implement this.
        }
    }

}
