package org.playasophy.wonderdome.program;

import processing.core.*;

import org.playasophy.wonderdome.PixelMatrix;


public class ColorCycle implements Program {

    ///// CONSTANTS /////

    private static final int scale = 4;
    private static final int brightness = 196;
    private static final float speed = 0.05f;



    ///// PROPERTIES /////

    private final PApplet parent;
    private final ColorGradient gradient;
    private float offset = 0.0f;



    ///// INITIALIZATION /////

    public ColorCycle(PApplet parent) {
        this.parent = parent;
        this.gradient = new ColorGradient(30.0f, 240.0f,
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



    ///// Program METHODS /////

    public void update(PixelMatrix pixels) {

        // Iterate over pixels in the matrix and set them to the appropriate spot in gradient.
        int numRows = pixels.getNumRows();
        int numColumns = pixels.getNumColumns();
        for ( int s = 0; s < numRows; s++ ) {
            for ( int p = 0; p < numColumns; p++ ) {
                int color = gradient.getColor(offset + (float)p/numColumns);
                pixels.setPixel(s, p, color);
            }
        }

        // Update state.
        offset += speed;

    }



    ///// PRIVATE STATIC INNER CLASSES /////

    private class ColorGradient {

        // Definitions of the color scale, density, and gradient resolution.
        public final float density;
        public final float shades;
        public final int[] colors;

        public ColorGradient(float density, float shades, int... colors) {
            this.density = density;
            this.shades = shades;
            this.colors = colors;
        }

        // Determines the color to assign to the given escape time.
        int getColor(float t) {
            t = (t*density % shades) * (colors.length/shades);

            int t0 = (int) parent.floor(t);
            int t1 = (int) parent.ceil(t);

            int c0 = ( t0 >= colors.length ) ? colors[colors.length - 1] : colors[t0];
            int c1 = ( t1 >= colors.length ) ? colors[0] : colors[t1];

            return parent.lerpColor(c0, c1, t - t0);
        }

    }

}
