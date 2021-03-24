#!/bin/bash
#
# This work is protected under copyrights held by the members of the
# TOOP Project Consortium as indicated at
# http://wiki.ds.unipi.gr/display/TOOP/Contributors
# (c) 2018-2021. All rights reserved.
#
# This work is licensed under the EUPL 1.2.
#
#  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
#
# Licensed under the EUPL, Version 1.2 or – as soon they will be approved
# by the European Commission - subsequent versions of the EUPL
# (the "Licence");
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
#
#         https://joinup.ec.europa.eu/software/page/eupl
#



version=`mvn -o org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
JAR="target/toop-simulator-ng-${version}-bundle.jar"

if [[ ! -r $JAR ]]
then
  mvn verify
else
  echo "$JAR exists"
fi

export SIM_MODE=DP
#DC_PORT=8080
#DP_PORT=8082
#DC_URL="http://localhost:8080/to-dc"
#DP_URL="http://localhost:8082/to-dp"
#CONNECTOR_PORT=8081

java -jar $JAR
