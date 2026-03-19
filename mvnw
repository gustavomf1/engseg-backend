#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF)
# Maven Wrapper startup script
# ----------------------------------------------------------------------------

if [ -z "$JAVA_HOME" ] ; then
  JAVACMD=`which java`
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-"$(pwd)"}"
MAVEN_HOME="${MAVEN_HOME:-/home/mag/.sdkman/candidates/maven/current}"

exec "$MAVEN_HOME/bin/mvn" "$@"
