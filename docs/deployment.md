**SOS Agent Deployment Guide**

Make sure you have the required topology to run SOS. 
On the nodes, you want to use as **SOS Agent** download the latest release from the GitHub release page.
Once downloaded, run the Java application
`java -jar sosagent.jar`

Download the controller https://github.com/khayamgondal/SOSForFloodlight (use `shella` branch) 

Follow the instructions below to set up the controller
https://clemsonopenflowgroup.atlassian.net/wiki/spaces/SOS/pages/4947970/How+to+Use+the+SOS+Controller


Agent rest points 
`Rest endpoints:`

`http://127.0.0.1:8002/sos/v1.0/health`
`http://127.0.0.1:8002/sos/v1.0/request`
