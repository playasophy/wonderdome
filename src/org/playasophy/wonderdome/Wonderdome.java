package org.playasophy.wonderdome;


import ddf.minim.*;
import ddf.minim.analysis.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.*;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.input.InputEventListener;
import org.playasophy.wonderdome.input.KonamiCodeListener;
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
    private Mode easterEggMode;
    private boolean easterEggRunning;
    private int currentModeIndex;
    private State state;
    private boolean autoCycle;
    private long lastUpdate;
    private long lastEvent;

    // Event management variables.
    private List<InputEventListener> listeners;



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
            new FlickerMode(parent),
            //new PulseMode(parent),
            //new MovementTest(parent),
            new LanternMode(parent),
            new SeizureMode(parent),
            new LeftRightMode(parent),
            new OpticalAssaultMode(parent)
            //new ShootingStarMode(parent)
        );

        // Initial Mode [Change for ease of use when testing new modes].
        switchToMode(0);

        easterEggMode = new StrobeMode(parent);
        easterEggRunning = false;

        // Initialize event listeners.
        listeners = new ArrayList<InputEventListener>();
        listeners.add(new KonamiCodeListener(this));
        setLastEvent();

        state = State.RUNNING;
        autoCycle = true;
        lastUpdate = System.currentTimeMillis();

    }



    ///// PUBLIC METHODS /////

    public void pre() {

        // If auto-cycling is enabled and no events have occurred in the last
        // 5 minutes, cycle modes.
        if ( autoCycle && System.currentTimeMillis() - lastEvent > 5 * 60 * 1000) {
            cycleModes();
            lastEvent = System.currentTimeMillis();
        }

        if ( state == State.RUNNING ) {

            // Perform audio processing.
            if ( audio != null ) audio.update();

            long dt = System.currentTimeMillis() - lastUpdate;

            try {
                getCurrentMode().update(pixels, dt);
            } catch ( Exception e ) {
                evictCurrentMode(e);
            }

            // If auto-cycling is disabled, indicate it with a single red pixel.
            if ( !autoCycle ) {
                parent.colorMode(parent.RGB);
                pixels[0][PIXELS_PER_STRIP - 1] = parent.color(255, 0, 0);
            }

        }

        lastUpdate = System.currentTimeMillis();
    }


    public int[][] getPixels() {
        return pixels;
    }


    public void handleEvent(InputEvent event) {
        System.out.println("Handling event '" + event + "'");
        setLastEvent();

        boolean consumed = false;

        // First, run through all of the registered event listeners.
        for ( InputEventListener listener : listeners ) {
            consumed = listener.handleEvent(event);
            if ( consumed ) {
                return;
            }
        }

        // If no registered listener consumed the event, process it here.
        // FIXME: Move this code into one or more additional event listeners.
        if ( event instanceof ButtonEvent ) {
            ButtonEvent be = (ButtonEvent) event;
            if ( be.getId() == ButtonEvent.Id.SELECT ) {
                handleSelectButton(be.getType());
                consumed = true;
            } else if ( be.getId() == ButtonEvent.Id.START ) {
                if ( be.getType() == ButtonEvent.Type.PRESSED ) {
                    toggleAutoCycle();
                }
                consumed = true;
            }
        }

        // If nothing else consumed the event, send it to the current mode.
        if ( !consumed ) {
            try {
                getCurrentMode().handleEvent(event);
            } catch ( Exception e ) {
                evictCurrentMode(e);
            }
        }

    }


    public void toggleAutoCycle() {
        autoCycle = !autoCycle;
    }


    public void togglePause() {
        System.out.println("togglePause");
        if ( state == State.RUNNING ) {
            pause();
        } else {
            resume();
        }
    }


    public void pause() {
        System.out.println("pause");
        state = State.PAUSED;
    }


    public void resume() {
        System.out.println("resume");
        state = State.RUNNING;
    }


    public void runEasterEgg() {
        System.out.println("Running easter egg!");
        easterEggRunning = true;
        getCurrentMode().onShow();
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(20 * 1000);
                } catch ( InterruptedException e ) { 
                    // Do nothing.
                }
                easterEggRunning = false;
                System.out.println("Easter egg stopped");
                setLastEvent();
            }
        }.start();
    }


    public void setModeList(List<Mode> modes) {
        // TODO: Implement this.
    }



    ///// PRIVATE METHODS /////

    private Mode getCurrentMode() {
        if ( easterEggRunning ) {
            return easterEggMode;
        } else {
            return modes.get(currentModeIndex);
        }
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
        setLastEvent();

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
            getCurrentMode().onShow();
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

    private void setLastEvent() {
        lastEvent = System.currentTimeMillis();
    }

}
