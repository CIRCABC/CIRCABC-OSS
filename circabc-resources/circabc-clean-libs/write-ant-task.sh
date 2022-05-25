#! /bin/sh
while read line; do if [[ ${line:0:1} == "-" ]] ; then echo '<include name="'${line:1}'"/>' ; fi; done < patch.txt
