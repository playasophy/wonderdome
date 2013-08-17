package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;


public class MovementTest implements Mode {

    ///// CONSTANTS /////

    private static final long BLINK_DURATION = 250L;



    ///// PROPERTIES /////

    private final PApplet parent;
    private int x;
    private int y;
    private int[] colors;
    private int colorIndex;
    private boolean showCursor;
    private long blinkElapsed;



    ///// INITIALIZATION /////

    public MovementTest(PApplet parent) {
        this.parent = parent;
        parent.colorMode(parent.RGB);
        x = 1;
        y = 1;
        colors = new int[] {
            parent.color(255, 0, 0),
            parent.color(0, 255, 0),
            parent.color(0, 0, 255)
        };
        colorIndex = 0;
        showCursor = true;
        blinkElapsed = 0;
    }



    ///// Mode METHODS /////

    @Override
    public void update(int[][] pixels, long dtMillis) {

        // Blink cursor if blink duration has elapsed.
        blinkElapsed += dtMillis;
        if ( blinkElapsed >= BLINK_DURATION ) {
            showCursor = !showCursor;
            blinkElapsed = 0;
        }

        // Draw box centered on x, y
        for ( int i = 0; i < pixels.length; i++ ) {
            for ( int j = 0; j < pixels[i].length; j++ ) {
                int c = parent.color(0, 0, 0);
                if ( showCursor &&
                     i >= x - 1 && i <= x + 1 &&
                     j >= y - 1 && j <= y + 1 ) {
                    c = colors[colorIndex];
                }

                setPixel(pixels, i, j, c);
            }
        }

    }

    private void setPixel(int[][] pixels, int x, int y, int color) {
        if ( x < 0 || x > 5 || y < 0 || y > 239 ) {
            return;
        }

        pixels[x][y] = color;
    }

    @Override
    public void handleEvent(InputEvent event) {
        if ( event instanceof ButtonEvent ) {
            handleButtonEvent((ButtonEvent) event);
        }
    }

    private void handleButtonEvent(ButtonEvent event) {

        boolean isPressed = event.getType() == ButtonEvent.Type.PRESSED;

        if ( !isPressed ) {
            return;
        }

        switch ( event.getButtonId() ) {
            case UP:
                y -= 1;
                break;
            case DOWN:
                y += 1;
                break;
            case LEFT:
                x -= 1;
                break;
            case RIGHT:
                x += 1;
                break;
            case A:
                colorIndex += 1;
                break;
            case B:
                colorIndex -= 1;
                break;
        }

        // Correct all values to be within allowed ranges.
        if ( x < 0 ) {
            x = 5;
        } else if ( x > 5 ) {
            x = 0;
        }

        if ( y < 0 ) {
            y = 239;
        } else if ( y > 239 ) {
            y = 0;
        }

        if ( colorIndex < 0 ) {
            colorIndex = colors.length - 1;
        } else if ( colorIndex >= colors.length ) {
            colorIndex = 0;
        }

    }

}
