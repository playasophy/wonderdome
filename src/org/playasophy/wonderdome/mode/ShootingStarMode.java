package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Iterator;
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

public class ShootingStarMode extends SimpleMode {
    protected static Random rand = new Random();
    
    // ADJUSTABLE PARAMS//
    private int baseStarSpeed = 1400;
    private int hue_range = 40;
    private int hue_start = 50;
    //////////////////////
    
    private long timeToNewStar = 300;
    private long currTimeToNewStar = 300;
    
    private long timeToPattern = 10000;
    private long currTimeToPattern = 10000;
    
    
    private ArrayList<ShootingStar> stars = new ArrayList<ShootingStar>();

    // C'Tor
    public ShootingStarMode (PApplet parent)
    {
        super(parent);
    }
    
    private int flashTime=0;
    private int flashHue=0;
    private int flashSat=0;
    @Override
    protected void updateState(long dtMillis)
    {
    	// Add new Stars regularly
    	currTimeToNewStar -= dtMillis;
    	if (currTimeToNewStar < 0)
    	{
    		currTimeToNewStar = timeToNewStar;
    		AddShootingStar();
    		
    		hue_start++;
    		hue_start %= 255;
    	}
    	
    	// Every once in awhile do a fun pattern.
    	currTimeToPattern -= dtMillis;
    	//System.out.println("" + currTimeToPattern);
    	if (currTimeToPattern < 0)
    	{
    		currTimeToPattern = timeToPattern;
    		
    		cascadeAttack();
    	}
    	
    	
    	// Update and remove Stars
		for (Iterator<ShootingStar> starIter = stars.iterator(); starIter.hasNext(); )
		{
			ShootingStar star = starIter.next();
			star.update(dtMillis);
			if (!star.isActive())
			{
				starIter.remove();
				//System.out.println("Removing Shooting Star");
			}
		}
		
		
		if (flashNextCycle)
		{
			flashTime -= dtMillis;
			if (flashTime < 0)
			{
				flashNextCycle = false;
			}
		}
		if (flashAttack)
		{
			flashNextCycle = true;
			flashAttack = false;
			flashTime = 500;
			flashHue = rand.nextInt(255);
			flashSat = rand.nextInt(255);
			
		}

		
    }
    
    private void cascadeAttack()
    {
    	AddShootingStar(0);
		AddShootingStar(1);
		AddShootingStar(2);
		AddShootingStar(3);
		AddShootingStar(4);
		AddShootingStar(5);
    }

    private boolean flashAttack = false;
    private boolean flashNextCycle = false;
    @Override
    protected int getPixelColor(int x, int y, long dtMillis)
    {
    	int color = 0;
    	for (ShootingStar star : stars)
    	{
    		if (star.row == x)
    		{
    			color += star.GetColor(y);
    		}
    	}
    	
    	if (flashNextCycle)
    	{
    		color = parent.color(flashHue,flashSat,255);
    	}
    	
    	return color;
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
    	int maxSpeed = 10000;
     	baseStarSpeed += 200;
    	if (baseStarSpeed > maxSpeed) baseStarSpeed = maxSpeed;
    	System.out.println("Speed = " + baseStarSpeed);
    }
    
    @Override
    protected void LeftButtonPressed()
    {
    	int minSpeed = 250;
    	baseStarSpeed -= 200;
    	if (baseStarSpeed < minSpeed) baseStarSpeed = minSpeed;
    	System.out.println("Speed = " + baseStarSpeed);
    }
    
    @Override
    protected void AButtonPressed()
    {
    	// Flash attack!
    	flashAttack = true;
    }
    
    @Override
    protected void BButtonPressed()
    {
    	hue_start += 170;
    	hue_start %= 255;
    	cascadeAttack();
    	/*
    	// Increase the Hue
    	if (hueRangeStart + HUE_RANGE + 20 > 255)
    	{
    		hueRangeStart = 0;
    	}
    	else
    	{
    		hueRangeStart += 20;
    	}
    	System.out.println("Hue Range Start = " + hueRangeStart);*/
    }
    
    private int maxBrightness = 255;
    @Override
    protected void DownButtonPressed()
    {
    	
    	// Decrease Brightness
    	if (maxBrightness - 20 < 0) maxBrightness = 1;
    	else maxBrightness -= 20;
    	
    	System.out.println("Max Brightness = " + maxBrightness);
    }
    
    @Override
    protected void UpButtonPressed()
    {
    	// Increase brightness
    	if (maxBrightness + 20 >= 255) maxBrightness = 255;
    	else maxBrightness += 20;
    	
    	System.out.println("Max Brightness = " + maxBrightness);
    }
    //
    // END Input Handlers
    //
   
    
    private void AddShootingStar(int x)
    {
    	stars.add(new ShootingStar(x));
    }
    
