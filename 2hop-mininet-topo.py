#!/usr/bin/python

"""
Simple test topology for SoS. One agent on each side along with one client and server
@author Khayam Anjam
@email kanjam@g.clemson.edu
"""

from mininet.net import Mininet
from mininet.node import Controller, OVSSwitch, RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from os import environ
from mininet.link import Link, TCLink

from mininet.log import error
from mininet.util import quietRun, errRun

import subprocess

def tunnelX11( node, display=None):
    """Create an X11 tunnel from node:6000 to the root host
       display: display on root host (optional)
       returns: node $DISPLAY, Popen object for tunnel"""
    if display is None and 'DISPLAY' in environ:
        display = environ[ 'DISPLAY' ]
    if display is None:
        error( "Error: Cannot connect to display\n" )
        return None, None
    host, screen = display.split( ':' )
    # Unix sockets should work
    if not host or host == 'unix':
        # GDM3 doesn't put credentials in .Xauthority,
        # so allow root to just connect
        quietRun( 'xhost +si:localuser:root' )
        return display, None
    else:
        # Create a tunnel for the TCP connection
        port = 6000 + int( float( screen ) )
        connection = r'TCP\:%s\:%s' % ( host, port )
        cmd = [ "socat", "TCP-LISTEN:%d,fork,reuseaddr" % port,
                "EXEC:'mnexec -a 1 socat STDIO %s'" % connection ]
    return 'localhost:' + screen, node.popen( cmd )

def makeTerm( node, title='Node', term='xterm', display=None, cmd='bash'):
    """Create an X11 tunnel to the node and start up a terminal.
       node: Node object
       title: base title
       term: 'xterm' or 'gterm'
       returns: two Popen objects, tunnel and terminal"""
    title = '"%s: %s"' % ( title, node.name )
    if not node.inNamespace:
        title += ' (root)'
    cmds = {
        'xterm': [ 'xterm', '-title', title, '-display' ],
        'gterm': [ 'gnome-terminal', '--title', title, '--display' ]
    }
    if term not in cmds:
        error( 'invalid terminal type: %s' % term )
        return
    display, tunnel = tunnelX11( node, display )
    if display is None:
        return []
    term = node.popen( cmds[ term ] +
                       [ display, '-e', 'env TERM=ansi %s' % cmd ] )
    return [ tunnel, term ] if tunnel else [ term ]

def runX11( node, cmd ):
    "Run an X11 client on a node"
    _display, tunnel = tunnelX11( node )
    if _display is None:
        return []
    popen = node.popen( cmd )
    return [ tunnel, popen ]

def cleanUpScreens():
    "Remove moldy socat X11 tunnels."
    errRun( "pkill -9 -f mnexec.*socat" )

def makeTerms( nodes, title='Node', term='xterm' ):
    """Create terminals.
       nodes: list of Node objects
       title: base title for each
       returns: list of created tunnel/terminal processes"""
    terms = []
    for node in nodes:
        terms += makeTerm( node, title, term )
    return terms

def multiControllerNet():

    net = Mininet( controller=Controller, switch=OVSSwitch, link=TCLink )

    info( "*** Creating switches\n" )
    s1 = net.addSwitch( 's1' ) #switches for the data network
    s2 = net.addSwitch( 's2' )

    s5 = net.addSwitch( 's5')
    s4 = net.addSwitch( 's4')

    s3 = net.addSwitch( 's3' ) #switch for the control network

    info( "*** Creating hosts\n" )
    agent1 = net.addHost('agent1', ip='10.0.0.11')
    agent2 = net.addHost('agent2', ip='10.0.0.21')

    client1 = net.addHost('client1', ip='10.0.0.111')
    server1 = net.addHost('server1', ip='10.0.0.211')

    #controller = net.addHost('controller', ip='10.0.2.150')


    #controller.cmd("cd  /home/vagrant/sos-for-floodlight && java -jar target/floodlight.jar")

    info( "*** Creating data links\n" )

    net.addLink( s1, client1)
    net.addLink(s1, s4)
    net.addLink(s4, agent1)

    net.addLink( s1, s2 , delay='50ms')
    net.addLink(s2,server1)
    net.addLink(s2, s5)
    net.addLink(s5,agent2)

    info( "*** Creating control links\n" )
    Link(agent1, s3, intfName1='agent1-eth1')
    agent1.cmd('ifconfig agent1-eth1 192.168.100.1 netmask 255.255.255.0')
    Link(agent2, s3, intfName1='agent2-eth1')
    agent2.cmd('ifconfig agent2-eth1 192.168.100.2 netmask 255.255.255.0')


    info( "*** Starting network\n" )
    net.build()

    makeTerms([agent1, agent2, server1, client1])

    info( "*** Creating controllers\n" )
    local = net.addController( 'localctl', port=6633 ) #controller for control network
    local.start()
    s3.start([local])

    info("*** Assigning IP to host interface\n")
    #call ('sudo', '/sbin/ifconfig', 's3', '192.168.100.10/24', 'up', shell=True)
    subprocess.Popen('sudo ifconfig s3 192.168.100.10/24 up', shell=True)

    remote = RemoteController( 'remotectl', ip='192.168.100.10',
                               port=6653 ) #controller for data network (floodlight IP)
    remote.start()
    s1.start( [ remote ] )
    s2.start( [ remote] )
    s4.start( [ remote] )
    s5.start( [ remote] )


    #info( "*** Testing network\n" )
    #net.pingAll()

    #info( "*** Running CLI\n" )
    CLI( net )

    #info( "*** Stopping network\n" )
    cleanUpScreens()
    local.stop()
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )  # for CLI output
    multiControllerNet()