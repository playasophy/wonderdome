package org.playasophy.wonderdome.mode;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Random;

// 
// Class: OpticalAssaultMode
// Author: Zack
//
// Description:
//      Optical Assault Mode, IT WILL FUCK WITH YOUR EYES.
//			It starts with a countdown, where the pixels fill with bright red to the brim over about 8 seconds.
//			After the countdown, it fuckin' goes nuts. random incredibly bright pixels.
//			You can press left/right to cycle between all out pixel madness, row/column/entire grid assault.
//
//
//		Controls:
//			Up/Down: Controls the speed of the assault.
//			Left/Right: 
//			A: Toggles the brightness down and up (the oh shit button)
//			B: Starts the countdown again.
//

public class OpticalAssaultMode extends SimpleMode {
    protected static Random rand = new Random();
    
    private AssaultPixel[][] pixels = new AssaultPixel[MAX_LED_STRIPS][MAX_LEDS_PER_STRIP];
    private AssaultPixel[] assaultRows = new AssaultPixel[MAX_LEDS_PER_STRIP];
    private AssaultPixel[] assaultColumns = new AssaultPixel[MAX_LED_STRIPS];
    private AssaultPixel allOutAssault;
    
    private int holyShitFuckTurnItDownBrightness = 255;
    private int holyShitFuckTurnItDownSpeed = 50;
    
    private int assaultMode = 0;
    private int numModes = 4;
    /* 
    	PixelAssault,
    	ColumnAssault,
    	RowAssault,
    	AllOutAssault,
    */
    // Also Countdown mode which is separate.
    private CountdownMode countdownMode;
    
    // C'Tor
    public OpticalAssaultMode(PApplet parent)
    {
        super(parent);
        
        assaultMode = 1;
        
        for (int i=0; i<MAX_LED_STRIPS; ++i)
        {
        	for (int j=0; j<MAX_LEDS_PER_STRIP; ++j)
        	{
        		pixels[i][j] = new AssaultPixel();
        	}
        }
        
        for (int i=0; i<MAX_LED_STRIPS; ++i)
        {
        	assaultColumns[i] = new AssaultPixel();
        }
        
    	for (int j=0; j<MAX_LEDS_PER_STRIP; ++j)
    	{
    		assaultRows[j] = new AssaultPixel();
    	}
        
        allOutAssault = new AssaultPixel();
        
        countdownMode = new CountdownMode();
        countdownMode.start();
    }

    @Override
    public void onShow()
    {
        countdownMode.start();
    }

    @Override
    protected void updateState(long dtMillis)
    {
    	if (countdownMode.isCountingDown())
    	{
    		countdownMode.update(dtMillis);
    	}
    	else
    	{
    	switch (assaultMode)
    	{
    	case 0:
    		for (int i=0; i<MAX_LED_STRIPS; ++i)
            {
            	for (int j=0; j<MAX_LEDS_PER_STRIP; ++j)
            	{
            		pixels[i][j].update(dtMillis);
            	}
            }
    		break;
    	case 1:
    		for (int i=0; i<MAX_LED_STRIPS; ++i)
            {
    			assaultColumns[i].update(dtMillis);
            }
    		break;
    	case 2:
    		for (int j=0; j<MAX_LEDS_PER_STRIP; ++j)
        	{
        		assaultRows[j].update(dtMillis);
        	}
    		break;
    	case 3:
    		allOutAssault.update(dtMillis);
    		break;
    	};
            cycleTimer -= dtMillis;
            if (cycleTimer <= 0)
            {
               Cycle();
            }
            
    	}
    }
    
    @Override
    protected int getPixelColor(int x, int y, long dtMillis)
    {
    	if (countdownMode.isCountingDown())
    	{
    		return countdownMode.getColor(x, y);
    	}
    	else
    	{
    	switch (assaultMode)
    	{
    	case 0:
    		return pixels[x][y].getColor();
    	case 1:
    		return assaultColumns[x].getColor();
    	case 2:
    		return assaultRows[y].getColor();
    	case 3:
    		return allOutAssault.getColor();
    	};
    	}
    	return 0;
    }

