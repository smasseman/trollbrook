#!/bin/bash

rm -rf deploy
cp -r webapp deploy
find deploy/WEB-INF/classes/ -name "*Test.class" -exec rm {} \;

jar cf bryggmester.war -C deploy .
scp bryggmester.war pi@${1}:/home/pi/jetty-distribution-9.0.4.v20130625/webapps/root.war
