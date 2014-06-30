Wonderdome
==========

This is the codebase for the Playasophy Wonderdome project. The Wonderdome is an
LED art project which uses controller input to drive a light visualization
across a display made of individually color-controllable LEDs.

## Hardware

The Wonderdome is enclosed in a weather-resistant plastic shell with ports
mounted on the side to hook up power and networking. The system includes two
power supplies - one that accepts standard 120VAC generator power and another
that takes 12VDC from a deep-cycle battery. Both power supplies convert to the
5VDC that the rest of the system uses.

The LED display is built around the [HeroicRobotics PixelPusher](http://www.heroicrobotics.com/products/pixelpusher)
and [LPD8806 RGB LED strips](http://www.illumn.com/other-products/pixelpusher-and-led-strips.html).
The PixelPusher presents itself as a network device and listens for UDP
broadcasts to register with a controller. The controller can send UDP packets to
the PP to give pixel color-setting commands.

The computer 'brain' running the system is currently a [CubieBoard v1](http://cubieboard.org/).
An upgrade to an [ODROID-U3](http://hardkernel.com/main/products/prdt_info.php)
is in the works, as the cubieboard proved itself a bit underpowered in 2013.

## Software

There are two main components to the software stack; some JVM code using Clojure
and to drive the display, and a Ruby Sinatra app to provide a web interface and
admin controls.

TODO: more details

## Deployment

TODO: describe how to deploy

The Wonderdome system is engineered to be fairly fault-tolerant, and especially
to be self-activating as much as possible. Getting the system running should not
require anything except plugging it in and turning it on. The startup process is
as follows:

  1. Computer boots into Linux.
  2. `tty6` starts `mingetty`.
  3. `mingetty` automatically logs in the `wonder` user.
  4. The `wonder` user's `.zlogin` checks for the correct tty and calls `xinit`.
  5. The X server is started.
  6. The `wonder` user's `.xinitrc` execs the `start-wonderdome` script.
  7. `start-wonderdome` runs the Sinatra webserver with ruby.
  8. The webserver launches the JVM process and starts a health-check thread.
  9. JVM starts and loads the Wonderdome code.

On exit, or if the Wonderdome code crashes, the webserver's monitoring thread
should restart the process. If the webserver crashes (or the 'terminate' command
is given), the control flows back down the stack:

  1. `start-wonderdome` exits.
  2. `xinit` exits.
  3. The X server stops.
  4. `.zlogin` calls `logout`.
  5. `mingetty` exits.
  6. `tty6` respawns `mingetty`.

At which point, control returns to the third step in the start sequence.
Essentially, crashes should be self-repairing, and if necessary the system
should return to a good state after a power cycle.

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
