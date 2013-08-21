package org.playasophy.wonderdome.mode;


import processing.core.PApplet;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.sdk.ColorGradient;


public class ColorCycle implements Mode {

    ///// CONSTANTS /////

    // The "speed" determines how fast the mode completes a single cycle
    // through the color gradient. This is how many cycles it moves per second.
    private static final float DEFAULT_SPEED = 0.5f;
    private static final float MIN_SPEED = 0.05f;
    private static final float MAX_SPEED = 4.00f;

    private static final float DEFAULT_DENSITY = 3.0f;
    private static final float MIN_DENSITY = 0.5f;
    private static final float MAX_DENSITY = 12.0f;



    ///// PROPERTIES /////

    private final PApplet parent;
    private final ColorGradient gradient;
    private float offset = 0.0f;
    private float density = DEFAULT_DENSITY;
    private float speed = DEFAULT_SPEED;



    ///// INITIALIZATION /////

    public ColorCycle(PApplet parent) {
        this.parent = parent;
        parent.colorMode(parent.RGB);
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
    public void update(int[][] pixels, long dt) {

        // Iterate over pixels in the matrix and set them to the appropriate spot in gradient.
        for ( int s = 0; s < pixels.length; s++ ) {
            for ( int p = 0; p < pixels[s].length; p++ ) {
                float v = offset + (float)p/pixels[s].length;
                pixels[s][p] = gradient.getColor(v*density);
            }
        }

        // Update state.
        offset += speed*dt/1000f;

    }

    @Override
    public void handleEvent(final InputEvent event) {
        if ( event instanceof ButtonEvent ) {
            handleButtonEvent((ButtonEvent) event);
        }
    }

    private void handleButtonEvent(ButtonEvent event) {

        boolean isPressed = event.getType() == ButtonEvent.Type.PRESSED;
        switch ( event.getId() ) {
            case UP:
                speed *= 1.1f;
                break;
            case DOWN:
                speed /= 1.1f;
                break;
            case LEFT:
                density /= 1.1f;
                break;
            case RIGHT:
                density *= 1.1f;
                break;
        }

        speed = Math.min(Math.max(speed, MIN_SPEED), MAX_SPEED);
        density = Math.min(Math.max(density, MIN_DENSITY), MAX_DENSITY);

        System.out.printf("Colorcycle speed: %.2f / density: %.2f\n", speed, density);

    }

}
