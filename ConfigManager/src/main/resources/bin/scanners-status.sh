#!/usr/bin/env bash

. hosts.conf

# The scanner ssh hosts to sync to, override with SCANNERS env variable
SCANNERS=${SCANNERS:-${DEFAULT_SCANNERS}}
echo "SCANNERS=${SCANNERS}"

for host in $SCANNERS;
do
    echo "+++ Checking host: $host"
    ssh root@${host} systemctl status test-network.timer scannerd.service
    echo "+++ End $host"
    echo
    echo
done
