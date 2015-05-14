#!/bin/bash
GEMFIRE_SERVER_FILENAME="pivotal-gemfire-8.0.0.3-49715.el6.noarch.rpm"
GEMFIRE_DIRECTORY="/opt/pivotal/gemfire/Pivotal_GemFire_8003"

NATIVE_CLIENT_FILENAME="Pivotal_GemFire_NativeClient_Linux_64bit_8001_b6212.zip"
NATIVE_CLIENT_DIRECTORY="/opt/pivotal/gemfire/NativeClient_Linux_64bit_8001_b6212"

JAVA_RPM_FILENAME="jdk-7u65-linux-x64.rpm"
JAVA_RPM_URL="http://download.oracle.com/otn-pub/java/jdk/7u65-b17/$JAVA_RPM_FILENAME"

set -e

echo "Setting up Centos 6.5"

if ! yum -C repolist | grep epel ; then
  rpm --import https://fedoraproject.org/static/0608B895.txt
  rpm -Uvh http://download-i2.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm
fi

yum -y install yum-plugin-auto-update-debug-info.noarch

yum -y install \
  gcc-c++ \
  gdb \
  git \
  htop \
  man \
  openssl-devel \
  unzip

if [ ! -e $GEMFIRE_DIRECTORY ]; then
  if [ ! -e /vagrant/tmp/$GEMFIRE_SERVER_FILENAME ]; then
    echo "----------------------------------------------------"
    echo "Please download $GEMFIRE_SERVER_FILENAME"
    echo "from https://network.pivotal.io/products/pivotal-gemfire"
    echo "(Pivotal GemFire v8.0.0.1 Linux RH6 RPM - 8.0.0.1)"
    echo "and place it in the ./tmp subdirectory of this project."
    echo "Then re-run \`vagrant provision\`."
    echo "----------------------------------------------------"
    exit 1
  fi
  rpm -ivh /vagrant/tmp/$GEMFIRE_SERVER_FILENAME
fi

mkdir -p /vagrant/java/lib/
cp -f $GEMFIRE_DIRECTORY/lib/gemfire.jar /vagrant/java/lib/

if [ ! -e $NATIVE_CLIENT_DIRECTORY ]; then
  if [ ! -e /vagrant/tmp/$NATIVE_CLIENT_FILENAME ]; then
    echo "----------------------------------------------------"
    echo "Please download $NATIVE_CLIENT_FILENAME"
    echo "from https://network.pivotal.io/products/pivotal-gemfire"
    echo "(Pivotal GemFire Native Client Linux 64bit v8.0.0.3 - 8.0.0.3)"
    echo "and place it in the ./tmp subdirectory of this project."
    echo "Then re-run \`vagrant provision\`."
    echo "----------------------------------------------------"
    exit 1
  fi
  cd /opt/pivotal/gemfire
  unzip /vagrant/tmp/$NATIVE_CLIENT_FILENAME
fi

if [ ! -e /usr/bin/javac ]; then
  if [ ! -e /vagrant/tmp/$JAVA_RPM_FILENAME ]; then
    wget --no-verbose -O /vagrant/tmp/$JAVA_RPM_FILENAME --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" $JAVA_RPM_URL
  fi
  rpm -i --quiet /vagrant/tmp/$JAVA_RPM_FILENAME
fi

sh -c "cat > /etc/profile.d/gfcpp.sh" <<EOF
export GFCPP=$NATIVE_CLIENT_DIRECTORY
export GEMFIRE=$GEMFIRE_DIRECTORY
EOF
sh -c "cat >> /etc/profile.d/gfcpp.sh" <<'EOF'
export JAVA_HOME=/usr/java/default
export PATH=$GFCPP/bin:/usr/local/bin:$PATH
export LD_LIBRARY_PATH=$GFCPP/lib:$LD_LIBRARY_PATH
EOF
