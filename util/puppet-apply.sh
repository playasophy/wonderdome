#!/bin/bash -ex

module_version=$(grep version puppet/Modulefile | cut -d \' -f 2)
module_name="playasophy-wonderdome"
module_file="${module_name}-${module_version}.tar.gz"

cd puppet
puppet module build
scp pkg/$module_file wonderdome:/home/greg/

ssh wonderdome "sudo puppet module install -f ${module_file} && sudo puppet apply -e 'include wonderdome'"
