#!/bin/bash

old_cwd=`pwd`
cd `dirname $0`

case "$1" in
start|restart)

  if [ ! -d ./sandbox ]; then
    mkdir ./sandbox
  else
    rm -r ./sandbox/conf
    rm -r ./sandbox/felix
    rm -r ./sandbox/bundles
  fi

  cp -r ./conf ./sandbox

esac

SERVICE_NAME=CRCE
PATH_TO_JAR="pax-runner.jar --ups --workingDirectory=./sandbox scan-dir:required-bundles scan-dir:crce-bundles war:file:crce-wars/crce-webui.war scan-bundle:file:crce-wars/crce-rest-v2.war scan-file:file:platform.properties"
PID_PATH_NAME=/tmp/crce-pid

case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java -jar $PATH_TO_JAR 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup java -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac

cd $old_cwd