#!/usr/bin/env bash


# This bash script runs maven build for vaadin production
# and then creates a dockerfile with the pom version.

version='2.1.0-1'
artifact=toop-simulator-ng

echo Building toop/${toop-simulator-ng}:${version}

#mvn clean package -Pproduction

docker build --build-arg VERSION=${version} -t toop/${artifact}:${version} .
docker build --build-arg VERSION=${version} -t toop/${artifact}:latest .