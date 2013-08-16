import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import processing.core.*;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;


/**
 * This class renders pixels to the PixelPusher hardware.
 *
 * @author Kevin Litwack
 * @author Greg Look
 */
class PixelPusherOutput implements PixelOutput {

    /** Device registry to interact with. */
    private final DeviceRegistry registry;

    /** Current list of attached strips. */
    private final List<Strip> strips = new ArrayList<Strip>();


    /**
     * Construct a new PixelPusher output with the given device registry.
     *
     * @param registry  hardware registry
     */
     public PixelPusherOutput(final DeviceRegistry registry) {

        this.registry = registry;

        Observer deviceObserver = new Observer() {
            @Override
            public void update(final Observable target, final Object arg) {
                updateStrips(arg);
            }
        };

        registry.addObserver(deviceObserver);

    }


    @Override
    public void draw(final int[][] pixels) {
        synchronized(strips) {
            if ( strips.isEmpty() ) return;

            int count = Math.min(strips.size(), pixels.length);
            for ( int i = 0; i < count; i++ ) {
                drawStrip(strips.get(i), pixels[i]);
            }
        }
    }


    private void drawStrip(Strip strip, int[] pixels) {
        int count = Math.min(strip.getLength(), pixels.length);
        for ( int i = 0; i < count; i++ ) {
            drawPixel(strip, i, pixels[i]);
        }
    }


    private void drawPixel(Strip strip, int index, int c) {
        strip.setPixel(c, index);
    }


    /**
     * Updates the view of the attached strips by querying the library.
     *
     * @param device  device object which was updated
     */
    private void updateStrips(final Object device) {

        List<Strip> newStrips = registry.getStrips();

        if ( device != null ) {

            System.out.println("Updated device: " + device);

            if ( strips.isEmpty() ) {
                System.out.println("Initializing device registry and starting pushing");
                registry.startPushing();
                registry.setExtraDelay(0);
                registry.setAutoThrottle(true);
            }

        } else if ( !strips.isEmpty() && newStrips.isEmpty() ) {
            System.out.println("Stopping pushing");
            registry.stopPushing();
        }

        synchronized(strips) {
            strips.clear();
            strips.addAll(newStrips);
        }

    }

}
