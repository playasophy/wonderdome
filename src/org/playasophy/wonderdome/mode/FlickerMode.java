package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Random;

// 
// Class: FlickerMode
// Author: Zack
//
// Description:
//      Flicker Mode, is an ambient mode that pulses each LED gently.
//
//		Controls:
//			Up/Down: Controls the flicker frequency
//			Left/Right: Controls the Hue
//			A/B: Controls the brightness
//

public class FlickerMode extends SimpleMode {
    protected static Random rand = new Random();
    
    // ADJUSTABLE PARAMS//
    private static final int HUE_RANGE = 30;
    private static final int MIN_SPEED = 50;
    private static final int MAX_SPEED = 1500;
    private int speed = 350;
    private int maxBrightness = 255;
    private int hueRangeStart = 40;
    //////////////////////

    private FlickerPixel[][] pixels = new FlickerPixel[MAX_LED_STRIPS][MAX_LEDS_PER_STRIP];

    // C'Tor
    public FlickerMode(PApplet parent)
    {
        super(parent);
        
        for (int i=0; i<MAX_LED_STRIPS; ++i)
        {
        	for (int j=0; j<MAX_LEDS_PER_STRIP; ++j)
        	{
        		pixels[i][j] = new FlickerPixel();
        	}
        }
    }

    @Override
    protected int getPixelColor(int x, int y, long dtMillis)
    {
    	return pixels[x][y].getColor(dtMillis);
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
    protected void UpButtonPressed() 
    {
    	// Increase The Flicker Frequency
    	if (speed - 100 < MIN_SPEED)
    	{ 
    		speed = MIN_SPEED+1;
    	}
    	else
    	{
    		speed -= 100;
    	}
    	System.out.println("Flicker Delay = " + speed);
    }
    
    @Override
    protected void DownButtonPressed()
    {
    	// Decrease the flicker frequency
    	if (speed + 100 > MAX_SPEED)
    	{
    		speed = MAX_SPEED;
    	}
    	else
    	{
    		speed += 100;	
    	}
    	System.out.println("Flicker Delay = " + speed);
    }
    
    @Override
    protected void LeftButtonPressed()
    {
    	// Decrease the Hue
    	if (hueRangeStart - 20 < 0)
    	{
    		hueRangeStart = 255 - HUE_RANGE;
    	}
    	else
    	{
    		hueRangeStart -= 20;
    	}
    	System.out.println("Hue Range Start = " + hueRangeStart);
    }
    
    @Override
    protected void RightButtonPressed()
    {
    	// Increase the Hue
    	if (hueRangeStart + HUE_RANGE + 20 > 255)
    	{
    		hueRangeStart = 0;
    	}
    	else
    	{
    		hueRangeStart += 20;
    	}
    	System.out.println("Hue Range Start = " + hueRangeStart);
    }
    
    @Override
    protected void AButtonPressed()
    {
    	// Decrease Brightness
    	if (maxBrightness - 20 < 0) maxBrightness = 1;
    	else maxBrightness -= 20;
    	
    	System.out.println("Max Brightness = " + maxBrightness);
    }
    
    @Override
    protected void BButtonPressed()
    {
    	// Increase brightness
    	if (maxBrightness + 20 >= 255) maxBrightness = 255;
    	else maxBrightness += 20;
    	
    	System.out.println("Max Brightness = " + maxBrightness);
    }
    //
    // END Input Handlers
    //
    
    
    //
    // PRIVATE CLASS FlickerPixel
    //
    private class FlickerPixel
    {
    	
    	private long timeToTargetMillis = 100;
        private long timeSinceTargetSetMillis = 0L;

        private int h=0,s=0,b=0;
        private int startH=0,startS=0,startB=0;
        private int targetH,targetS,targetB;
        private FlickerPixel()
        {
           resetTarget();
        }

        public void resetTarget()
        {
           targetH = rand.nextInt(HUE_RANGE) + hueRangeStart;
           targetS = rand.nextInt(255);
           targetB = rand.nextInt(maxBrightness);

           timeSinceTargetSetMillis = 0L;
           // FIXME: This line is crashing with the error:
           // "java.lang.IllegalArgumentException: n must be positive"
           // Presumably this means speed is sometimes <= 50.
           int randSpeed = speed - MIN_SPEED;
           if ( randSpeed <= 0 ) {
               randSpeed = 1;
           }
           timeToTargetMillis = rand.nextInt(randSpeed) + MIN_SPEED;
        }
        
        public int getColor(long dtMillis)
        {
        	timeSinceTargetSetMillis += dtMillis;
        	
        	if (timeSinceTargetSetMillis >= timeToTargetMillis)
        	{
        		h=startH=targetH;
        		s=startS=targetS;
        		b=startB=targetB;
        		
        		resetTarget();
        	}
        	else
        	{
        		double percentToTarget = (double)timeSinceTargetSetMillis / (double)timeToTargetMillis;
        		
        		h = interpolate(startH, targetH, percentToTarget);
        		s = interpolate(startS, targetS, percentToTarget);
        		b = interpolate(startB, targetB, percentToTarget);
        	}
        	
        	return parent.color(h,s,b);
        }
        
        private int interpolate(int start, int target, double percentInterpolated)
        {
        	return (int)((target - start)*percentInterpolated) + start;
        }
    }
}
