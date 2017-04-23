package playasophy.wonderdome.mode;

import clojure.lang.Associative;
import clojure.lang.Keyword;
import clojure.java.api.Clojure;
import java.util.Set;
import playasophy.wonderdome.util.Color;

/**
 * Proof-of-concept mode for edge-based coordinates.
 */
public class AsteriskMode implements Mode<AsteriskMode> {

    private final int[] colors;
    private final int index;
    private final int framesPerColor;
    private final int frameIndex;

    private static Keyword TYPE = (Keyword) Clojure.read(":type");
    private static Keyword INPUT = (Keyword) Clojure.read(":input");
    private static Keyword TIME_TICK = (Keyword) Clojure.read(":time/tick");
    private static Keyword GRAPH_EDGE = (Keyword) Clojure.read(":graph/edge");
    private static Keyword GRAPH_OFFSET = (Keyword) Clojure.read(":graph/offset");

    private static final Set<Keyword> ASTERISK_EDGES = (Set) Clojure.read(
            "#{:v1.v2, :v1.v3, :v1.v4, :v1.v5, :v1.v6}"
    );
    private static final int[] DEFAULT_COLORS = new int [] { Color.RED, Color.BLUE, Color.GREEN };

    public AsteriskMode() {
        this(DEFAULT_COLORS, 0, 1, 0);
    }

    public AsteriskMode(int[] colors, int index, int framesPerColor, int frameIndex) {
        this.colors = colors;
        this.index = index;
        this.framesPerColor = framesPerColor;
        this.frameIndex = frameIndex;
    }

    @Override
    public AsteriskMode update(Associative event) {
        if (TIME_TICK.equals(event.valAt(TYPE)) && event.valAt(INPUT) == null) {
            if (frameIndex >= framesPerColor) {
                int newIndex = (index + 1) % colors.length;
                return new AsteriskMode(colors, newIndex, framesPerColor, 1);
            } else {
                return new AsteriskMode(colors, index, framesPerColor, frameIndex + 1);
            }
        } else {
            // Return this instance unchanged, regardless of the event.
            return this;
        }
    }

    @Override
    public int render(Associative pixel) {
        EdgeAndOffset edgeAndOffset = getEdgeAndOffset(pixel);
        if (isOnAsterisk(edgeAndOffset.edge, edgeAndOffset.offset)) {
            return colors[index];
        } else {
            return Color.NONE;
        }
    }

    private static EdgeAndOffset getEdgeAndOffset(Associative pixel) {
        Keyword edge = (Keyword) pixel.valAt(GRAPH_EDGE);
        int offset = ((Number) pixel.valAt(GRAPH_OFFSET)).intValue();
        return new EdgeAndOffset(edge, offset);
    }

    private static boolean isOnAsterisk(Keyword edge, int offset) {
        return ASTERISK_EDGES.contains(edge);
    }

    private static class EdgeAndOffset {

        public final Keyword edge;
        public final int offset;

        public EdgeAndOffset(Keyword edge, int offset) {
            this.edge = edge;
            this.offset = offset;
        }

    }

}
