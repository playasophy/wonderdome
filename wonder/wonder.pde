/* This is a simple learning sketch to exercise the WonderDome framework
 * and drive a simple color cycle along the attached strips.
 *
 * vim: ft=java
 * @author Greg Look (greg@playasophy.org)
 * @author Kevin Litwack (kevin.litwack@gmail.com)
 */

import ddf.minim.*;
import ddf.minim.analysis.*;

import java.util.Map;

import hypermedia.net.UDP;
import com.codeminders.hidapi.*;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;

import org.playasophy.wonderdome.Wonderdome;



///// GLOBAL STATE /////

final boolean DEBUG = false;

Wonderdome wonderdome;

NESControllerInput controller;

Minim minim;
AudioInput audio;

PixelOutput output;

// Profiling variables.
int cycles = 0;
static final int PROFILE_UNIT_CYCLE_COUNT = 100;
long profileUnitStart = System.currentTimeMillis();



///// LIFECYCLE /////

// Set up the sketch.
void setup() {

    // Initialize Minim and get an audio input.
    minim = new Minim(this);
    //audio = minim.getLineIn();

    // Initialize the wonderdome object.
    wonderdome = new Wonderdome(this, audio);

    // Set up UDP event handling.
    UDPInput udp = new UDPInput(wonderdome);
    System.out.println("UDPInput constructed successfully");

    // Try to set up a USB NES controller input.
    controller = NESControllerInput.getController(wonderdome);
    if ( controller != null ) {
        System.out.println("NESControllerInput constructed successfully");
    } else {
        System.out.println("NESControllerInput not constructed");
    }

    // Initialize output based on environment variable.
    if ( System.getenv("WONDERDOME_USE_HARDWARE") != null ) {
        System.out.println("Attempting to use wonderdome hardware");
        // Set the graphical display size to a single pixel. This will ensure
        // minimal resources are spent on rendering the blank canvas.
        size(1, 1);
        DeviceRegistry registry = new DeviceRegistry();
        output = new PixelPusherOutput(registry);
    } else {
        System.out.println("Using simulated graphics output");
        // TODO: Don't hardcode the display size.
        size(140, 520);
        output = new PixelGraphicsOutput(g);
    }

}


// Rendering loop.
void draw() {

    // Update the state of the NES controller, if present.
    if ( controller != null ) {
        controller.updateState();
    }

    output.draw(wonderdome.getPixels());

    // Report profiling if the desired number of cycles has elapsed.
    if ( cycles > 0 && cycles % PROFILE_UNIT_CYCLE_COUNT == 0 ) {

        long profileUnitElapsed = System.currentTimeMillis() - profileUnitStart;

        if ( DEBUG ) {
            System.out.printf("cycle %d: ~%d ms/cycle (%.2f cps)\n",
                cycles,
                profileUnitElapsed / PROFILE_UNIT_CYCLE_COUNT,
                1000.0 * PROFILE_UNIT_CYCLE_COUNT / profileUnitElapsed);
        }

        profileUnitStart = System.currentTimeMillis();

    }

    cycles++;

}


// Free sketch resources.
void stop() {

  if ( audio != null ) audio.close();
  minim.stop();

  super.stop();

}
