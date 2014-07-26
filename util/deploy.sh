#!/bin/bash -ex

jar_version="1.0.0-SNAPSHOT"
jar_name="wonderdome"
uberjar_file="${jar_name}-${jar_version}-standalone.jar"

lein uberjar
scp target/$uberjar_file wonder@wonderdome:/srv/wonder/wonderdome.jar

curl -i -X POST http://wonderdome/admin -d "button=restart"
