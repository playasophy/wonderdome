package org.playasophy.wonderdome;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import processing.core.*;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;

import org.playasophy.wonderdome.program.ColorCycle;
import org.playasophy.wonderdome.program.Program;


public class Wonderdome {

    private final PApplet parent;
    private DeviceRegistry registry;
    private DeviceObserver devices;
    private List<Strip> strips;
    private SimulatedPixelMatrix simulation;
    private Program currentProgram;

    // Profiling variables.
    private int cycles = 0;
    private int profileCycles = 100;
    private long gfxElapsed = 0L;
    private long pixelElapsed = 0L;
    private long totalElapsed = 0L;



    ///// INITIALIZATION /////

    public Wonderdome(PApplet parent) {
        this.parent = parent;
        parent.registerMethod("draw", this);
        parent.registerMethod("dispose", this);
        initHardware();
        simulation = new SimulatedPixelMatrix(parent);
        // TODO: Don't hardcode the program.
        currentProgram = new ColorCycle(parent);
    }



    ///// PUBLIC METHODS /////

    public void draw() {

        long totalStart = System.currentTimeMillis();
        if ( isSimulated() ) {
            long gfxStart = System.currentTimeMillis();
            simulation.draw();
            gfxElapsed += System.currentTimeMillis() - gfxStart;
        } else {
            long pixelStart = System.currentTimeMillis();
            strips = registry.getStrips();
            pixelElapsed += System.currentTimeMillis() - pixelStart;
        }
        totalElapsed += System.currentTimeMillis() - totalStart;

        // TODO: Only perform profiling if requested via environment variable (or some other
        // runtime-configurable state).
        updateProfiling();

    }

    public void dispose() {
        // Anything in here will be called automatically when 
        // the parent sketch shuts down. For instance, this might
        // shut down a thread used by this library.
    }

    public PixelMatrix getPixelMatrix() {
        if ( isSimulated() ) {
            return simulation;
        } else {
            // TODO: return a hardware PixelMatrix.
            throw new UnsupportedOperationException("Hardware PixelMatrix not implemented yet");
        }
    }

    public Program getProgram() {
        return currentProgram;
    }



    ///// PRIVATE METHODS /////

    private void initHardware() {
        registry = new DeviceRegistry();
        devices = new DeviceObserver();
        registry.addObserver(devices);
    }

    private boolean isSimulated() {
        return !devices.present;
    }

    private void updateProfiling() {

        // Report profiling.
        if ( cycles % profileCycles == 0 ) {

            System.out.printf("cycle %d: ~%d ms/cycle (%.2f cps), %d ms gfx (%.2f%%), %d ms pixels (%.2f%%)\n",
                cycles,
                totalElapsed / profileCycles,
                1000.0 * profileCycles / totalElapsed,
                gfxElapsed / profileCycles,
                100.0 * gfxElapsed / totalElapsed,
                pixelElapsed / profileCycles,
                100.0 * pixelElapsed / totalElapsed);

            gfxElapsed = 0L;
            pixelElapsed = 0L;
            totalElapsed = 0L;

        }

        cycles++;

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
