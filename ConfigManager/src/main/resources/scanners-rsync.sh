#!/usr/bin/env bash

# The name of the ~/.ssh/public_key to transfer to hosts to enable password free ssh
SSH_KEY=""
while getopts "K:" opt; do
  case $opt in
    K)
      SSH_KEY=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac
done

function copy() {
    for host in $SCANNERS;
    do
        rsync -rz -e ssh $1 root@${host}:$2
    done
}

# The scanner ssh hosts to sync to, override with SCANNERS env variable
DEFAULT_SCANNERS="room201 room202 room202x room203 room204 general generalx"
SCANNERS=${SCANNERS:-${DEFAULT_SCANNERS}}
echo "SCANNERS=${SCANNERS}"

# First setup the authorized_keys on hosts if -K given
if [ -n "${SSH_KEY}" ]; then
    for host in $SCANNERS;
    do
        rsync -rz -e ssh -p --chmod=u=r,og-rw ~/.ssh/${SSH_KEY} root@${host}:/root/.ssh/authorized_keys
    done
fi

# This needs a shared ssh private key in order to avoid having to enter password for each host
copy systemd/ /usr/lib/systemd/system
copy boot/ /boot
