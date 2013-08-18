package org.playasophy.wonderdome;

import java.util.ArrayList;
import java.util.List;

import processing.core.*;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.mode.ColorCycle;
import org.playasophy.wonderdome.mode.Mode;
import org.playasophy.wonderdome.mode.MovementTest;
import org.playasophy.wonderdome.mode.ZackTest;
import org.playasophy.wonderdome.mode.SimpleMode;

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
    private List<Mode> modes;
    private int currentModeIndex;
    private State state;
    private long lastUpdate;



    ///// INITIALIZATION /////

    public Wonderdome(PApplet parent) {
        this.parent = parent;
        parent.registerMethod("pre", this);
        pixels = new int[NUM_STRIPS][PIXELS_PER_STRIP];
        modes = new ArrayList<Mode>();
        modes.add(new ColorCycle(parent));
        modes.add(new MovementTest(parent));
        modes.add(new ZackTest(parent));
        modes.add(new SimpleMode(parent));
        currentModeIndex = 0;
        state = State.RUNNING;
        lastUpdate = System.currentTimeMillis();
    }



    ///// PUBLIC METHODS /////

    public void pre() {
        if ( state == State.RUNNING ) {
            long dt = System.currentTimeMillis() - lastUpdate;
            getCurrentMode().update(pixels, dt);
        }
        lastUpdate = System.currentTimeMillis();
    }

    public int[][] getPixels() {
        return pixels;
    }

    public void handleEvent(InputEvent event) {
        boolean consumed = false;
        if ( event instanceof ButtonEvent ) {
            ButtonEvent be = (ButtonEvent) event;
            if ( be.getId() == ButtonEvent.Id.SELECT ) {
                handleSelectButton(be.getType());
                consumed = true;
            }
        }

        if ( !consumed ) {
            getCurrentMode().handleEvent(event);
        }
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



    ///// PRIVATE METHODS /////

    private Mode getCurrentMode() {
        return modes.get(currentModeIndex);
    }

    private void handleSelectButton(final ButtonEvent.Type type) {
        if ( type == ButtonEvent.Type.PRESSED ) {
            currentModeIndex++;
            if ( currentModeIndex >= modes.size() ) {
                currentModeIndex = 0;
            }
            
            System.out.println("Switching to mode: " + modes.get(currentModeIndex).getClass());
        }
    }

}
