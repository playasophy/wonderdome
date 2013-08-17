package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;
import org.playasophy.wonderdome.sdk.ColorGradient;


public class ColorCycle implements Mode {

    ///// CONSTANTS /////

    private static final float DEFAULT_SPEED = 0.05f;



    ///// PROPERTIES /////

    private final PApplet parent;
    private final ColorGradient gradient;
    private float offset = 0.0f;
    private float speed = DEFAULT_SPEED;
    private boolean aPressed = false;
    private boolean bPressed = false;



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

    @Override
    public void update(int[][] pixels, long dtMillis) {

        // Iterate over pixels in the matrix and set them to the appropriate spot in gradient.
        for ( int s = 0; s < pixels.length; s++ ) {
            for ( int p = 0; p < pixels[s].length; p++ ) {
                int color = gradient.getColor(offset + (float)p/pixels[s].length);
                pixels[s][p] = color;
            }
        }

        // Update state.
        offset += speed;

    }

    @Override
    public void handleEvent(InputEvent event) {
        if ( event instanceof ButtonEvent ) {
            handleButtonEvent((ButtonEvent) event);
        }
    }

    private void handleButtonEvent(ButtonEvent event) {

        boolean isPressed = event.getType() == ButtonEvent.Type.PRESSED;
        switch ( event.getButtonId() ) {
            case A:
                aPressed = isPressed;
                speed *= 2;
                break;
            case B:
                bPressed = isPressed;
                speed /= 2;
                break;
        }

        if ( speed < .01 ) {
            speed = .01f;
        } else if ( speed > .2 ) {
            speed = .2f;
        }

        System.err.println("ColorCycle handling button event " + event.getType() + " on button " + event.getButtonId());

    }

}
