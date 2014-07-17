Glossary
========

### Pixel

A single individually-controllable LED. Pixels are uniquely addressed by the
strip they are part of and their index along the strip.

### Strip

A strip is a linear collection of pixels. Strips are logically contiguous but
may not be _physically_ contiguous. For example, a single strip can run back and
forth to make a grid, but from the perspective of the system is still a linear
sequence of pixels.

Each PixelPusher can drive up to eight strips, and each of the strips has 240
pixels.

### Layout

The _layout_ maps pixels to a physical location. For example, in the old linear
layout, each pixel is placed along an arc across the surface of the dome. The
coordinates set by the layout are passed to the active mode, which can choose to
use either the canonical strip/index coordinate or the physical pixel location
to set the color.

Pixel locations are given in both the familiar _cartesian_ `[x y z]` coordinate
vector as well as
[_spherical coordinates_](http://en.wikipedia.org/wiki/Spherical_coordinate_system),
with the special case that the radius is generally a fixed size because the
strips are laid against the surface of a geodesic dome. Spherical coordinates
have three components:
- _r_: the radius of the pixel, in meters.
- _theta_: the _polar angle_ from straight vertical (the _pole_) in radians.
- _phi_: the _azimuthal angle_ around the pole, in radians.

### Event

An _event_ is some input that is given to the system to update its state. The
most common event type is a `:dt` timer tick, or user input in the form of
button events from the gamepad controller or web interface. Another type of
event could contain frames of audio data.

### Mode

A _mode_ is a visualization which contains some state and renders pixel colors
based on their coordinates. Mode states can be updated over time or in response
to input events.

### Handler

An event handler is a function which takes the current system state and an input
event and returns a new system state in response to the event. The primary
handler is `update-mode`, which finds the current mode in the system and updates
its state by calling `mode/update` with the event.

This gets more complex when _middleware_ is used. Middleware functions wrap
another event handler with some additional logic; for example, a `switch-modes`
middleware could catch presses of the select button and rotate the current mode
instead of passing the event down to the mode for updating.

### Display

A display contains a number of pixels and provides a target for setting the
pixel colors. The two primary displays are a Processing sketch for local
development, and the PixelPusher output for rendering to the actual LED
hardware.
