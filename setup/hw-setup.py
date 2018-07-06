#author Khayam Gondal kanjam@g.clemson.edu
#This script can install packages on multiple nodes at the same time to save time

import sys, os, string, threading
import paramiko


def host_conn(host):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(host, username=user, key_filename=key)
    update_nodes(host, ssh)
    install_ovs(host, ssh)

def agent_conn(host):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(host, username=user, key_filename=key)
    update_nodes(host, ssh)
    install_java_8(host, ssh)
    install_ovs(host, ssh)
    #install_infini_drivers(host, ssh)

def wan_conn(host):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(host, username=user, key_filename=key)
    update_nodes(host, ssh)
    install_java_8(host, ssh)
    setup_controller(host, ssh)

def update_nodes(host, ssh):
    stdin, stdout, stderr = ssh.exec_command('sudo apt update')
    exit_status = stdout.channel.recv_exit_status()
    if exit_status == 0:
        pass
    else:
        print(host + " Failed to APT update")

def install_infini_drivers(host, ssh):
    stdin, stdout, stderr = ssh.exec_command('wget http://content.mellanox.com/ofed/MLNX_OFED-4.3-1.0.1.0/MLNX_OFED_LINUX-4.3-1.0.1.0-ubuntu16.04-x86_64.iso')
    stdout.channel.recv_exit_status()

    stdin, stdout, stderr = ssh.exec_command('sudo mkdir /mnt/mx && sudo mount -o loop MLNX_OFED_LINUX-4.3-1.0.1.0-ubuntu16.04-x86_64.iso /mnt/mx && sudo /mnt/mx/mlnxofedinstall')
    stdin.write('y')
    exit_status = stdout.channel.recv_exit_status()

    stdin, stdout, stderr = ssh.exec_command('sudo /etc/init.d/openibd restart')
    stdout.channel.recv_exit_status()

    if exit_status == 0:
        pass
    else:
        print(host + " Failed to install drivers")


def install_java_8(host, ssh):
    stdin, stdout, stderr = ssh.exec_command('sudo apt install -y openjdk-8-jdk')
    exit_status = stdout.channel.recv_exit_status()
    if exit_status == 0:
        #print(host + " Java installed")
        pass
    else:
        print(host + " Failed to install Java")

def install_ovs(host, ssh):
    stdin, stdout, stderr = ssh.exec_command('sudo apt install -y openvswitch-switch')
    exit_status = stdout.channel.recv_exit_status()
    if exit_status == 0:
        #print(host + " OVS installed")
        setup_ovs(host, ssh)
    else:
        print(host + " Failed to install OVS")

def setup_ovs(host, ssh):
    stdin, stdout, stderr = ssh.exec_command('sudo ovs-vsctl add-br br0 && sudo ovs-vsctl add-port br0 '+ interface_name +' && sudo ifconfig '+ interface_name + ' 0 up && sudo ovs-vsctl add-port br0 '+ interface_name2 +' && sudo ifconfig '+ interface_name2 + ' 0 up')
    exit_status = stdout.channel.recv_exit_status()
    stdin, stdout, stderr = ssh.exec_command('sudo ovs-vsctl set-controller br0 tcp:'+ ctl_ip + ':'+ctl_port )
    exit_status = stdout.channel.recv_exit_status()
    if exit_status == 0:
        #print(host + " OVS setup!")
        pass
    else:
        print(host + " Failed to setup OVS")




def setup_controller(host, ssh):
    install_maven(host, ssh)
    stdin, stdout, stderr = ssh.exec_command('git clone http://github.com/khayamgondal/floodlight && cd floodlight/ && git checkout shellaN && mvn package -Dmaven.test.skip=true')
    exit_status = stdout.channel.recv_exit_status()
    if exit_status == 0:
        pass
    else:
        print(host + " Failed to setup floodlight controller")

def install_maven(host, ssh):
    stdin, stdout, stderr = ssh.exec_command('sudo apt install -y maven')
    exit_status = stdout.channel.recv_exit_status()
    if exit_status == 0:
        setup_ovs(host, ssh)
    else:
        print(host + " Failed to install maven")



clients = []
agents = ['apt177.apt.emulab.net', 'apt057.apt.emulab.net', 'apt007.apt.emulab.net', 'apt060.apt.emulab.net', 'apt053.apt.emulab.net',
          'apt184.apt.emulab.net']
servers = []

wan = 'apt184.apt.emulab.net'

user = 'khayam'
key = '/home/khayam/.ssh/id_rsa'

interface_name = 'enp8s0d1'
interface_name2 = 'enp3s0f0'
ctl_ip = "128.110.96.155"
ctl_port="6693"

threads = []

for node in clients + servers:
    t = threading.Thread(target=host_conn(node), args=(node))
    t.start()
    threads.append(t)
for t in threads:
    t.join()

for node in agents:
    t = threading.Thread(target=agent_conn(node), args=(node))
    t.start()
    threads.append(t)
for t in threads:
    t.join()

#wan_conn(wan)