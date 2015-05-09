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

# include the DEFAULT_SCANNERS
. bin/hosts.conf

# The scanner ssh hosts to sync to, override with SCANNERS env variable
SCANNERS=${SCANNERS:-${DEFAULT_SCANNERS}}
echo "SCANNERS=${SCANNERS}"

function copy() {
    for host in $SCANNERS;
    do
        rsync -rz -e ssh $1 root@${host}:$2
    done
}
function copy_systemd() {
    for host in $SCANNERS;
    do
        rsync -rz -e ssh $1 root@${host}:$2
        if [ $? -eq 0 ]; then
            ssh root@${host} systemctl daemon-reload
        fi
    done
}

# First setup the authorized_keys on hosts if -K given
if [ -n "${SSH_KEY}" ]; then
    for host in $SCANNERS;
    do
        rsync -rz -e ssh -p --chmod=u=r,og-rw ~/.ssh/${SSH_KEY} root@${host}:/root/.ssh/authorized_keys
    done
fi

# This needs a shared ssh private key in order to avoid having to enter password for each host
copy_systemd systemd/ /usr/lib/systemd/system
copy boot/ /boot

