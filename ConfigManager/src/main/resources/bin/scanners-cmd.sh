#!/usr/bin/env bash
. hosts.conf

CMD="$*"
# The scanner ssh hosts to sync to, override with SCANNERS env variable
SCANNERS=${SCANNERS:-${DEFAULT_SCANNERS}}
echo "SCANNERS=${SCANNERS}"

echo "CMD=$CMD"
for host in $SCANNERS;
do
    echo "+++ Running on host: $host"
    ssh root@${host} "$CMD"
    echo "+++ End $host"
    echo
    echo
done
