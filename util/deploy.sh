#!/bin/bash -e

user=wonder
host=wonderdome
dest=$user@$host
home=$dest:/srv/wonder
jar_name="wonderdome"
config_file="config.clj"


echo "Packaging code..."
lein uberjar

echo "Stopping service..."
ssh $host "sudo service wonderdome stop"

echo "Deploying uberjar..."
scp target/uberjar/$jar_name-*-standalone.jar $home/wonderdome.jar

echo "Deploying native libraries..."
scp -r target/native/linux/ $home/lib/

echo "Deploying configuration..."
scp $config_file $home/$config_file

echo "Restarting service..."
ssh $host "sudo service wonderdome start"
