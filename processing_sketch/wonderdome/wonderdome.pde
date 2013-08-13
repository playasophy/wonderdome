/* This is a simple learning sketch to exercise the WonderDome framework
 * and drive a simple color cycle along the attached strips.
 *
 * vim: ft=java
 * @author Greg Look (greg@playasophy.org)
 * @author Kevin Litwack (kevin.litwack@gmail.com)
 */

import hypermedia.net.*;
import org.playasophy.wonderdome.Wonderdome;

Wonderdome wonderdome;

// Set up the sketch.
void setup() {
  wonderdome = new Wonderdome(this);
}

// Rendering loop.
void draw() {
  wonderdome.getProgram().update(wonderdome.getPixelMatrix());
}
