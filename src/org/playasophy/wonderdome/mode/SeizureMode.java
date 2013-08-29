package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.sdk.ColorGradient;


public class SeizureMode extends SimpleMode {

    ///// CONSTANTS /////

    private static final float COLOR_CYCLE_SPEED_DELTA = .02f;
    private static final float MIN_COLOR_CYCLES_PER_SECOND = .05f;
    private static final float MAX_COLOR_CYCLES_PER_SECOND = .5f;
    private static final long POSITION_UPDATE_INTERVAL_DELTA = 25;
    private static final long MIN_POSITION_UPDATE_INTERVAL = 25;
    private static final long MAX_POSITION_UPDATE_INTERVAL = 500;
    private static final int PIXELS_ON_PER_PERIOD = 4;
    private static final int PIXELS_OFF_PER_PERIOD = 3;
    private static final int PIXELS_PER_PERIOD = PIXELS_ON_PER_PERIOD + PIXELS_OFF_PER_PERIOD;



    ///// PROPERTIES /////

    private final ColorGradient gradient;
    private float colorOffset = 0.0f;
    private float colorCyclesPerSecond = 0.1f;
    private int positionOffset = 0;
    private long positionUpdateInterval = 100;
    private long nextPositionUpdate = 0;



    ///// INITIALIZATION /////

    public SeizureMode(PApplet parent) {
        super(parent);
        this.gradient = new ColorGradient(parent,
            parent.color(  0,   0,   0),
            parent.color(255,   0, 255),
            parent.color(  0, 128, 255),
            parent.color(  0, 255,   0),
            parent.color(255, 128,   0),
            parent.color(255,   0,   0),
            parent.color(128,   0,   0)
        );
    }



    ///// Mode METHODS /////

    @Override
    public void update(int[][] pixels, long dtMillis) {

        // Get the current color based on the color offset.
        colorOffset += colorCyclesPerSecond * (dtMillis / 1000f);
        int currentColor = gradient.getColor(colorOffset);

        long now = System.currentTimeMillis();
        if ( now > nextPositionUpdate ) {
            incrementPositionOffset();
            nextPositionUpdate = now + positionUpdateInterval;
        }

        // Set pixels to that color.
        for ( int s = 0; s < pixels.length; s++ ) {
            for ( int p = 0; p < pixels[s].length; p++ ) {
                int location = ( s % 2 == 0 ? p + positionOffset : p - positionOffset );
                if ( location % PIXELS_PER_PERIOD < PIXELS_ON_PER_PERIOD ) {
                    pixels[s][p] = currentColor;
                } else {
                    pixels[s][p] = parent.color(0, 0, 0);
                }
            }
        }

    }

    @Override
    protected int getPixelColor(int x, int y, long dtMillis) {
        // FIXME: Change SimpleMode to not require subclasses to implement this.
        return 0;
    }



    ///// SimpleMode OVERRIDE METHODS /////

    @Override
    protected void UpButtonPressed() {
        setColorCyclesPerSecond(colorCyclesPerSecond + COLOR_CYCLE_SPEED_DELTA);
    }

    @Override
    protected void DownButtonPressed() {
        setColorCyclesPerSecond(colorCyclesPerSecond - COLOR_CYCLE_SPEED_DELTA);
    }

    @Override
    protected void LeftButtonPressed() {
        setPositionUpdateInterval(positionUpdateInterval + POSITION_UPDATE_INTERVAL_DELTA);
    }

    @Override
    protected void RightButtonPressed() {
        setPositionUpdateInterval(positionUpdateInterval - POSITION_UPDATE_INTERVAL_DELTA);
    }

    @Override
    protected void AButtonPressed() {
    }

    @Override
    protected void BButtonPressed() {
    }



    ///// PRIVATE METHODS /////

    private static float boundF(float value, float min, float max) {
        if ( value <= min ) {
            return min;
        } else if ( value >= max ) {
            return max;
        } else {
            return value;
        }
    }

    private static long boundL(long value, long min, long max) {
        if ( value <= min ) {
            return min;
        } else if ( value >= max ) {
            return max;
        } else {
            return value;
        }
    }

    private void setColorCyclesPerSecond(float value) {
        colorCyclesPerSecond = boundF(value, MIN_COLOR_CYCLES_PER_SECOND, MAX_COLOR_CYCLES_PER_SECOND);
    }

    private void setPositionUpdateInterval(long value) {
        positionUpdateInterval = boundL(value, MIN_POSITION_UPDATE_INTERVAL, MAX_POSITION_UPDATE_INTERVAL);
    }

    private void incrementPositionOffset() {
        positionOffset++;
        if ( positionOffset > PIXELS_PER_PERIOD ) {
            positionOffset = 0;
        }
    }

}
