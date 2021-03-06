#!/usr/bin/env bash
# sync scanners NativeRaspberryPiBeaconParser git repo and optionally build it
# To just sync git repos:
#  ./scanners-git.sh
# To sync and build the native scanner
#  ./scanners-git.sh -D
# To sync and build some-target
#  ./scanners-git.sh -B some-target

# Check for the build(-B) or default target(D) option
BUILD_TARGET=""
while getopts "B:D" opt; do
  case $opt in
    B)
      BUILD_TARGET=$OPTARG
      ;;
    D)
      BUILD_TARGET="NativeScannerBlueZ"
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac
done

# include the DEFAULT_SCANNERS
. hosts.conf

# The scanner ssh hosts to sync, override with SCANNERS env variable
SCANNERS=${SCANNERS:-${DEFAULT_SCANNERS}}

#
CMD="cd ~/NativeRaspberryPiBeaconParser; git pull"
if [ -n "${BUILD_TARGET}" ]; then
    CMD="${CMD}; cmake --build Debug --target ${BUILD_TARGET}"
fi

for host in $SCANNERS;
do
    echo "+++ Syncing host: $host"
    ssh root@${host} "${CMD}"
    echo "+++ End $host"
    echo
    echo
done
