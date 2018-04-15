#!/bin/sh
#This script will install ovs and mininext
#Use this script if you have ubuntu 14.04 or lower

if [ "$(id -u)" != "0" ]; then
    echo "This script must be run as root" 1>&2
    exit 1
fi

#Install JDK 8
sudo apt-get update && sudo apt-get install software-properties-common && sudo add-apt-repository ppa:openjdk-r/ppa && sudo apt-get update && sudo apt-get install openjdk-8-jdk

#Install OVS
sudo apt-get install openvswitch-switch

git clone git://github.com/mininet/mininet
cd mininet
git checkout 2.2.2
~/mininet/util/install.sh -a
