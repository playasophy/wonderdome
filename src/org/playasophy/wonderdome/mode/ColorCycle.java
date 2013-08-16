package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import org.playasophy.wonderdome.sdk.ColorGradient;


public class ColorCycle implements Mode {

    ///// CONSTANTS /////

    private static final float SPEED = 0.05f;



    ///// PROPERTIES /////

    private final PApplet parent;
    private final ColorGradient gradient;
    private float offset = 0.0f;



    ///// INITIALIZATION /////

    public ColorCycle(PApplet parent) {
        this.parent = parent;
        parent.colorMode(parent.RGB);
        this.gradient = new ColorGradient(parent, 30.0f, 240.0f,
            parent.color(  0,   0,   0),
            parent.color(255,   0, 255),
            parent.color(  0, 128, 255),
            parent.color(  0, 255,   0),
            parent.color(255, 128,   0),
            parent.color(255,   0,   0),
            parent.color(128,   0,   0),
            parent.color(255, 255, 255)
        );
    }



    ///// Mode METHODS /////

    private boolean updated = false;

    @Override
    public void update(int[][] pixels) {

        // Iterate over pixels in the matrix and set them to the appropriate spot in gradient.
        for ( int s = 0; s < pixels.length; s++ ) {
            for ( int p = 0; p < pixels[s].length; p++ ) {
                int color = gradient.getColor(offset + (float)p/pixels[s].length);
                pixels[s][p] = color;
            }
        }

        // Update state.
        offset += SPEED;

    }

}
