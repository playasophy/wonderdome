#!/bin/bash

if which scalac 2>&1 > /dev/null; then
    echo "Compiling scala files..."
    scalac \
        -classpath $(lein classpath) \
        -d target/classes \
        src/scala/*.scala
else
    echo "No scalac found, skipping."
fi
