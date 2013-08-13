package org.playasophy.wonderdome;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import processing.core.*;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;


public class Wonderdome {

  private final PApplet parent;
  private DeviceRegistry registry;
  private DeviceObserver devices;
  private List<Strip> strips;
  private List<SimulatedStrip> simulatedStrips;
  private int numStrips;
  private int pixelsPerStrip;

  public Wonderdome(PApplet parent) {
    this.parent = parent;
    parent.registerMethod("draw", this);
    parent.registerMethod("dispose", this);
    initHardware();
    initSimulation();
  }



  ///// ACCESSORS/MUTATORS /////

  public int getNumStrips() {
    return numStrips;
  }

  public int getPixelsPerStrip() {
    return pixelsPerStrip;
  }



  ///// PUBLIC METHODS /////

  public void draw() {
    //long gfxStart = System.currentTimeMillis();
    if ( isSimulated() ) {
      for ( int s = 0; s < simulatedStrips.size(); s++ ) {
        simulatedStrips.get(s).draw();
      }
    } else {
      strips = registry.getStrips();
    }
    //gfxElapsed += System.currentTimeMillis() - gfxStart;
  }

  public void setPixel(int strip, int pixel, int color) {
    if ( isSimulated() ) {
      simulatedStrips.get(strip).setPixel(color, pixel);
    } else {
      strips.get(strip).setPixel(color, pixel);
    }
  }

  public void dispose() {
    // Anything in here will be called automatically when 
    // the parent sketch shuts down. For instance, this might
    // shut down a thread used by this library.
  }



  ///// PRIVATE METHODS /////

  private void initHardware() {
    registry = new DeviceRegistry();
    devices = new DeviceObserver();
    registry.addObserver(devices);
  }

  private void initSimulation() {
    if ( isSimulated() ) {

      // Set the window size to a reasonably large display.
      parent.size(300, 1000);

      numStrips = 6;
      pixelsPerStrip = 240;
      simulatedStrips = new ArrayList<SimulatedStrip>(numStrips);

      int stripWidth = parent.width / numStrips;
      for ( int i = 0; i < numStrips; i++ ) {
        SimulatedStrip strip = new SimulatedStrip(parent, (i * stripWidth) + (stripWidth / 2), 0, pixelsPerStrip);
        simulatedStrips.add(strip);
      }

    } else {
      parent.size(5, 5);
      simulatedStrips = null;
    }
  }

  private boolean isSimulated() {
    return !devices.present;
  }



  ///// PRIVATE CLASSES /////

  // Observer class for hardware detection.
  // TODO: Split out into separate source file? (No good reason for this to be an inner class...)
  class DeviceObserver implements Observer {
    public boolean present = false;

    @Override
    public void update(final Observable target, final Object device) {

      DeviceRegistry registry = (DeviceRegistry) target;

      if ( device != null ) {
        parent.println("Updated device: " + device);

        if ( !present ) {
          parent.println("Initializing device registry and starting pushing");
          registry.startPushing();
          registry.setExtraDelay(0);
          registry.setAutoThrottle(true);
        }

        present = true;
      } else {
        List<Strip> strips = registry.getStrips();

        if ( present && strips.isEmpty() ) {
          parent.println("Stopping pushing");
          registry.stopPushing();
        }

        present = !strips.isEmpty();
      }

    }

  }

}
