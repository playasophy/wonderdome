package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;


public class StrobeMode extends SimpleMode {

    ///// CONSTANTS /////




    ///// PROPERTIES /////

    private final int[] colors;
    private int colorIndex;



    ///// INITIALIZATION /////

    public StrobeMode(PApplet parent) {
        super(parent);
        this.colors = new int[] {
            parent.color(255,   0,   0),
            parent.color(  0, 255,   0),
            parent.color(  0,   0, 255),
        };
        this.colorIndex = 0;
    }



    ///// Mode METHODS /////

    @Override
    public void update(int[][] pixels, long dtMillis) {
        colorIndex++;
        if ( colorIndex >= colors.length ) {
            colorIndex = 0;
        }
        super.update(pixels, dtMillis);
    }

    @Override
    protected int getPixelColor(int x, int y, long dtMillis) {
        return colors[colorIndex];
    }



    ///// SimpleMode OVERRIDE METHODS /////

    @Override
    protected void UpButtonPressed() {
    }

    @Override
    protected void DownButtonPressed() {
    }

    @Override
    protected void LeftButtonPressed() {
    }

    @Override
    protected void RightButtonPressed() {
    }

    @Override
    protected void AButtonPressed() {
    }

    @Override
    protected void BButtonPressed() {
    }

}
