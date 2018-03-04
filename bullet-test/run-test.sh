#!/bin/bash

echo cleaning
rm -r repo/org/jmonkeyengine/jme3-bullet
rm -r repo/org/jmonkeyengine/jme3-bullet-native
rm *.so

pathjar=$(find /home/xuan/src/jmonkeyengine/jme3-bullet/build/libs -not -name '*-sources.jar' -name '*.jar')
pathnat=$(find /home/xuan/src/jmonkeyengine/jme3-bullet-native/build/libs -not -name '*-sources.jar' -name '*.jar')
echo installing from $path
mvn clean install:install-file -Dfile=$pathjar -DgroupId=org.jmonkeyengine -DartifactId=jme3-bullet -Dversion=3.2.0-custom-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=repo
mvn clean install:install-file -Dfile=$pathnat -DgroupId=org.jmonkeyengine -DartifactId=jme3-bullet-native -Dversion=3.2.0-custom-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=repo

echo FILES
find repo/org/jmonkeyengine/jme3-bullet -name '*.jar'
find repo/org/jmonkeyengine/jme3-bullet-native -name '*.jar'

mvn clean package
java -jar target/bullet-test-1.0-SNAPSHOT-jar-with-dependencies.jar

