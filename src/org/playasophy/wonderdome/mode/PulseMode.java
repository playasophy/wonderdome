package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Random;

// 
// Class: PulseMode
// Author: Zack
//
// Description:
//      Pulse Mode: The grid as a whole pulses from black to a color slowly. Flashes of twinkling stars occur throughout.  
//
//		Controls:
//			Up/Down: Controls the brightness
//			Left/Right: Controls the speed.
//			A: Changes the hue delta each turn, up then loops back to zero.
//			B: Changes direction of the cascade.
//

public class PulseMode extends SimpleMode {
    protected static Random rand = new Random();
   
    private static final int HUE_RANGE = 30;
    private static final int MIN_SPEED = 50;
    private static final int MAX_SPEED = 10000;
    private int speed = 1400;

    private int currHue = 200;
    private int currSat = 255;
    
    private int currBrightness = 0;
    
    // Stores hue and sat for the grid;
    private int[][][] currentPixels = new int[MAX_LED_STRIPS][MAX_LEDS_PER_STRIP][2];
    
    private StarManager starManager;
    
    // C'Tor
    public PulseMode(PApplet parent)
    {
        super(parent);
        
        resetPixels();
        starManager = new StarManager();
    }
    
    private boolean pulsingBrighter = true;
    private long timeToPulse = speed;
    @Override
    protected void updateState(long dtMillis)
    {
    	//starManager.update(dtMillis);
    	
    	timeToPulse -= dtMillis;
    	if (timeToPulse <= 0)
    	{
    		timeToPulse = speed;
    		pulsingBrighter = !pulsingBrighter;
    		if (pulsingBrighter)
    		{
    			resetPixels();
    		}
    	}
    	
    	// Adjust current brightness to pulse with the time left on the counter.
    	float percentLeftToPulse = (float)timeToPulse / (float)speed;
    	if (pulsingBrighter)
    	{
    		currBrightness = box(maxBrightness - (int)(percentLeftToPulse * (float)maxBrightness));
    	}
    	else
    	{
    		currBrightness = box((int)(percentLeftToPulse * (float)maxBrightness));
    	}
    }
   
    private int maxBrightness = 155;
    private int box(int x)
    {
    	return Math.max(Math.min(255, x), 0);
    }
    
    private void resetPixels()
    {
    	currHue = rand.nextInt(255);
		currSat = rand.nextInt(150) + 105;
		
		for (int i=0; i<MAX_LED_STRIPS; ++i)
		{
			for (int j=0; j<MAX_LEDS_PER_STRIP; ++j)
			{
				currentPixels[i][j][0] = box(currHue + ((rand.nextInt(2) == 0 ? -1 : 1) * 10));
				currentPixels[i][j][1] = box(currHue + ((rand.nextInt(2) == 0 ? -1 : 1) * 20));
			}
		}
    }
    
    @Override
    protected int getPixelColor(int x, int y, long dtMillis)
    {
    	// Stars take precedence.
    	if (starManager.hasStar(x,y))
    	{
    		return starManager.getStarColor(x,y);
    	}
    	else
    	{
    		return parent.color(currentPixels[x][y][0], currentPixels[x][y][1], currBrightness);
    	}
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
    	speed -= 300;
    	speed = Math.max(MIN_SPEED, speed);
    }
    
    @Override
    protected void DownButtonPressed()
    {
    	speed += 300;
    	speed = Math.min(MAX_SPEED, speed);
    }
    
    @Override
    protected void AButtonPressed()
    {
    	resetPixels();
        maxBrightness += 10;
        maxBrightness %= 255;
    }
    
    @Override
    protected void BButtonPressed()
    {
    	//starManager.EngageTwinkleAttack();
    }
    
    @Override
    protected void LeftButtonPressed()
    {
    	starManager.RemoveStar();
    }
    
    @Override
    protected void RightButtonPressed()
    {
    	starManager.AddStar();
    }
    //
    // END Input Handlers
    //
    
    private class StarManager
    {
    	private ArrayList<Star> stars = new ArrayList<Star>();
    	private StarManager()
    	{
    		for (int i = 0; i<8; ++i)
    		{
    			stars.add(new Star());
    		}
    	}
    	
    	public boolean hasStar(int x, int y)
    	{
    		for (Star s : stars)
    		{
    			if (s.x == x && s.y == y)
    			{
    				return true;
    			}
    		}
    		
    		return false;
    	}
    	
    	public int getStarColor(int x,int y)
    	{
    		for (Star s : stars)
    		{
    			if (s.x == x && s.y == y)
    			{
    				return s.getColor();
    			}
    		}
    		return 0;
    	}
    	
    	public void AddStar()
    	{
    		stars.add(new Star());
    	}
    	
    	public void RemoveStar()
    	{
    		if (stars.size() > 0)
    			stars.remove(0);
    	}
    	
    	public void update(long dtMillis)
		{
    		for (Star s : stars)
    		{
    			s.update(dtMillis);
    		}
    		
    		// Twinkle Attack Mode!
    		if (twinkleAttackTimer > 0)
    		{
    			twinkleAttackTimer -= dtMillis;
    			if (twinkleAttackTimer <= 0)
    			{
    				twinkleAttackTimer = 0;
    				DisengageTwinkleAttack();
    			}
    		}
		}
    	
    	private long twinkleAttackTimer = 0;
    	private int numStarsBeforeTwinkleAttack = 0;
    	public void EngageTwinkleAttack()
    	{
    		numStarsBeforeTwinkleAttack = stars.size();
    		twinkleAttackTimer = 3000;
    		while (stars.size() < 1000)
    		{
    			AddStar();
    		}
    	}
    	
    	private void DisengageTwinkleAttack()
    	{
    		while (stars.size() > numStarsBeforeTwinkleAttack)
    		{
    			RemoveStar();
    		}
    	}
    	
    	private class Star
    	{
    		public int x,y;
    		private int twinkleSpeed;
    		private long twinkleTimer;
    		private Star()
    		{
    			reset();
    		}
    		
    		private int twinkleBrightness = 0;
    		private boolean twinkleUp = true;
    		public void update(long dtMillis)
    		{
    			twinkleTimer -= dtMillis;
    			if(twinkleTimer <= 0)
    			{
    				twinkleUp = !twinkleUp;
    				if (twinkleUp) reset();
    				twinkleTimer = twinkleSpeed;
    			}
    			
    			if (twinkleUp) twinkleBrightness = 255 - box((int)((float)twinkleTimer / (float)twinkleSpeed));
    			else 		   twinkleBrightness =       box((int)((float)twinkleTimer / (float)twinkleSpeed));
    		}
    		
    		public int getColor()
    		{
    			return parent.color(0,0,twinkleBrightness);
    		}
    		
    		private void reset()
    		{
    			x = rand.nextInt(MAX_LED_STRIPS);
    			y = rand.nextInt(MAX_LEDS_PER_STRIP);
    			twinkleSpeed = rand.nextInt(250) + 50;
    		}
    		
    		
    		
    
    		
    	}
    }
}
