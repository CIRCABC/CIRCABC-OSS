#!/bin/sh
#
# $Header: dbaas/docker/build/setup/startupDB.sh /main/1 2016/07/27 09:51:49 xihzhang Exp $
#
# startupDB.sh
#
# Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      startupDB.sh - startup database
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      run as oracle or root
#
#    MODIFIED   (MM/DD/YY)
#    xihzhang    10/25/16 - Remove EE bundles
#    xihzhang    05/23/16 - Creation
#

# check user
USER=`whoami`
if [ "$USER" == "root" ]
then
    su - oracle <<EOF
    /bin/bash /home/oracle/setup/setupScriptsDB.sh
EOF
exit 0
fi

if [ "$USER" != "oracle" ]
then
    echo "setupScriptsDB.sh needs to be executed by user : oracle"
    echo "Please swift user and try again"
    exit 1
fi

# logfile
SETUPSCRIPT_LOG=/home/oracle/setup/log/setupScriptsDB.log
echo `date`
echo `date` >> $SETUPSCRIPT_LOG
# launch setup scripts
echo "launching setup scripts"
echo "launch setup scripts" >> $SETUPSCRIPT_LOG
echo exit | sqlplus SYS/password@PDB1 as sysdba 2>&1 >> $SETUPSCRIPT_LOG  @/home/oracle/setup/setup.sql

echo "The database is initialized with setup scripts ."
echo "" >> $SETUPSCRIPT_LOG

# end
