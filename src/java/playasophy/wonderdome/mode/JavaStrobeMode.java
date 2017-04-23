package playasophy.wonderdome.mode;

import clojure.lang.Associative;
import clojure.lang.Keyword;
import clojure.java.api.Clojure;
import playasophy.wonderdome.util.Color;

/**
 * A Java port of the (in)famous Strobe Mode; mainly just exists to test events in a Java mode.
 */
public class JavaStrobeMode implements Mode<JavaStrobeMode> {

    private final int[] colors;
    private final int index;
    private final int framesPerColor;
    private final int frameIndex;

    private static final int[] DEFAULT_COLORS = new int [] { Color.RED, Color.BLUE, Color.GREEN };

    public JavaStrobeMode() {
        this(DEFAULT_COLORS, 0, 1, 0);
    }

    public JavaStrobeMode(int[] colors, int index, int framesPerColor, int frameIndex) {
        this.colors = colors;
        this.index = index;
        this.framesPerColor = framesPerColor;
        this.frameIndex = frameIndex;
    }

    @Override
    public JavaStrobeMode update(Associative event) {
        Keyword type = (Keyword) Clojure.read(":type");
        Keyword input = (Keyword) Clojure.read(":input");
        Keyword timeTick = (Keyword) Clojure.read(":time/tick");
        if (timeTick.equals(event.valAt(type)) && event.valAt(input) == null) {
            if (frameIndex >= framesPerColor) {
                int newIndex = (index + 1) % colors.length;
                return new JavaStrobeMode(colors, newIndex, framesPerColor, 1);
            } else {
                return new JavaStrobeMode(colors, index, framesPerColor, frameIndex + 1);
            }
        } else {
            // Return this instance unchanged, regardless of the event.
            return this;
        }
    }

    @Override
    public int render(Associative pixel) {
        return colors[index];
    }

}
