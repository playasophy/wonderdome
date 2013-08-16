import java.util.List;

import processing.core.*;

import com.heroicrobot.dropbit.devices.pixelpusher.Strip;

class HardwarePixelOutput implements PixelOutput {

  private final int[][] pixelMatrix;
  private final List<Strip> strips;

  public HardwarePixelOutput(List<Strip> strips, int[][] pixelMatrix) {
    this.strips = strips;
    this.pixelMatrix = pixelMatrix;
  }

  public void draw() {
    int numStripsToDraw = Math.min(strips.size(), pixelMatrix.length);
    for ( int i = 0; i < numStripsToDraw; i++ ) {
      drawStrip(strips.get(i), pixelMatrix[i]);
    }
  }

  private void drawStrip(Strip strip, int[] pixelArray) {
    int numPixelsToDraw = Math.min(strip.getLength(), pixelArray.length);
    for ( int i = 0; i < numPixelsToDraw; i++ ) {
      drawPixel(strip, i, pixelArray[i]);
    }
  }

  private void drawPixel(Strip strip, int index, int c) {
    strip.setPixel(c, index);
  }

}
