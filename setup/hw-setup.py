#author Khayam Gondal kanjam@g.clemson.edu
#This script can install packages on multiple nodes at the same time to save time
from fabric import Connection
from fabric.api import *

@parallel
def install_java_8(node):
    return node.run('sudo apt install -y openjdk-8-jdk')



nodes = ['khayam@apt162.apt.emulab.net', 'khayam@apt146.apt.emulab.net', 'khayam@apt138.apt.emulab.net', 'khayam@apt150.apt.emulab.net']

for node in nodes:
    print install_java_8(Connection(node))