#!/bin/bash

exec java \
    -showversion -server -Dfile.encoding=UTF-8 \
    -Dspring.profiles.active=docker \
    -jar /opt/libooker/lib/app.jar