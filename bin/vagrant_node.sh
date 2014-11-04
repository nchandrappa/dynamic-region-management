#!/bin/bash

set -e

(curl https://raw.githubusercontent.com/creationix/nvm/v0.17.3/install.sh | bash) || true
source ~/.nvm/nvm.sh

echo "installing node 0.10"
output=`nvm install 0.10 2>&1` || (echo $output 1>&2 && exit 1)
nvm exec 0.10 npm install -g grunt-cli
nvm alias default 0.10
