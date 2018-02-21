#!/bin/sh
#This script will install ovs, mininet and JDK 8
#Use this script if you have ubuntu 14.04 or lower

if [ "$(id -u)" != "0" ]; then
    echo "This script must be run as root" 1>&2
    exit 1
fi


sudo apt install openvswitch-switch
sudo apt install openjdk-8-jdk
sudo apt install tmux
sudo apt install xterm

git clone git://github.com/mininet/mininet
cd mininet
git checkout 2.2.2
~/mininet/util/install.sh -a
