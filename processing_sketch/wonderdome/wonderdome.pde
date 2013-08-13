/* This is a simple learning sketch to exercise the WonderDome framework
 * and drive a simple color cycle along the attached strips.
 *
 * vim: ft=java
 * @author Greg Look (greg@playasophy.org)
 * @author Kevin Litwack (kevin.litwack@gmail.com)
 */

import hypermedia.net.*;
import org.playasophy.wonderdome.Wonderdome;


final int PORT_RX = 50000;
final String HOST_IP = "localhost";

Wonderdome wonderdome;
UDP udp;

// Set up the sketch.
void setup() {
  
  // Initialize the wonderdome object.
  wonderdome = new Wonderdome(this);
  
  // Set up UDP event handling.
  udp = new UDP(this, PORT_RX, HOST_IP);
  udp.log(true);
  udp.listen(true);
  //noLoop();
  
}

// Rendering loop.
void draw() {
  wonderdome.getProgram().update(wonderdome.getPixelMatrix());
}

void receive(byte[] data, String HOST_IP, int PORT_RX){
  String value = new String(data);
  println(value);
}
