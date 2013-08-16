/**
 * This interface defines an output which can render a matrix of pixels.
 *
 * @author Kevin Litwack
 */
interface PixelOutput {

    /**
     * Renders a matrix of pixels to this output.
     *
     * @param pixels  2D array of colors
     */
    void draw(int[][] pixels);

}
