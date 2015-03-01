#!/usr/bin/env bash

echo -n "checking for take2rc... "
if [ ! -e ./.take2rc ]; then
    echo "missing"
    echo -n "enter the unique project number we gave you: "
    read PORT
    echo "http://ec2-52-10-51-63.us-west-2.compute.amazonaws.com:$PORT/" > ./.take2rc
else
    echo "found"
fi

echo -n "checking for accioignore... "
if [ ! -e ./.accioignore ]; then
    echo "missing"
    echo  > ./.accioignore
else
    echo "found"
fi

echo
echo "project init succeeded!"
