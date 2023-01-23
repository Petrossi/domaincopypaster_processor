#!/bin/bash

COMPILE_DIR=./
BASE_DIR=./target
START_COMMAND="/usr/bin/java -Xms8g -Xmx8g -Dspring.profiles.active=local -jar $BASE_DIR/domainsurvey.jar"
PID_FILE=$BASE_DIR/domainsurvey.pid
LOG_DIR=$BASE_DIR

start() {
    rm -rf BASE_DIR
    cd $COMPILE_DIR || exit
    mvn clean package -DskipTests=true
    mv $BASE_DIR/domainsurvey-0.1.jar $BASE_DIR/domainsurvey.jar
    PID=`$START_COMMAND > $LOG_DIR/init.log 2>$LOG_DIR/init.error.log & echo $!`
    echo "Already startet [$PID]"
}

case "$1" in
start)
    if [ -f $PID_FILE ]; then
        PID=`cat $PID_FILE`
        if [ -z "`ps axf | grep "${PID}" | grep -v grep`" ]; then
            start
        else
            echo "Already running [$PID]"
            exit 0
        fi
    else
        start
    fi

    if [ -z $PID ]; then
        echo "Failed starting"
        exit 1
    else
        echo $PID > $PID_FILE
        echo "Started [$PID]"
        exit 0
    fi
;;
status)
    if [ -f $PID_FILE ]; then
        PID=`cat $PID_FILE`
        echo grep -v grep
        exit 0
        if [ -z "`ps axf | grep "${PID}" | grep -v grep`" ]; then
            echo "Not running (process dead but PID file exists)"
            exit 1
        else
            echo "Running [$PID]"
            exit 0
        fi
    else
        echo "Not running"
        exit 0
    fi
;;
stop)
    kill -9 $(ps ax | grep 'domainsurvey' | fgrep -v grep | awk '{ print $1 }')
;;
log)
  tail -f -n 100 $LOG_DIR/init.log
;;
error)
  tail -f $LOG_DIR/error.log
;;

restart)
    $0 stop
    $0 start
;;
*)
    echo "Usage: $0 {status|start|stop|restart}"
    exit 0
esac