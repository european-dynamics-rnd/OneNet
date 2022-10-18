#!/bin/bash

/sbin/httpd -D BACKGROUND
java -jar /opt/feg-local/backend/onenet-0.0.1-SNAPSHOT.jar --sofia.uri=${SOFIA_URI_ENV}
