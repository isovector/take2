#!/usr/bin/env bash

cd back
rm RUNNING_PID
/opt/play-2.2.1/play "-Dconfig.file=conf/$1.conf" "start -Dhttp.port=$2"