    @Override
    protected int getColorModeForThisMode()
    {
    	return parent.HSB;
    }
    
    private void GoToMode(int newMode)
    {
    	if (newMode < numModes && newMode > 0)
    	{
    		assaultMode = newMode;
    	}
    }

    //
    // BEGIN Input Handlers
    //
    @Override
    protected void RightButtonPressed() 
    {
    	GoToMode(assaultMode+1 % numModes);
    }
    
    @Override
    protected void LeftButtonPressed()
    {
    	GoToMode(assaultMode-1 % numModes);
    }
    
    @Override
    protected void AButtonPressed()
    {
    	holyShitFuckTurnItDownBrightness = holyShitFuckTurnItDownBrightness == 255 ? 45 : 255;
    }
    
    @Override
    protected void BButtonPressed()
    {
    	//countdownMode.start();
    }

    private int cycleTimer = 500;
    private void Cycle()
    {
        System.out.println("Cycling Modes...");
        assaultMode++;
        if (assaultMode >= numModes)
        {
            assaultMode = 0;
        }
        cycleTimer = 500;
    }
    
    @Override
    protected void DownButtonPressed()
    {
    	if (holyShitFuckTurnItDownSpeed <= 1400)
    		holyShitFuckTurnItDownSpeed += 100;
    	else
    		holyShitFuckTurnItDownSpeed = 1500;
    }
    
    @Override
    protected void UpButtonPressed()
    {
    	if (holyShitFuckTurnItDownSpeed >= 150)
    		holyShitFuckTurnItDownSpeed -= 100;
    	else
    		holyShitFuckTurnItDownSpeed = 50;
    }
    //
    // END Input Handlers
    //
    
    
    private class CountdownMode
    {
    	private long countdownTimer;
    	private static final long COUNTDOWN_LENGTH = 8000;
    	
    	private CountdownMode()
    	{
    		countdownTimer = 0;
    	}
    	
    	public void start()
    	{
    		countdownTimer = COUNTDOWN_LENGTH;
            System.out.println("Starting Countdown to Optical Assault!");
    	}
    	
    	public boolean isCountingDown()
    	{
    		return countdownTimer > 0;
    	}
    	
    	private float percentOfCountdownComplete = 0.0f; 
    	private int upToColumnLit= 0;
    	public void update(long dtMillis)
    	{
    		if (countdownTimer > 0)
    		{
	    		countdownTimer -= dtMillis;
	    		if (countdownTimer < 0)
	    		{
	    			countdownTimer = 0;
	    		}
	    		
	    		percentOfCountdownComplete = 1.0f - ((float)countdownTimer / (float)COUNTDOWN_LENGTH);
	    		upToColumnLit = (int)(percentOfCountdownComplete * (float)MAX_LEDS_PER_STRIP);
    		}
    	}
    	
    	
    	public int getColor(int x, int y)
    	{
    		if (y <= upToColumnLit)
    		{
    			return parent.color(0,255,(int)(percentOfCountdownComplete*(float)205) + 50);
    		}
    		else
    		{
    			return parent.color(0,0,0);
    		}
    	}
    }    
    
    //
    // PRIVATE CLASS AssaultPixel
    //
    private class AssaultPixel
    {
    	private int hue = 0;
    	private long assaultTimer = 0;
    	
    	// This bitch don't fux around.
    	private AssaultPixel()
    	{
    		reset();
    	}
    	
    	private void reset()
    	{
	    	hue = rand.nextInt(255);
	    	assaultTimer = rand.nextInt(50) + holyShitFuckTurnItDownSpeed;
    	}
    	
    	public void update(long dtMillis)
    	{
    		assaultTimer -= dtMillis;
    		if (assaultTimer <= 0)
    		{
    			reset();
    		}
    	}
    	
    	public int getColor()
    	{
    		return parent.color(hue, 255, holyShitFuckTurnItDownBrightness);
    	}
    }
}


