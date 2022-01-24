#!/bin/bash

mysql -u ad < dropSpatialIndex.sql

mysql -u ad < createSpatialIndex.sql

sh ./run-Indexer.sh
javac -cp /usr/share/java/mysql-connector-java-5.1.28.jar:/usr/share/java/lucene-core-5.4.0.jar:/usr/share/java/lucene-analyzers-common-5.4.0.jar:/usr/share/java/lucene-queryparser-5.4.0.jar:/usr/share/java/lucene-queries-5.4.0.jar:. Searcher.java

# Remove all temporary files
rm *.class
rm *.csv
