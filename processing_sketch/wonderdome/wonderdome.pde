/* This is a simple learning sketch to exercise the WonderDome framework
 * and drive a simple color cycle along the attached strips.
 *
 * vim: ft=java
 * @author Greg Look (greg@playasophy.org)
 * @author Kevin Litwack (kevin.litwack@gmail.com)
 */

import hypermedia.net.*;
import org.playasophy.wonderdome.Wonderdome;


class ColorGradient {

  // Definitions of the color scale, density, and gradient resolution.
  public final float density;
  public final float shades;
  public final color[] colors;

  public ColorGradient(float density, float shades, color... colors) {
    this.density = density;
    this.shades = shades;
    this.colors = colors;
  }

  // Determines the color to assign to the given escape time.
  color getColor(float t) {
    t = (t*density % shades)*(colors.length/shades);

    int t0 = (int)floor(t);
    int t1 = (int)ceil(t);

    color c0 = ( t0 >= colors.length ) ? colors[colors.length - 1] : colors[t0];
    color c1 = ( t1 >= colors.length ) ? colors[0] : colors[t1];

    return lerpColor(c0, c1, t - t0);
  }

}


// Sketch globals.
int scale = 4;
int brightness = 196;
float offset = 0.0;
float speed = 0.05;

ColorGradient gradient = new ColorGradient(30.0, 240.0,
  color(  0,   0,   0),
  color(255,   0, 255),
  color(  0, 128, 255),
  color(  0, 255,   0),
  color(255, 128,   0),
  color(255,   0,   0),
  color(128,   0,   0),
  color(255, 255, 255)
);

Wonderdome wonderdome;

// Set up the sketch.
void setup() {
  wonderdome = new Wonderdome(this);
}


// primitive profiling metrics.
int profileCycles = 100;
int cycles = 0;
long totalElapsed = 0L;
long gfxElapsed = 0L;
long pixelElapsed = 0L;

// Rendering loop.
void draw() {

  long totalStart = System.currentTimeMillis();

  // Iterate over strips and set pixels to appropriate spot in gradient.
  int numStrips = wonderdome.getNumStrips();
  int pixelsPerStrip = wonderdome.getPixelsPerStrip();
  for ( int s = 0; s < numStrips; s++ ) {
    for ( int p = 0; p < pixelsPerStrip; p++ ) {
      color c = gradient.getColor(offset + (float)p/pixelsPerStrip);
      wonderdome.setPixel(s, p, c);
    }
  }
  
  // Call the wonderdome to draw the strips, if necessary.
  //wonderdome.draw();

  totalElapsed += System.currentTimeMillis() - totalStart;

  // Report profiling.
  // TODO: Fix profiling.
  if ( cycles % profileCycles == 0 ) {
      System.out.printf("cycle %d: ~%d ms/cycle (%.2f cps), %d ms gfx (%.2f%%), %d ms pixels (%.2f%%)\n",
              cycles, totalElapsed/profileCycles,
              1000.0*profileCycles/totalElapsed,
              gfxElapsed/profileCycles,
              100.0*gfxElapsed/totalElapsed,
              pixelElapsed/profileCycles,
              100.0*pixelElapsed/totalElapsed);
      gfxElapsed = 0L;
      pixelElapsed = 0L;
      totalElapsed = 0L;
  }
  cycles++;

  offset += speed;
}
