#!/bin/bash

interface=br0
ip=10.0.0.xx
delay=25ms

tc qdisc del dev br0 root
tc qdisc add dev $interface root handle 1: prio
tc filter add dev $interface parent 1:0 protocol ip prio 1 u32 match ip dst $ip flowid 2:1
tc qdisc add dev $interface parent 1:1 handle 2: netem delay $delay

