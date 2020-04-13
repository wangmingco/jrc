#!/usr/bin/env bash

cd ../../jrc-core
mvn clean install

cd ../jrc-sample/jrc-web-server
mvn clean install

java -jar ./target/jrc-web-server-0.1.jar