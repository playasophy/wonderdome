import processing.core.*;

class SimulatedPixelOutput implements PixelOutput {

  private final PApplet parent;
  private final int[][] pixelMatrix;
  private final int numStrips;
  private final int pixelsPerStrip;

  public SimulatedPixelOutput(PApplet parent, int[][] pixelMatrix) {
    this.parent = parent;
    // FIXME: Don't hardcode the parent size.
    parent.size(140, 520);
    parent.background(0);
    
    this.pixelMatrix = pixelMatrix;
    // TODO: Validate the assumption made here that all of the strips have the same length.
    numStrips = pixelMatrix.length;
    pixelsPerStrip = pixelMatrix[0].length;
  }

  public void draw() {
    parent.loadPixels();
    for ( int i = 0; i < numStrips; i++ ) {
      drawStrip(20 + (i * 20), 20, pixelMatrix[i]);
    }
    parent.updatePixels();
  }

  private void drawStrip(int x, int y, int[] pixelArray) {
    for ( int i = 0; i < pixelsPerStrip; i++ ) {
      drawPixel(x, y + (i * 2), pixelArray[i]);
    }
  }

  private void drawPixel(int x, int y, int c) {
    parent.pixels[x + (y * parent.width)] = c;
  }

}
