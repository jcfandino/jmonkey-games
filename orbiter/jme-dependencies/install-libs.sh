#!/bin/bash

version=3.0-SNAPSHOT
groupId=org.jmonkeyengine
depGroupId=$groupId.jme
projectPath=jmonkeyengine
mkdir -p $projectPath

echo "
<?xml version=\"1.0\"?>
<project>
	<modelVersion>4.0.0</modelVersion>
	<groupId>$groupId</groupId>
	<version>$version</version>
	<artifactId>jme</artifactId>
	<packaging>jar</packaging>
	<name>JME 3</name>
	<dependencies>
" > $projectPath/pom.xml

for f in $(ls lib); do
	name=$(echo $f | sed 's/\.jar$//')
	echo \
"		<dependency>
			<groupId>$depGroupId</groupId>
			<artifactId>$name</artifactId>
			<version>$version</version>
		</dependency>
" >> $projectPath/pom.xml
	mvn install:install-file \
		-DartifactId=$name \
		-DgroupId=$depGroupId \
		-Dpackaging=jar \
		-Dversion=$version \
		-Dfile=lib/$f
done

echo '
	</dependencies>
</project>
' >> $projectPath/pom.xml

