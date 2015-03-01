#!/usr/bin/env bash

cd back
/opt/play-2.2.1/play "-Dconfig.file=conf/$1.conf" "run $2 -mem 256"
