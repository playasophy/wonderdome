#!/bin/bash -e

host=wonderdome

admin_user=greg
admin=$admin_user@$host

srv_user=wonder
srv=$srv_user@$host
srv_home=$srv:/srv/wonder

jar_name="wonderdome"
config_file="config.clj"


echo "Packaging code..."
lein uberjar

echo "Backing up deployed jar..."
ssh $srv "cp /srv/wonder/wonderdome.jar /srv/wonder/wonderdome.jar.bak"

echo "Stopping service..."
ssh $admin "sudo service wonderdome stop"

echo "Deploying uberjar..."
scp target/uberjar/$jar_name-*-standalone.jar $srv_home/wonderdome.jar

echo "Deploying native libraries..."
scp -r target/native/linux/ $srv_home/lib/

echo "Deploying configuration..."
scp $config_file $srv_home/$config_file

echo "Restarting service..."
ssh $admin "sudo service wonderdome start"
