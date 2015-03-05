#!/usr/bin/env bash

if [ "$3" = "prod" ]; then
    RUN_METHOD="start"  #PROD
else
    RUN_METHOD="run"    #DEV
fi

cd back
rm RUNNING_PID
/opt/play-2.2.1/play "-Dconfig.file=conf/$1.conf" "$RUN_METHOD -Dhttp.port=$2"

