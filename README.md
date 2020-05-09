# TOOP Simulator Next Generation


This is a web application that provides simulated DSD, SMP and MEM interfaces.

## DSD Interface

This is a `RegRep` REST interface provided via `/rest/search`. It accepts 3 parameters as shown below


queryId	MUST	This parameter refers to the {urn:oasis:names:tc:ebxml-regrep:xsd:rim:4.0}:QueryDefinitionType defined on the server. Its current (default) value is urn:top:dsd:ebxml-regrem:queries:DataSetRequest.
dataSetType	MUST	The dataset type id
countryCode	OPTIONAL	Two letter country code

| Parameter        | Requirement   | Description  |
| -----------------|:--------------|:-------------|
| queryId          | MUST          | This parameter refers to the {urn:oasis:names:tc:ebxml-regrep:xsd:rim:4.0}:QueryDefinitionType defined on the server. Its current (default) value is urn:top:dsd:ebxml-regrem:queries:DataSetRequest |
| dataSetType      | MUST      |   The dataset type id |
| countryCode | OPTIONAL | Two letter country code |

For more details please visit [TOOP DSD Wiki](http://wiki.ds.unipi.gr/display/TOOP/.Data+Services+Directory+v2.0.1)

## SMP Interface
Todo


## MEM Interface

The Message Exchange Module simulator.

### Usage

TOOP Simulator Next Generation can be used in two ways:

1) By cloning and building this repository and deploying the war file to an application server like tomcat.
2) Via docker image (docker pull toop/dsd:2.0.0-SNAPSHOT)
