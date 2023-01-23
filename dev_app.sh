#!/bin/bash

JARDIR=/var/www/domainsuervey/crawler
JARNAME=domainsurvey
PID_FILE="$JARDIR/$JARNAME.pid"
PROFILE=tourzy

SOURCE_DIR=target
SSH_AUTH=root@157.230.91.80
START_COMMAND="java -Dspring.profiles.active=$PROFILE -jar $JARDIR/$JARNAME.jar > $JARDIR/init.log 2>$JARDIR/error.log & echo $!"

build(){
    ssh $SSH_AUTH "rm $JARDIR/$JARNAME.jar &"
    rm -rf $SOURCE_DIR
    mvn clean package -DskipTests=true
    scp $SOURCE_DIR/domainsurvey-0.1.jar $SSH_AUTH:$JARDIR/$JARNAME.jar
    rm -rf $SOURCE_DIR
}

start() {
  ssh $SSH_AUTH "java -Dspring.profiles.active=$PROFILE -jar $JARDIR/$JARNAME.jar >$JARDIR/init.log 2>$JARDIR/error.log & echo"
}

case "$1" in
start)
    start
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
log)
  ssh $SSH_AUTH "tail -f $JARDIR/init.log"
;;
error)
  ssh $SSH_AUTH "tail -f $JARDIR/error.log"
;;

build)
    build
;;
*)
    echo "Usage: $0 {status|start|stop|restart}"
    exit 0
esac