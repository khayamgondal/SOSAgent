`Deploy agents in commercial environment.`

Make sure you have setup required topology to run SOS. 
On the nodes you want to use as **SOS Agent** download the latest release from github.
Once downloaded, just start the binary as a Java app _java -jar sosagent.jar_.
Thats it now just add agent nodes IP address to controller's whitelist.

`Rest endpoints:`

`http://127.0.0.1:8002/sos/v1.0/health
http://127.0.0.1:8002/sos/v1.0/request`