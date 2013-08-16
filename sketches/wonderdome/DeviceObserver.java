import java.util.List;
import java.util.Observable;
import java.util.Observer;

import processing.core.*;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;

// Observer class for hardware detection.
class DeviceObserver implements Observer {

    private final PApplet parent;
    public boolean present = false;

    public DeviceObserver(PApplet parent) {
        this.parent = parent;
    }

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
