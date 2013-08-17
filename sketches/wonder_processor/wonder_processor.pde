/* This is a simple learning sketch to exercise the WonderDome framework
 * and drive a simple color cycle along the attached strips.
 *
 * vim: ft=java
 * @author Greg Look (greg@playasophy.org)
 * @author Kevin Litwack (kevin.litwack@gmail.com)
 */

import hypermedia.net.UDP;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;

import org.playasophy.wonderdome.Wonderdome;

final boolean DEBUG = false;
Wonderdome wonderdome;
PixelOutput output;

// Set up the sketch.
void setup() {

    // Initialize the wonderdome object.
    wonderdome = new Wonderdome(this);

    // Set up UDP event handling.
    UDPInput udp = new UDPInput(wonderdome);

    // Initialize output (either to hardware or simulated to the display).
    if ( false ) {
        DeviceRegistry registry = new DeviceRegistry();
        output = new PixelPusherOutput(registry);
    } else {
        // TODO: Don't hardcode the display size.
        size(140, 520);
        output = new PixelGraphicsOutput(g);
    }

}



// Profiling variables.
private int cycles = 0;
private static final int PROFILE_UNIT_CYCLE_COUNT = 100;
private long profileUnitStart = System.currentTimeMillis();


// Rendering loop.
void draw() {

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
