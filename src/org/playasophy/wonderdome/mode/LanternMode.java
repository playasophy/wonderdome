package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

// 
// Class: LanternMode
// Author: Zack
//
// Description:
//      In Lantern Mode, it just displays all white at a constant brightness [0-255].
//      The starting brightness is 200.
//      You can adjust the brightness up and down with the button UP and button DOWN buttons.
//      You can also snap the brightness to full with A and snap it to off with B.
//

public class LanternMode extends SimpleMode {

    private static final int MAX_BRIGHTNESS = 255;
    private static final int MIN_BRIGHTNESS = 0;
    private static final int DIMMER_DELTA = 30;
    
    private int lanternBrightness = 200; 
    

    // C'Tor
    public LanternMode(PApplet parent)
    {
        super(parent);
    }

    @Override
    protected int getPixelColor(int x, int y, long dtMillis)
    {
        // Return all white 
        return getColorRGB(lanternBrightness, lanternBrightness, lanternBrightness);
    }
    
    private void setBrightness(int newBrightness)
    {
        if (newBrightness > MAX_BRIGHTNESS) newBrightness = MAX_BRIGHTNESS;
        if (newBrightness < MIN_BRIGHTNESS) newBrightness = MIN_BRIGHTNESS;
        
        lanternBrightness = newBrightness;
        dumpBrightness();
    }
    
    private void dumpBrightness()
    {
        System.out.println("Lantern Mode Brightness = " + lanternBrightness);
    }
    
    // Brighten the lantern on Up Pressed
    protected void UpButtonPressed() 
    {
        setBrightness(lanternBrightness + DIMMER_DELTA);
    }
    
    // Dim the lantern on Down Pressed
    protected void DownButtonPressed() 
    {
        setBrightness(lanternBrightness - DIMMER_DELTA);
    }
    
    // A Turns it On
    protected void AButtonPressed() 
    {
        setBrightness(MAX_BRIGHTNESS);
    }
    
    // B Turns it Off
    protected void BButtonPressed() 
    {
        setBrightness(MIN_BRIGHTNESS);
    }
}
