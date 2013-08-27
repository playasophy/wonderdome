package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.sdk.ColorGradient;


public class SeizureMode extends SimpleMode {

    ///// CONSTANTS /////



    ///// PROPERTIES /////

    private final ColorGradient gradient;
    private float colorOffset = 0.0f;
    private float speed = 0.1f;
    private int positionOffset = 0;



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

        // Get the current color based on the color offest.
        colorOffset += speed * (dtMillis / 1000f);
        int currentColor = gradient.getColor(colorOffset);

        // Set pixels to that color.
        for ( int s = 0; s < pixels.length; s++ ) {
            for ( int p = 0; p < pixels[s].length; p++ ) {
                pixels[s][p] = currentColor;
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
    }

    @Override
    protected void DownButtonPressed() {
    }

    @Override
    protected void AButtonPressed() {
    }

    @Override
    protected void BButtonPressed() {
    }

}
