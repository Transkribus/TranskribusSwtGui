#!/bin/bash

trap "EXIT=1" SIGINT SIGTERM

echoerr() { echo "$@" 1>&2; }
print_usage() { echo "USAGE: create_thumbs.sh input_dir [overwrite]"; }

EXIT=0
echo Creating thumbs in dir: $1

if [ -z "$1" ]
then
print_usage;
exit 1
fi

cd $1
mkdir -p thumbs
files=`ls | find -type f -maxdepth 1 | egrep -i '\.(tif|jpeg|jpg|jpe|png)$'`

IFS=$(echo -en "\n\b") # don't split on whitespaces!

for filename in $files; do
    fbname=$(basename "$filename" | cut -d. -f1)
    #echo basename = $fbname
    thumbfn="thumbs/$fbname.jpg"
    if ! [ -e "$thumbfn" ] || [ -n "$2" ]; then
    	echo creating thumb $thumbfn
    	convert "$filename" -thumbnail x120 "$thumbfn"
    fi
    
    if [ $EXIT -eq 1 ]; then
        echo "Stopping...";
        break;
    fi
done