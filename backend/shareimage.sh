#!/bin/bash

# Copyright 2013 University of Michigan
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# shellcheck disable=SC2046
SCRIPT_DIR=$(cd $(dirname $0) || exit; pwd)
# shellcheck disable=SC1090
. $SCRIPT_DIR/capsules.cfg

usage () {

  echo "Usage: $0 --wdir <Directory for VM>"
  echo ""
  echo "Determines the operational status of the VM in the given directory."
  echo ""
  echo "(--wdir)  Directory: The directory where this VM's data is held."
  echo ""
  echo "(--sourceimage)  Source Image Name: The name of the image that is going to be shared."
  echo ""
  echo "(--newimage)  New Image Name: The name of the image that is going to be created from the source image."
  echo ""
  echo "-h|--help Show help."

}

# Initialize all the option variables.
# This ensures we are not contaminated by variables from the environment.
VM_DIR=
SOURCE_IMAGE=
NEW_IMAGE=

while :; do
    case $1 in
        -h|-\?|--help)
            usage    # Display a usage synopsis.
            exit
            ;;
        --wdir)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                VM_DIR=$2
                shift
            else
                die 'ERROR: "--wdir" requires a non-empty option argument.'
            fi
            ;;
        --wdir=?*)
            VM_DIR=${1#*=} # Delete everything up to "=" and assign the remainder.
            ;;
        --wdir=)         # Handle the case of an empty --wdir=
            die 'ERROR: "--wdir" requires a non-empty option argument.'
            ;;
        --sourceimage)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                SOURCE_IMAGE=$2
                shift
            else
                die 'ERROR: "--sourceimage" requires a non-empty option argument.'
            fi
            ;;
        --sourceimage=?*)
            SOURCE_IMAGE=${1#*=} # Delete everything up to "=" and assign the remainder.
            ;;
        --sourceimage=)         # Handle the case of an empty --wdir=
            die 'ERROR: "--sourceimage" requires a non-empty option argument.'
            ;;
        --newimage)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                NEW_IMAGE=$2
                shift
            else
                die 'ERROR: "--newimage" requires a non-empty option argument.'
            fi
            ;;
        --newimage=?*)
            NEW_IMAGE=${1#*=} # Delete everything up to "=" and assign the remainder.
            ;;
        --newimage=)         # Handle the case of an empty --wdir=
            die 'ERROR: "--newimage" requires a non-empty option argument.'
            ;;
        --)              # End of all options.
            shift
            break
            ;;
        -?*)
            printf 'WARN: Unknown option (ignored): %s\n' "$1" >&2
            usage
            exit 1
            ;;
        *)               # Default case: No more options, so break out of the loop.
            break
    esac

    shift
done

if [[ -z "$VM_DIR" || -z "$SOURCE_IMAGE" || -z "$NEW_IMAGE" ]]; then
  printf 'WARN: Missing required argument '  >&2
  usage
  exit 1
fi

if [ ! -d $VM_DIR ] ; then
  echo "Error: Invalid VM directory specified!"
  exit 2
fi

# Check if VM is in SHUTDOWN state
if [[ `$SCRIPT_DIR/vmstatus.sh --wdir $VM_DIR` =~ "Status:  Running" ]]; then
  echo "Error: VM is running!"
  exit 3
fi

# Load config file
# shellcheck disable=SC1090
. "$VM_DIR"/config

# Check whether VM has the correct and full disk image
if [[ "$IMAGE" = *.diff  && -e $VM_DIR/${IMAGE%.diff} && -s $VM_DIR/${IMAGE%.diff}.newsum ]]; then
  echo "Error: Full disk image is not copied to the VM!"
  exit 4
fi

SOURCE_IMAGE=$(basename "$SOURCE_IMAGE")

if [ "$SOURCE_IMAGE" != "$IMAGE" ]; then
    echo "Error: VM has different disk image!"
    exit 5
fi

if [ ! -e "$VM_DIR"/"$SOURCE_IMAGE" ]; then
    echo "Error: Image is not available in the capsule directory!"
    exit 6
fi


#Copy image to host's image store
# shellcheck disable=SC1072
cp "$VM_DIR"/"$SOURCE_IMAGE" "$NEW_IMAGE"  2>>"$VM_DIR"/image_share_log

logger "$SOURCE_IMAGE is successfully copied to $NEW_IMAGE in the host."


nohup "$SCRIPT_DIR"/copyimage.sh --sourceimage "$NEW_IMAGE" 2>>"$VM_DIR"/image_share_log >>$VM_DIR/image_share_log &
echo "$!" > $VM_DIR/image_share_pid


exit 0
