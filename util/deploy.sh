#!/bin/bash -e

# Note that this relies on the wonder user being able to control the wonderdome
# service without a password. This requires a sudoers directive like:
#
#     wonder  ALL=(root) NOPASSWD:/usr/sbin/service wonderdome *

host=wonderdome

srv_user=wonder
srv=$srv_user@$host
srv_home=$srv:/srv/wonder

jar_name="wonderdome"
config_file="config.clj"

echo "Packaging code..."
[[ -f target/uberjar/$jar_name-*-standalone.jar ]] || lein uberjar

echo "Backing up deployed jar..."
ssh $srv "cp /srv/wonder/wonderdome.jar /srv/wonder/wonderdome.jar.bak"

echo "Stopping service..."
ssh $srv "sudo service wonderdome stop"

echo "Deploying uberjar..."
scp target/uberjar/$jar_name-*-standalone.jar $srv_home/wonderdome.jar

echo "Deploying native libraries..."
scp -r target/native/linux/ $srv_home/lib/

echo "Deploying configuration..."
scp $config_file $srv_home/$config_file

echo "Restarting service..."
ssh $srv "sudo service wonderdome start"
