import processing.core.*;


/**
 * This output simulates the Wonderdome by rendering the pixels to a graphical
 * display.
 *
 * @author Kevin Litwack
 * @author Greg Look
 */
class PixelGraphicsOutput implements PixelOutput {

  private final PGraphics graphics;
  public PGraphics getGraphics() { return graphics; }


  /**
   * Constructs a new graphics renderer for pixels.
   *
   * @param graphics  graphics buffer to render to
   */
  public PixelGraphicsOutput(final PGraphics graphics) {

    this.graphics = graphics;

    //parent.size(140, 520);

    graphics.background(0);

  }


  @Override
  public void draw(final int[][] pixels) {

    if (( pixels == null ) || ( pixels.length == 0 )) return;

    int spacing = graphics.width/(pixels.length + 1);

    graphics.loadPixels();

    for ( int i = 0; i < pixels.length; i++ ) {
      drawStrip((i + 1)*spacing, spacing, pixels[i]);
    }

    graphics.updatePixels();

  }


  private void drawStrip(final int x, final int y, final int[] pixels) {
    for ( int i = 0; i < pixels.length; i++ ) {
      drawPixel(x, y + (i * 2), pixels[i]);
    }
  }


  private void drawPixel(int x, int y, int c) {
    graphics.pixels[x + (y * graphics.width)] = c;
  }

}
