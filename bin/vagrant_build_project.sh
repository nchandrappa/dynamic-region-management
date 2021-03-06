#!/bin/sh
set -e

npm install -g jasmine grunt-cli

#NODE_GEMFIRE_FILENAME=`basename \`ls -c1 /vagrant/tmp/node-gemfire-*.tar.gz\``
NODE_GEMFIRE_FILENAME=node-gemfire-0.0.15.tar.gz

cd /vagrant

if [ ! -e /vagrant/tmp/$NODE_GEMFIRE_FILENAME ]; then
  echo "----------------------------------------------------"
  echo "Please get a copy of $NODE_GEMFIRE_FILENAME"
  echo "from someone at Pivotal (ask node-labs-gemfire@pivotal.io)"
  echo "and place it in the ./tmp subdirectory of this project"
  echo "Then re-run \`vagrant provision\`."
  echo "----------------------------------------------------"
  exit 1
fi
NODE_TLS_REJECT_UNAUTHORIZED=0 npm install /vagrant/tmp/$NODE_GEMFIRE_FILENAME

npm install

echo ""
echo ""
echo "Ready to go! Run the following to get started:"
echo ""
echo "$ vagrant ssh"
echo "$ cd /vagrant"
echo "$ grunt"
