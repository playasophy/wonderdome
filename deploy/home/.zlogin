#!/bin/zsh

# Enable rbenv for local ruby versioning.
if [[ -d "$HOME/.rbenv" ]]; then
    unset RUBYOPT
    path=("$HOME/.rbenv/bin" $path)
    eval "$(rbenv init -)"
fi

# Start the X server if this is the local auto-login terminal.
if [[ -z "$DISPLAY" && $(tty) == /dev/tty6 ]]; then
    xinit
    logout
fi
