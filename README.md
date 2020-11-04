# TOOP Simulator Next Generation

[![Build Status](https://travis-ci.com/TOOP4EU/toop-simulator-ng.svg?branch=master)](https://travis-ci.com/TOOP4EU/toop-simulator-ng)

**Latest Release:** [2.0.0-rc3](https://repo1.maven.org/maven2/eu/toop/toop-simulator-ng/2.0.0-rc3/)

**Current Development Version:** 2.1.0-SNAPSHOT


## Introduction

The TOOP Simulator is a development tool that simulates the APIs provided by the TOOP Connector and the transactions between the development system and a mock DP or DC.


## TOOP Simulator Architecture

The simulator is built on the TOOP Connector API, providing implementations simulating the interactions between the infrastructure of TOOP (Data Services Directory, SMP) and the AS4 Gateways. It also integrates the implementation of Elonia and Freedonia, in order to provide full support of the transactions defined by TOOP.



## Provided APIs

The following TOOP Connector APIs are been implemented and provided by the Simulator

| Name  | RelativeURL | Sub Component | Description |
|-------|:------------|:--------------|:------------|
|Simple Validate, Lookup and Send	| /api/user/submit/request	| Helper | Validate, SMP Lookup and AS4 sending of an EDM Request in a single call |
|Simple Validate, Lookup and Send	|/api/user/submit/response	| Helper	| Validate, SMP Lookup and AS4 sending of an EDM Response in a single call| 
|Simple Validate, Lookup and Send	|/api/user/submit/error	| Helper	|  Validate, SMP Lookup and AS4 sending of an EDM Error Response in a single call| 
|Validate EDM Error Response	| /api/validate/error	| Validation	| Validate a TOOP EDM Error Response against the XSD and the Schematron| 
|Validate EDM Request	| /api/validate/request	| Validation	| Validate a TOOP EDM Request against the XSD and the Schematron| 
|Validate EDM Response	| /api/validate/response	| Validation	| Validate a TOOP EDM Response against the XSD and the Schematron| 


## Simulated Environment

The Simulator has the ability to only discover and submit messages to the Elonia and Freedonia System which it simulates. The following Tables provide an overview of the metadata that can be used for successful message transactions

| Message Type | Receiving System | Receiving Participant Identifier | Document Type Identifier | Process Identifier | Transmission Protocol |
|-------|:-----|:-------|:------|:------|:-------|
| EDM Concept Request	| Elonia DP	| iso6523-actorid-upis::9999:elonia	| toop-doctypeid-qns::RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.1 | toop-procid-agreement::urn:eu.toop.process.dataquery | bdxr-transport-ebms3-as4-v1p0 |
| EDM Document Request	| Elonia DP	| iso6523-actorid-upis::9999:elonia| 	toop-doctypeid-qns::FinancialRecord::FINANCIAL_RECORD_TYPE::UNSTRUCTURED::toop-edm:v2.1	| toop-procid-agreement::urn:eu.toop.process.documentquery |	bdxr-transport-ebms3-as4-v1p0| 
| EDM Response (Concept)	| Freedonia DC	| iso6523-actorid-upis::9999:freedonia | toop-doctypeid-qns::QueryResponse::toop-edm:v2.1	| toop-procid-agreement::urn:eu.toop.process.dataquery	| bdxr-transport-ebms3-as4-v1p0 | 
| EDM Response (Document)	|  Freedonia DC	| iso6523-actorid-upis::9999:freedonia | 	toop-doctypeid-qns::QueryResponse::toop-edm:v2.1 | toop-procid-agreement::urn:eu.toop.process.documentquery | bdxr-transport-ebms3-as4-v1p0 |

## Deployment and Users Guide


Toop Simulator is distributed as either a standalone runnable [jar bundle](https://repo1.maven.org/maven2/eu/toop/toop-simulator-ng/2.0.0-rc3/toop-simulator-ng-2.0.0-rc3-bundle.jar)
  or a [docker image](https://hub.docker.com/r/toop/toop-simulator-ng/tags) (latest version points to 2.0.0:rc3)

    docker pull toop/toop-simulator-ng:latest.


For TOOP clients development activities it is recommended to use the jar bundle and for server side deployment docker images are more convenient.

## Quickstart

Running the simulator without any extra parameter will launch the simulator in a closed circuit mode. You can type send-dc-request to see that the message goes to the connector and then DP, and the response is sent to connector and DC automatically.
Running Using the Jar Bundle

Please download the latest release from [here](https://repo1.maven.org/maven2/eu/toop/toop-simulator-ng/2.0.0-rc3/toop-simulator-ng-2.0.0-rc3-bundle.jar). And run the simulator with the default configurations as below:

    java -jar toop-simulator-ng-2.0.0-rc3-bundle.jar


### Running Via Docker Image

    docker run --rm -it --name toop-sim -p 8081:8081 toop/toop-simulator-ng

These commands will create a simulator instance in DP mode (see modes below) with an underlying toop-connector bound to port 8081.

### Persisting Datasets with Docker Image

The simulator has an embedded dp module what uses a set of files for generating responses. When the application is first run, a folder named 'datasets' is created on the current directory. If the directory already exists and the files with the same name already exist in it, then they are left untouched, otherwise they are created. This gives the users the opportunity to modify the files with respect to their use cases and save their data. In case of a docker container, this directory is /simulator/datasets; however it is inside the container and when the container is shut down, it is gone. If the data inside this directory needs to be persisted to some known location, then it needs to be mounted to a local volume on the host machine. In this case, the user has a chance to edit the contents of this directory and make them usable for future runs. In order to achieve this, the docker image can be launched as below:

    docker run --rm -it --name toop-sim -p 8081:8081 -v $(pwd)/datasets:/simulator/datasets toop/toop-simulator-ng
    
 The -v $(pwd)/datasets:/simulator/datasets parameter means "map the /simulator/datasets directory inside the container to the datasets directory in the current directory of the host machine". A sample listing of the datasets directory is given below.

    datasets
       edm-conceptRequest-lp.xml
       document
          LP12345.yaml
       gbm
          LP12345.yaml
          NP12345.yaml

### Docker Connectivity with a DP/DC running locally (on host)

When the simulator docker image is run for testing against a DC/DP that runs on the host machine (not a docker container), the DC or DP endpoint does not work with "localhost". In that case, the address of the module that runs on the host might be provided as below:

Windows: docker.for.win.localhost
Mac: docker.for.mac.localhost
Linux: 171.17.0.1

Example:

    docker run --rm -it \
          -e SIM_MODE=DP \
          -e DC_ENDPOINT="http://docker.for.mac.localhost:8080/to-dc" \
          -e CONNECTOR_PORT="8081" \
          -p 8081:8081 \
          toop/toop-simulator-ng
          
          



## Configuration Parameters

Below is given the table of parameters, their default values and descriptions. All parameters can be either provided as JVM args (i.e. -DXYZ) or ENV variables.

| Parameter  | Default Value | Description |
|-------|:---|:---|
| SIM_MODE | DP | The simulation mode, one of DP, SOLE and DC |
| CONNECTOR_PORT | 8081 | The port that the toop-connector and toop-simulator HTTP endpoints will be published on. |
| DC_ENDPOINT | http://localhost:${CONNECTOR_PORT}/to-dc | Data Consumer /to-dc endpoint |
| DP_ENDPOINT | http://localhost:${CONNECTOR_PORT}/to-dp |	Data Provider /to-dp endpoint |
| DP_RESPONSE_AUTO | TRUE | Determines whether the DP side should respond automatically (or not) to an incoming request |


When using docker images, these parameters can be provided by -e flag:
docker run --rm --name mysim -it \
     -e DP_ENDPOINT="http://some.dp/to-dp" \
     -e DC_ENDPOINT="http://some.dc/to-dc" \
     -e SIM_MODE=SOLE \
     -e CONNECTOR_PORT=9876 \
     -p 8080:9876 toop/toop-simulator-ng

## Simulation Modes

Toop simulator supports three working modes; namely DC, SOLE and DP (default). In all modes, a command line interface is also provided to the user.

### DC Mode

    As JVM ARG: -DSIM_MODE=DC
    As ENV variable: export SIM_MODE=DC
    
In DC mode, toop-simulator simulates a DC with a TOOP Connector and a real DP is being tested.  You may provide a URL for an external DP via the DP_ENDPOINT parameter. The default value for DP_ENDPOINT is given in the TOOP Simulator v2.0.0#Parameters section.

To launch the simulator in DC mode, run either of the following commands

      # using JVM ARGS
      java -DSIM_MODE=DC \
            -DDP_ENDPOINT="http://some.dp/to-dp" \
                -jar toop-simulator-2.0.0-rc3-bundle.jar
    
      # using ENV variables
      export SIM_MODE=DC
      export DP_ENDPOINT="http://some.dp/to-dp"
      java -jar toop-simulator-2.0.0-rc3-bundle.jar


      # via docker
      docker run --rm -it \
          -e SIM_MODE=DC \
          -e DP_ENDPOINT="http://some.dp/to-dp" \
          -p 8081:8081 \
          toop/toop-simulator-ng


### DP Mode

    As JVM ARG: -DSIM_MODE=DP
    As ENV variable: export SIM_MODE=DP

In DP mode, toop-simulator simulates a DP with a TOOP Connector and a real DC is being tested.  The requests received from the DC are automatically responded by the embedded DP simulator. You may provide a URL for an external DC via the DC_ENDPOINT variable. The default value for DC_ENDPOINT is given in the TOOP Simulator v2.0.0#Parameters section.

      # using JVM ARGS
      java -DSIM_MODE=DP \
          -DDC_ENDPOINT="http://some.dc/to-dc" \
                -jar toop-simulator-2.0.0-rc3-bundle.jar
      
      # using ENV variables
      export SIM_MODE=DP
      export DC_ENDPOINT="http://some.dc/to-dc"
      java -jar toop-simulator-2.0.0-rc3-bundle.jar


      # via docker
      docker run --rm -it \
            -e SIM_MODE=DP \
            -e DC_ENDPOINT="http://some.dc/to-dc" \
            -p 8081:8081 \
            toop/toop-simulator-ng


### SOLE Mode

    As JVM ARG: -DSIM_MODE=SOLE
    As ENV variable: export SIM_MODE=SOLE

In SOLE mode, toop-simulator runs a TOOP Connector and a real DC and a real DP are being tested.  The requests received from the DC are sent to the DP, and responses received from the DP are sent to the DC. You may provide a URL for the DC and DP via the DC_ENDPOINT and DP_ENDPOINT variables respectively. The default values for these parameters are provided in the TOOP Simulator v2.0.0#Parameters section.

To launch the simulator in SOLE mode, run one of the following commands

      #using JVM ARGS
      java -DSIM_MODE=SOLE \
          -DDC_ENDPOINT="http://some.dc/to-dc" \
                -DDP_ENDPOINT="http://some.dp/to-dp" \
                -jar toop-simulator-2.0.0-rc3-bundle.jar
      
      #using ENV variables
      export SIM_MODE=SOLE
      export  DC_ENDPOINT="http://some.dc/to-dc"
      export DP_ENDPOINT="http://some.dp/to-dp"
      java -jar toop-simulator-2.0.0-rc3-bundle.jar


      #via docker
      docker run --rm -it \
          -e SIM_MODE=SOLE \
          -e DC_ENDPOINT="http://some.dc/to-dc" \
          -e DP_ENDPOINT="http://some.dp/to-dp" \
          -p 8081:8081 \
          toop/toop-simulator-ng

## Command Line Interface

    TODO
