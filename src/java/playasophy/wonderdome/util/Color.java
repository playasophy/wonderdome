package playasophy.wonderdome.util;

public final class Color {

    public static final int NONE = 0;
    public static final int BLACK = NONE;
    public static final int WHITE = rgb(1, 1, 1);
    public static final int RED = rgb(1, 0, 0);
    public static final int GREEN = rgb(0, 1, 0);
    public static final int BLUE = rgb(0, 0, 1);
    public static final int YELLOW = rgb(1, 1, 0);
    public static final int TEAL = rgb(0, 1, 1);
    public static final int MAGENTA = rgb(1, 0, 1);
    public static int gray(float l) {
        return rgb(l, l, l);
    }

    public static int rgb(float r, float g, float b) {
        return new java.awt.Color(r, g, b).getRGB();
    }

    public static int blendRgb(float p, int x, int y) {
        checkNormalizedArgument(p, "Blend ratio must be in [0.0, 1.0]");
        // TODO: Add implementation.
        throw new UnsupportedOperationException("Not implemented");
    }

    public static int hsv(float h, float s, float v) {
        checkHsvArguments(h, s, v);
        return java.awt.Color.HSBtoRGB(h, s, v);
    }

    private static void checkHsvArguments(float h, float s, float v) {
        checkNormalizedArgument(s, "Saturation must be in [0.0, 1.0]");
        checkNormalizedArgument(v, "Value (a.k.a. brightness) must be in [0.0, 1.0]");
    }

    private static void checkNormalizedArgument(float x) {
        checkNormalizedArgument(x, "Argument must be in [0.0, 1.0], but was: " + x);
    }
    private static void checkNormalizedArgument(float x, String message) {
        if (!(0.0 <= x && x <= 1.0)) {
            new IllegalArgumentException(message);
        }
    }

}
