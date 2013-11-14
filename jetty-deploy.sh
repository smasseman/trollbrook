#!/bin/bash

##
## This script can be used to deploy the web app on your local dev machine.
##

APP_DIR=`pwd`/webapp
JETTY_HOME=/usr/local/jetty

cat > "$JETTY_HOME/contexts/bryggmester.xml" << EOF
<?xml version="1.0"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure.dtd">
<Configure class="org.eclipse.jetty.webapp.WebAppContext">
  <Set name="contextPath">/bryggmester</Set>
  <Set name="war">$APP_DIR</Set>
  <Set name="defaultsDescriptor"><SystemProperty name="jetty.home" default="."/>/etc/webdefault.xml</Set>
</Configure> 
EOF

