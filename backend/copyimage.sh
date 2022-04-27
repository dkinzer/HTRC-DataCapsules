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
  echo "(--sourceimage)  Source Image Path: The path of the image that is going to be copied."
  echo ""
  echo "-h|--help Show help."

}

# Initialize all the option variables.
# This ensures we are not contaminated by variables from the environment.
SOURCE_IMAGE=


while :; do
    case $1 in
        -h|-\?|--help)
            usage    # Display a usage synopsis.
            exit
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

if [ -z "$SOURCE_IMAGE" ]; then
  printf 'WARN: Missing required argument '  >&2
  usage
  exit 1
fi

if [ ! -e "$SOURCE_IMAGE" ] ; then
  echo "Error: Source image does not exist!"
  exit 2
fi

# Load config file
# shellcheck disable=SC1090
. "$VM_DIR"/config

# Use comma as separator and apply as pattern
if [ -z "$OTHER_DC_HOSTS" ]; then
 echo "Other DC Hosts list is empty!"
 exit 3
fi

for dchost in ${OTHER_DC_HOSTS//,/ }
do
   scp "$SOURCE_IMAGE" "$dchost":"$SOURCE_IMAGE"
   logger "$SOURCE_IMAGE is successfully copied to the $dchost host."
done

#Add DC_API call to activate the image



exit 0
