**Setting up SOS** 

**1:** Fork _khayamgondal/SOSAgent & khayamgondal/floodlight_ to your GitHub account

**2:** Setup ssh keys and clone these repos to your home folder

**3:** Install maven, vagrant and virtual box

**4:** Building Agents & Floodlight: _cd SOSAgent/ and mvn package_, similarly _cd floodlight/ git checkout shellaN & mvn package -Dmaven.test.skip=true_. These commands will package .jar files in target/ directory

**5:** cd _SOSAgent_/ and _vagrant up_ . This will bring up the vagrant VM. It might take some time. Once done do _vagrant ssh_

**6:** _cd sos-agent_ and cd topos and run sudo bash startupVM.sh. it will  bring up the mininet topology.

**7:** Once its done, You will see multiple xterm sessions on screen (If you are on Mac and see error "Can't open Display", You will need to install XQuartz). Go to client1 and try to ping server1 (10.0.0.211). It would be unsuccessful as floodlight is yet not running

**8:** Open another session by _vagrant ssh_ , _cd sos-for-floodlight/_ and run _java -jar target/floodlight.jar_ It will start the controller. Go back to client1 and you will see ping going through. 

**9:** Starting Agents: (On agent1 & agent 2) cd sos-agent & run java -jar target/sosagent.jar. It 
will start the agents.

**10:** Whitelisting agents: cd sos-for-floodlight/ and run con_whitelist.sh. 
This file contains the white list entries for controller.


