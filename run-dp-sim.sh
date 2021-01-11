#!/bin/bash
#
# Copyright (C) 2018-2021 toop.eu. All rights reserved.
#
# This project is dual licensed under Apache License, Version 2.0
# and the EUPL 1.2.
#
#  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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


# Since V0.10.7
# should we simulate the gateway connection or not?
# if true then the gateways are skipped and this simulator instance works as
# a compound connector of the two member states, otherwise (false) the MEM message
# is passed to the gateway configured with the key toop.mem.as4.endpoint and all
# the toop.mem.as4.* configurations (see toop-connector.properties) become significant
#MOCK_GATEWAY=true/false


java -jar $JAR
