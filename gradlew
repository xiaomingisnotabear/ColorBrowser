#!/bin/sh
#
# Gradle start up script for UN*X
#

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

DIRNAME=$(dirname "$0")
WRAPPER_JAR="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "ERROR: Could not find Gradle wrapper JAR" >&2
    exit 1
fi

exec java $DEFAULT_JVM_OPTS -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
