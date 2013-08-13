package org.playasophy.wonderdome;

import processing.core.*;

class SimulatedStrip {

  public final PApplet parent;
  public final int x;
  public final int y;
  public final int numPixels;
  public final int[] currentPixels;
  public final int totalWidth;
  public final int totalHeight;
  public final int background;
  public final int border;
  public static final int PIXEL_SIZE = 2;
  public static final int PIXEL_PADDING = 2;

  public SimulatedStrip(PApplet parent, int x, int y, int numPixels) {
    this.parent = parent;
    this.x = x;
    this.y = y;
    this.numPixels = numPixels;
    this.currentPixels = new int[numPixels];
    this.totalWidth = PIXEL_SIZE + (2 * PIXEL_PADDING) + 2;
    this.totalHeight = (numPixels * PIXEL_SIZE) + ((numPixels + 1) * PIXEL_PADDING) + 2;
    parent.colorMode(parent.HSB, 100);
    this.background = parent.color(0, 0, 75);
    this.border = parent.color(0, 0, 0);
  }

  public void setPixel(int c, int index) {
    // TODO: Bounds-checking on index.
    currentPixels[index] = c;
  }

  public void draw() {
    //long gfxStart = System.currentTimeMillis();
    parent.fill(background);
    parent.stroke(border);
    parent.rect(x, y, totalWidth, totalHeight);
    for ( int p = 0; p < numPixels; p++ ) {
      int c = currentPixels[p];
      parent.fill(c);
      parent.stroke(c);
      parent.rect(x + PIXEL_PADDING + 1, y + (p * (PIXEL_SIZE + PIXEL_PADDING)) + PIXEL_PADDING + 1, PIXEL_SIZE, PIXEL_SIZE);
    }
    //gfxElapsed += System.currentTimeMillis() - gfxStart;
  }

}
