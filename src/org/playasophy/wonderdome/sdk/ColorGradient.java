package org.playasophy.wonderdome.sdk;

import processing.core.PApplet;


public class ColorGradient {

    ///// PROPERTIES /////

    private final PApplet parent;

    // Definitions of the color scale, density, and gradient resolution.
    private final float density;
    private final float shades;
    private final int[] colors;



    ///// INITIALIZATION /////

    public ColorGradient(PApplet parent, float density, float shades, int... colors) {
        this.parent = parent;
        this.density = density;
        this.shades = shades;
        this.colors = colors;
    }



    ///// PUBLIC METHODS /////

    /**
     * Determines the color to assign to the given escape time.
     */
    public int getColor(float t) {
        t = (t*density % shades) * (colors.length/shades);

        int t0 = (int) parent.floor(t);
        int t1 = (int) parent.ceil(t);

        int c0 = ( t0 >= colors.length ) ? colors[colors.length - 1] : colors[t0];
        int c1 = ( t1 >= colors.length ) ? colors[0] : colors[t1];

        return parent.lerpColor(c0, c1, t - t0);
    }

}
