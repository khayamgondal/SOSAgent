    #!/usr/bin/env bash
#This script will start controller and mininet topology.
#author Khayam Anjam kanjam@g.clemson.edu

if [ "$(id -u)" != "0" ]; then
    echo "This script must be run as root" 1>&2
    exit 1
fi

tmux new-session -d -s sos -n floodlight 'sudo service openvswitch-switch start && sudo python 2hop-mininet-topo.py'
tmux split-window -h -p 70 -d -t sos:floodlight 'cd ../floodlight/  && java -jar target/floodlight.jar'
tmux split-window -p 30 -d -t sos:floodlight  'cd ../floodlight/  && bash'

tmux attach -t sos

