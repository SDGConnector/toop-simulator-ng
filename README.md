# TOOP Simulator Next Generation

[![Build Status](https://api.travis-ci.com/TOOP4EU/toop-simulator-ng.svg?branch=master)](https://travis-ci.com/TOOP4EU/toop-simulator-ng)

**Latest Release:** [2.0.0-rc3](https://repo1.maven.org/maven2/eu/toop/toop-simulator-ng/2.0.0-rc3/)

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
| EDM Concept Request	| Elonia DP	| iso6523-actorid-upis::9999:elonia	| toop-doctypeid-qns::RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.0 | toop-procid-agreement::urn:eu.toop.process.dataquery | bdxr-transport-ebms3-as4-v1p0 |
| EDM Document Request	| Elonia DP	| iso6523-actorid-upis::9999:elonia| 	toop-doctypeid-qns::FinancialRecord::FINANCIAL_RECORD_TYPE::UNSTRUCTURED::toop-edm:v2.0	| toop-procid-agreement::urn:eu.toop.process.documentquery |	bdxr-transport-ebms3-as4-v1p0| 
| EDM Response (Concept)	| Freedonia DC	| iso6523-actorid-upis::9999:freedonia | toop-doctypeid-qns::QueryResponse::toop-edm:v2.0	toop-procid-agreement::urn:eu.toop.process.dataquery	| bdxr-transport-ebms3-as4-v1p0| 
| EDM Response (Document)	|  Freedonia DC	| iso6523-actorid-upis::9999:freedonia | 	toop-doctypeid-qns::QueryResponse::toop-edm:v2.0| bdxr-transport-ebms3-as4-v1p0 |

## Deployment and Users Guide


Toop Simulator is distributed as either a standalone runnable [jar bundle](https://repo1.maven.org/maven2/eu/toop/toop-simulator-ng/2.0.0-rc3/toop-simulator-ng-2.0.0-rc3-bundle.jar)
  or a [docker image](https://hub.docker.com/r/toop/toop-simulator-ng/tags) (latest version points to 2.0.0:rc3)

    docker pull toop/toop-simulator-ng:latest.


For TOOP clients development activities it is recommended to use the jar bundle and for server side deployment docker images are more convenient.

