#!/bin/bash

# Remove all temporary files
rm *.class

mysql ad < dropSpatialIndex.sql

mysql ad < createSpatialIndex.sql

export CLASSPATH="/usr/share/java/mysql-connector-java-5.1.28.jar:/usr/share/java/lucene-core-5.4.0.jar:/usr/share/java/lucene-analyzers-common-5.4.0.jar:/usr/share/java/lucene-queryparser-5.4.0.jar:/usr/share/java/lucene-queries-5.4.0.jar:."
sh ./run-Indexer.sh
javac Searcher.java
