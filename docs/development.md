`Setting up development environment`

Fork _khayamgondal/SOSAgent & khayamgondal/floodlight_ to your GitHub account

Setup ssh keys and clone these repos to your home folder

Install maven, vagrant and virtual box

Building Agents & Floodlight: _cd SOSAgent/ and mvn package_, similarly _cd floodlight/ git checkout shella1.0 & mvn package -Dmaven.test.skip=true_. These commands will package .jar files in target/ directory

cd _SOSAgent_/ and _vagrant up_ . This will bring up the vagrant VM. It might take some time. Once done do _vagrant ssh_

You can either use vagrant for testing or you can use your local linux based system for testing

For vagrant: 
 _cd sos-agent_ and cd topos and run sudo bash setup/envsetup-vm.sh it will install the
 required packages Once finished run sudo bash mininet/startup-vm.sh 
 it will following bring up the mininet topology.
[ADD fig here]
Once its done, You will see multiple xterm sessions on screen 
(If you are on Mac and see error "Can't open Display",
 You will need to install XQuartz). 
 
Open another session by _vagrant ssh_ , _cd sos-for-floodlight/_ and run _java -jar target/floodlight.jar_ It will start the controller. Go back to client1 and you will see ping going through. 

Starting Agents: (On agent1 & agent 2) cd sos-agent & run java -jar target/sosagent.jar. It 
will start the agents.

Whitelisting agents: cd sos-for-floodlight/ and run wlist/mininet-whitelist.sh. 
This file contains the white list entries for controller.


