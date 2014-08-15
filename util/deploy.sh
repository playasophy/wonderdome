#!/bin/bash -e

user=wonder
host=wonderdome
dest=$user@$host
home=$dest:/srv/wonder
jar_name="wonderdome"
config_file="config.clj"


echo "Packaging code..."
lein uberjar

echo "Deploying uberjar..."
scp target/uberjar/$jar_name-*-standalone.jar $home/wonderdome.jar

echo "Deploying native libraries..."
scp -r target/native/linux/ $home/lib/

echo "Deploying configuration..."
scp $config_file $home/$config_file

echo "Restarting service..."
#curl -i -X POST http://wonderdome/admin -d "button=restart"
ssh $host "sudo service wonderdome restart"
