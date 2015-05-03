#!/usr/bin/env bash

# The scanner ssh hosts to sync to, override with SCANNERS env variable
DEFAULT_SCANNERS="room201 room202 room202x room203 room204 general generalx"
SCANNERS=${SCANNERS:-${DEFAULT_SCANNERS}}
echo "SCANNERS=${SCANNERS}"

# First setup the authorized_keys on hosts if -K given
for host in $SCANNERS;
do
    echo "+++ Checking host: $host"
    ssh root@${host} systemctl status test-network.timer scannerd.service
    echo "+++ End $host"
    echo
    echo
done
