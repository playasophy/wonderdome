package org.playasophy.wonderdome;

import java.util.List;

import processing.core.*;

import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.mode.ColorCycle;
import org.playasophy.wonderdome.mode.Mode;
import org.playasophy.wonderdome.mode.MovementTest;


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
    private long lastUpdate;



    ///// INITIALIZATION /////

    public Wonderdome(PApplet parent) {
        this.parent = parent;
        parent.registerMethod("pre", this);
        pixels = new int[NUM_STRIPS][PIXELS_PER_STRIP];
        // TODO: Don't hardcode the mode.
        //currentMode = new ColorCycle(parent);
        currentMode = new MovementTest(parent);
        state = State.RUNNING;
        lastUpdate = System.currentTimeMillis();
    }



    ///// PUBLIC METHODS /////

    public void pre() {
        if ( state == State.RUNNING ) {
            long dt = System.currentTimeMillis() - lastUpdate;
            currentMode.update(pixels, dt);
        }
        lastUpdate = System.currentTimeMillis();
    }

    public int[][] getPixels() {
        return pixels;
    }

    public void handleEvent(InputEvent event) {
        currentMode.handleEvent(event);
    }

    public void pause() {
        state = State.PAUSED;
    }

    public void resume() {
        state = State.RUNNING;
    }

    public void setModeList(List<Mode> modes) {
        // TODO: Implement this.
    }

}
