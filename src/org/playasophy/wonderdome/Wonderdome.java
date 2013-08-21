package org.playasophy.wonderdome;


import ddf.minim.*;
import ddf.minim.analysis.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.*;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.mode.*;
import org.playasophy.wonderdome.sdk.AudioContext;



public class Wonderdome {

    ///// TYPES /////

    private enum State {
        PAUSED,
        RUNNING
    }



    ///// CONSTANTS /////

    public static final int NUM_STRIPS = 6;
    public static final int PIXELS_PER_STRIP = 240;



    ///// PROPERTIES /////

    // Parent applet for graphics interaction.
    private final PApplet parent;

    // Display buffer.
    private final int[][] pixels;

    // Audio processing components.
    private final AudioContext audio;

    // Mode management variables.
    private List<Mode> modes;
    private int currentModeIndex;
    private State state;
    private long lastUpdate;



    ///// INITIALIZATION /////

    public Wonderdome(
            final PApplet parent,
            final AudioInput input) {

        this.parent = parent;
        parent.registerMethod("pre", this);
        pixels = new int[NUM_STRIPS][PIXELS_PER_STRIP];

        if ( input != null ) {
            System.out.println("Acquired audio input, initializing FFT and beat detection");
            audio = new AudioContext(input);
        } else {
            System.out.println("No audio input detected");
            audio = null;
        }

        modes = Arrays.asList(
            new ColorCycle(parent),
            new MovementTest(parent),
            new LanternMode(parent),
            new FlickerMode(parent)
        );

        // Initial Mode [Change for ease of use when testing new modes].
        switchToMode(0);

        state = State.RUNNING;
        lastUpdate = System.currentTimeMillis();

    }



    ///// PUBLIC METHODS /////

    public void pre() {

        if ( state == State.RUNNING ) {

            // Perform audio processing.
            if ( audio != null ) audio.update();

            long dt = System.currentTimeMillis() - lastUpdate;

            try {
                getCurrentMode().update(pixels, dt);
            } catch ( Exception e ) {
                evictCurrentMode(e);
            }
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
            try {
                getCurrentMode().handleEvent(event);
            } catch ( Exception e ) {
                evictCurrentMode(e);
            }
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


    private void evictCurrentMode(final Throwable cause) {

        System.err.println(
            "Mode '" + getCurrentMode().getClass() +
            "' threw exception '" + cause.getMessage() +
            "' and is being evicted from the mode cycle."
            );
        cause.printStackTrace();

        modes.remove(currentModeIndex);
        cycleModes();

    }


    private void handleSelectButton(final ButtonEvent.Type type) {
        if ( type == ButtonEvent.Type.PRESSED ) {
            cycleModes();
        }
    }


    private void switchToMode(int modeIndex) {
        if (modeIndex >= 0 && modeIndex < modes.size())
        {
            currentModeIndex = modeIndex;
            System.out.println("Now in mode " + currentModeIndex + ": " + modes.get(currentModeIndex).getClass());
        }
    }


    private void cycleModes() {
        int newMode = currentModeIndex + 1;
        if (newMode >= modes.size())
        {
            newMode = 0;
        }
        switchToMode(newMode);
    }

}
