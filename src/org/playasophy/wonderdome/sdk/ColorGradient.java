package org.playasophy.wonderdome.sdk;


import processing.core.PApplet;


public class ColorGradient {

    ///// PROPERTIES /////

    private final PApplet parent;

    // Definitions of the color scale.
    private final int[] colors;



    ///// INITIALIZATION /////

    public ColorGradient(PApplet parent, int... colors) {
        this.parent = parent;
        this.colors = colors;
    }



    ///// PUBLIC METHODS /////

    /**
     * Determines the color to assign to the given scale value.
     * Effectively, the color gradient is rendered smoothly from [0.0, 1.0],
     * such that both 0.0 and 1.0 are rendered the first color in the cycle.
     * If the value is outside this range, the gradient cycles.
     */
    public int getColor(float v) {

        float t = (v % 1.0f) * (colors.length + 1);

        // as t goes from 0 -> shades
        // 'color increments per shade'
        // density increases the 'rate' at which t moves through the color space

        int t0 = (int) Math.floor(t);
        int t1 = (int) Math.ceil(t);

        int c0 = ( t0 >= colors.length ) ? colors[colors.length - 1] : colors[t0];
        int c1 = ( t1 >= colors.length ) ? colors[0] : colors[t1];

        return parent.lerpColor(c0, c1, t - t0);
    }

}
