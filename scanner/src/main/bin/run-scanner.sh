#!/usr/bin/env bash

# A script to run the java beacon scanner

# Absolute path to this script
SCRIPT=$(readlink -f $0)
BIN=`dirname $SCRIPT`
ROOT=`dirname $BIN`

# Source the scanner configuration
# Typically this should have:
# - scannerID ; the name by which the scanner is defined in the beacon event messages
# - heartbeatUUID ; the uuid of the beacon used as the heartbeat for the scanner
# - brokerURL ; the url of the activemq broker
# - username ; the username to authenticate against the broker
# - password ; the password to authenticate against the broker
# - beaconMapping ; the beaconMapping argument to pass to the scanner
. ~/scanner.conf

# Aliases for backward compatibility to previous variables used from scanner.conf
scannerID=${scannerID:=$SCANNER_ID}
heartbeatUUID=${HEARTBEAT_UUID:=$heartbeatUUID }
brokerURL=${BROKER_URL:=$brokerURL}

# If scanner.conf defined a systemType, export it as SYSTEM_TYPE
if [ -n "$systemType" ]; then
        echo "Exporting SYSTEM_TYPE=$systemType"
        export SYSTEM_TYPE=$systemType
fi

#JAVA_HOME="/opt/java/jdk"
if [ -n "$JAVA_HOME" ]; then
# default to location of oracle jvm on our raspbian image
   JAVA_HOME="/usr/lib/jvm/jdk-8-oracle-arm-vfp-hflt"
fi

# The scannerLib native library is expected to be in the /usr/local/lib directory
JAVA_OPTS="-Djava.library.path=/usr/local/lib"

CMD="${JAVA_HOME}/bin/java"
ARGS="--scannerID "${scannerID:-`hostname`}" --brokerURL "${brokerURL:-192.168.1.107:5672}" --heartbeatUUID "${heartbeatUUID}""
if [ -n "$username" ]; then
        ARGS="${ARGS} --username ${username}"
fi
if [ -n "$password" ]; then
        ARGS="${ARGS} --password ${password}"
fi
if [ -n "$beaconMapping" ]; then
        ARGS="${ARGS} --beaconMapping ${beaconMapping}"
fi

BACKGROUND=""
# Check for a background argument
if [ "$1" == "-background" ]; then
        BACKGROUND=" >& /var/log/beacon-scanner.log &"
        shift
fi
ARGS="${ARGS} $*"
CMD="${CMD} ${ARGS} ${BACKGROUND}"

# Start the scanner
echo "Running: $CMD"
eval $CMD
