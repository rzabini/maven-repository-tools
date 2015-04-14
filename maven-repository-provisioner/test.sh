#!/bin/bash
# fail if anything errors
set -e
# fail if a function call is missing an argument
set -u

# for my local Nexus
source=http://localhost:8081/nexus/content/groups/public
target=http://localhost:8081/nexus/content/repositories/tmp
# for my local Nexus 3
#source=http://localhost:9081/content/groups/public
#target=http://localhost:9081/content/repositories/tmp

function deploy {
  java -jar target/maven-reposito*-with-dependencies.jar -s $source  -t $target -u admin -p admin123 -a $1
}

# deploy "junit:junit:4.11|junit:junit:3.8.1|com.squareup.assertj:assertj-android:aar:1.0.0"

# deploy "org.apache.maven.plugins:maven-surefire-plugin:jar:2.18.1"

# deploy "org.apache.commons:commons-lang3:jar:3.3.2"

# deploy "com.google.inject:guice:no_aop:jar:3.0"

# deploy "org.apache.commons:commons-lang3:jar:3.3.2|junit:junit:4.11|com.squareup.assertj:assertj-android:aar:1.0.0"

deploy "com.simpligility.maven:progressive-organization-pom:pom:2.3.0"