    private void AddShootingStar()
    {
    	//System.out.println("Adding Shooting Star...");	
    	stars.add(new ShootingStar());
    }
    
    //
    // PRIVATE CLASS ShootingStar
    //
    private class ShootingStar
    {
    	public int row;
    	private int headSize = 3;
    	protected int h=0, s=0, b=0;
    	
    	private long timeSinceBirth = 0;
    	private long timeToFinish = 500;
    	private long timeToFade;
    	
    	private boolean fShooting;
    	private boolean fActive;
    	
    	private int lastHeadIndex = 0;
    	
    	
    	private ShootingStarPixel[] pixels = new ShootingStarPixel[MAX_LEDS_PER_STRIP];
    	
    	private ShootingStar()
    	{
    		this(rand.nextInt(MAX_LED_STRIPS));
    	}
    	
    	private ShootingStar(int row)
        {
    		this.row = row;
    		
    		headSize = rand.nextInt(15) + 1;
    		timeToFinish = rand.nextInt(700) + baseStarSpeed; 
    		timeToFade = rand.nextInt(1000) + 700 + (int)(baseStarSpeed*0.25); 
    		h = (rand.nextInt(hue_range) + hue_start) % 255;
    		s = rand.nextInt(155) + 100;
    		b = maxBrightness;		   // FIX: Make randomized later
    		
    		fShooting = true;
    		fActive = true;
    		
    		for (int i=0; i<MAX_LEDS_PER_STRIP; ++i)
    		{
    			pixels[i] = new ShootingStarPixel(timeToFade, h, s, b);
    		}
        }
    	
    	public boolean isActive() { return fActive; }
    	
    	public void update(long dtMillis)
    	{
    		if (!fActive) return;
    		
    		// Figure out where head is
    		timeSinceBirth += dtMillis;
    		
    		double percentComplete = (double)timeSinceBirth / (double)timeToFinish;
    		percentComplete = Math.min(1.0, percentComplete);
    		
    		// NOTE: Head Location goes past the end of the strip.
    		int headLocation = (int)((percentComplete)*(double)(MAX_LEDS_PER_STRIP+headSize-1));
    		
    		// Light the head.
    		if (fShooting && percentComplete <= 1.0)
    		{
	    		// Light the head.
	    		for (int i = headLocation; i > headLocation - headSize && i >= 0; i--)
	    		{
	    			if (i < MAX_LEDS_PER_STRIP)
	    			{
	    				pixels[i].start();
	    			}
	    		}
	    		
	    		
	    		// Light all the pixels skipped over too.
	    		for (int i = lastHeadIndex; i <= headLocation; ++i)
	    		{
	    			if (i < MAX_LEDS_PER_STRIP)
	    			{
	    				pixels[i].start();
	    			}
	    		}
	    		
	    		lastHeadIndex = headLocation;
	    		
	    		if (percentComplete >= 1.0)
	    		{
	    			fShooting = false;
	    		}
    		}
    		
    		// Update the tails.
    		boolean tailsActive = false;
    		boolean tailExists = headLocation-headSize > 0;
    		for (int tailIndex=headLocation-headSize; tailIndex>=0; tailIndex--)
    		{
    			pixels[tailIndex].update(dtMillis);
    			if (pixels[tailIndex].isActive())
    			{
    				tailsActive = true;
    			}
    		}
    		
    		if (tailExists && !tailsActive && !fShooting)
    		{
    			fActive = false;
    		}
    	}
    	
    	public int GetColor(int index)
    	{
    		return parent.color(pixels[index].h, pixels[index].s, pixels[index].b);
    	}
    	
    	
    	private class ShootingStarPixel
    	{
    		long timeLeftToFade = 0;
    		long timeToFade = 1000;
    		
    		public int h=0, s=0, b=0, initBrightness=0;
    		
    		private ShootingStarPixel(long timeToFade, int h, int s, int initBrightness)
    		{
    			this.timeToFade = timeToFade;
    			this.h=h;
    			this.s=s;
    			b = 0;
    			this.initBrightness= initBrightness;
    		}

    		public void update(long dtMillis)
    		{
    			timeLeftToFade -= dtMillis;
    			timeLeftToFade = Math.max(0, timeLeftToFade);
    			b = Math.max(0, (int)(((double)timeLeftToFade / (double)timeToFade)*(double)initBrightness));
    		}
    		
    		public boolean isActive() { return timeLeftToFade > 0; }
    		
    		public void start()
    		{
    			timeLeftToFade = timeToFade;
    			b = initBrightness;
    		}
    	}
    }
}
