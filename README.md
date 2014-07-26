Wonderdome
==========

This is the codebase for the Playasophy Wonderdome project. The Wonderdome is an
LED art project which uses various inputs to drive a light-based visualization
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

The software driving the display is Clojure code running on the JVM. The overall
system is composed of many individual components communicating via `core.async`
channels. The system data-flow diagram can be found
[here](http://www.playasophy.org/media/wonderdome/system-processes.svg);
you can use the following command to render it locally:

```bash
dot -Tsvg < doc/system-processes.dot > target/system-processes.svg
```

The layout of the code and the interelation of the project's namespaces can be
visualized using the `lein-hiera` plugin and found in `target/ns-hierarchy.png`:

```bash
lein hiera
```

See the [glossary](doc/glossary.md) for a general overview of the terminology
and components used in the system.

See the [developer docs](doc/developing.md) to get started working with the
code base.

## Deployment

The Wonderdome system is engineered to be fairly fault-tolerant, and especially
to be self-activating as much as possible. Getting the system running should not
require anything except plugging it in and turning it on.

The host is configured by a [Puppet module](puppet), which sets up the
environment necessary to run the software. The Wonderdome code is packaged up
with all its dependencies into an 'uberjar' by Leiningen. The jar is run as an
Upstart script which respawns the process whenever it is terminated.

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
