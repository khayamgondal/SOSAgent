#!/bin/sh
#This script will install ovs 2.3.1 and mininext
if [ "$(id -u)" != "0" ]; then
    echo "This script must be run as root" 1>&2
    exit 1
fi

# Download OVS
wget http://openvswitch.org/releases/openvswitch-2.3.1.tar.gz
tar -xzvf openvswitch-2.3.1.tar.gz
mv openvswitch-2.3.1 openvswitch
cd openvswitch

# Install OVS
./boot.sh
./configure --with-linux=/lib/modules/`uname -r`/build
make && make install

# Load OVS module into kernel
cd datapath/linux
modprobe openvswitch
lsmod | grep openvswitch

# Create needed files and directories
touch /usr/local/etc/ovs-vswitchd.conf
mkdir -p /usr/local/etc/openvswitch

# Create conf.db in OVS directory
cd ../..
ovsdb-tool create /usr/local/etc/openvswitch/conf.db  vswitchd/vswitch.ovsschema

# Start OVS
ovsdb-server /usr/local/etc/openvswitch/conf.db \
--remote=punix:/usr/local/var/run/openvswitch/db.sock \
--remote=db:Open_vSwitch,Open_vSwitch,manager_options \
--private-key=db:Open_vSwitch,SSL,private_key \
--certificate=db:Open_vSwitch,SSL,certificate \
--bootstrap-ca-cert=db:Open_vSwitch,SSL,ca_cert --pidfile --detach --log-file

ovs-vsctl --no-wait init
ovs-vswitchd --pidfile --detach
ovs-vsctl show
ovs-vsctl --version


cd

git clone git://github.com/mininet/mininet
cd mininet
git checkout 2.2.2
~/mininet/util/install.sh -a
