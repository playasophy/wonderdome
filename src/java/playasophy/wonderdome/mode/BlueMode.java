package playasophy.wonderdome.mode;


import clojure.lang.Associative;

import java.awt.Color;


/**
 * A ludicrously simple test mode. As the name implies, it just sets every
 * pixel to pure blue and ignores all inputs.
 */
public class BlueMode implements Mode<BlueMode> {

    @Override
    public BlueMode update(Associative event) {
        // Return this instance unchanged, regardless of the event.
        return this;
    }

    @Override
    public int render(Associative pixel) {
        // All blue, all the time.
        return Color.BLUE.getRGB();
    }

}
