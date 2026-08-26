#!/bin/sh
#
# $Header: dbaas/docker/build/setup/dockerInit.sh /main/4 2016/10/07 14:33:02 xihzhang Exp $
#
# dockerInit.sh
#
# Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      dockerInit.sh - docker container initiation file
#
#    DESCRIPTION
#      init script to be run everytime docker container is started
#
#    NOTES
#      run as root
#
#    MODIFIED   (MM/DD/YY)
#    xihzhang    10/25/16 - Remove EE bundles
#    xihzhang    09/14/16 - Use ENTRYPOINT
#    xihzhang    09/06/16 - Optimize build
#    xihzhang    08/08/16 - Remove privilege mode
#    xihzhang    05/23/16 - Creation
#

# basic parameters
LOG_DIR=/home/oracle/setup/log
SETUP_DIR=/home/oracle/setup

if [ ! -d $LOG_DIR ]
then
    mkdir $LOG_DIR
    chmod 775 $LOG_DIR
    chown oracle:oinstall $LOG_DIR
fi

# logfile
INIT_LOG=$LOG_DIR/dockerInit.log
echo `date` >> $INIT_LOG

# root user check
# check user
USER=`whoami`
if [ "$USER" == "root" ]
then
    echo "User check : root."
    echo "User check : root." >> $INIT_LOG
else
    echo "ERROR : wrong user !"
    echo "DB is not setup or started ..."
    echo "please run dockerInit.sh as root user."
    echo "ERROR : wrong user !" >> $INIT_LOG
    echo "DB is not setup or started ..." >> $INIT_LOG
    echo "please run dockerInit.sh as root user." >> $INIT_LOG
    echo "" >> $INIT_LOG
    exit 1
fi

# check setup path
if [ ! -d $SETUP_DIR ]
then
    echo "ERROR : setup files are not found"
    echo "ERROR : setup files are not found" >> $INIT_LOG
    echo "" >> $INIT_LOG
    exit 1
fi

# path of Oracle home
oh=/u01/app/oracle/product/12.1.0/dbhome_1/;

# check whether it is the first time this container is up
# if it is the first time, setup the DB
# if not, startup the existing db
if [ -d $oh ]
then
    echo "Start up Oracle Database"
    echo "Start up Oracle Database" >> $INIT_LOG
    /bin/bash $SETUP_DIR/startupDB.sh 2>&1
else
    echo "Setup Oracle Database"
    echo "Setup Oracle Database" >> $INIT_LOG
    /bin/bash $SETUP_DIR/setupDB.sh 2>&1
    echo "Launch Setup scripts"
    echo "Launch Setup scripts" >> $INIT_LOG
    /bin/bash $SETUP_DIR/setupScriptsDB.sh 2>&1
fi

echo "" >> $INIT_LOG

# remove passwd param
unset DB_PASSWD
# keep container runing
tail -f $INIT_LOG &
childPID=$!
wait $childPID

# end
