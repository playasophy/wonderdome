package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Random;

// 
// Class: LeftRightMode
// Author: Zack
//
// Description:
//      Left Right Mode: Each strip is a solid color that cascades to the next strip over. 
//
//		Controls:
//			Up/Down: Controls the brightness
//			Left/Right: Controls the speed.
//			A: Changes the hue delta each turn, up then loops back to zero.
//			B: Changes direction of the cascade.
//

public class LeftRightMode extends SimpleMode {
    protected static Random rand = new Random();
   
    private static final int HUE_RANGE = 30;
    private static final int MIN_SPEED = 50;
    private static final int MAX_SPEED = 2500;
    private int speed = 100;
    private int brightness = 255;
    private int nextHue = 100;

    private int[] stripHues = new int[MAX_LED_STRIPS];
    
    // C'Tor
    public LeftRightMode(PApplet parent)
    {
        super(parent);
        
        for (int i =0; i< MAX_LED_STRIPS; ++i)
        {
        	stripHues[i] = getNewHue();
        }
    }
    
    
    private long timeToUpdate = speed;
    @Override
    protected void updateState(long dtMillis)
    {
    	timeToUpdate -= dtMillis;
    	if (timeToUpdate <= 0)
    	{
    		timeToUpdate = speed;
    		updateStrips();
    	}
    }
    
    private boolean directionReversed = false;
    private void updateStrips()
    {
    	if (!directionReversed)
    	{
    		for (int i=MAX_LED_STRIPS-1; i > 0; --i)
    		{
    			stripHues[i] = stripHues[i-1];
    		}
    		stripHues[0] = getNewHue();
    	}
    	else
    	{
    		
            for (int i=0; i < MAX_LED_STRIPS-1; ++i)
    		{
    			stripHues[i] = stripHues[i+1];
    		}
    		stripHues[MAX_LED_STRIPS-1] = getNewHue();
    	    
        }
    }

    private int hueDiff = 25;
    private int getNewHue()
    {
    	int newHue = nextHue;
    	if (!directionReversed) nextHue += hueDiff;
    	else nextHue -= hueDiff;
    	
        if (nextHue > 255) nextHue -= 255;
        if (nextHue < 0) nextHue += 255;
    	
    	return newHue;
    }
    
    @Override
    protected int getPixelColor(int x, int y, long dtMillis)
    {
    	return parent.color(stripHues[x], 200, brightness);
    }

    @Override
    protected int getColorModeForThisMode()
    {
    	return parent.HSB;
    }

    //
    // BEGIN Input Handlers
    //
    @Override
    protected void RightButtonPressed() 
    {
    	speed -= 50;
    	speed = Math.max(MIN_SPEED, speed);
    }
    
    @Override
    protected void LeftButtonPressed()
    {
    	speed += 50;
    	speed = Math.min(MAX_SPEED, speed);
    }
    
    @Override
    protected void AButtonPressed()
    {
    	hueDiff += 15;
    	hueDiff %= 255;
    }
    
    @Override
    protected void BButtonPressed()
    {
    	directionReversed = !directionReversed;
    }
    
    @Override
    protected void DownButtonPressed()
    {
    	// Decrease Brightness
    	if (brightness - 20 < 0) brightness = 1;
    	else brightness -= 20;
    	
    	System.out.println("Brightness = " + brightness);
    }
    
    @Override
    protected void UpButtonPressed()
    {
    	// Increase brightness
    	if (brightness + 20 >= 255) brightness = 255;
    	else brightness += 20;
    	
    	System.out.println("Brightness = " + brightness);
    }
    //
    // END Input Handlers
    //
}
