#!/bin/bash

release="2014-06-12"
version=3.0-$release
groupId=org.jmonkeyengine
depGroupId=$groupId.jme
projectPath=jmonkeyengine
url="http://updates.jmonkeyengine.org/stable/3.0/engine/jME3_${release}.zip"
whereami=$PWD

mkdir target
cd target

mkdir -p $projectPath

wget $url -O jME3.zip

unzip jME3.zip

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

cd $projectPath
mvn clean install

cd $whereami
