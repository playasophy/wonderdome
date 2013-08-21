#!/bin/zsh

# Start the X server if this is the local auto-login terminal.
if [[ -z "$DISPLAY" && $(tty) == /dev/tty6 ]]; then
    xinit
    logout
fi
