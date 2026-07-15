#!/bin/sh
# Maven Wrapper - 自动下载 Maven 并执行
MAVEN_VERSION=3.9.9
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
MAVEN_HOME="$HOME/.m2/wrapper/apache-maven-$MAVEN_VERSION"
MAVEN_CMD="$MAVEN_HOME/bin/mvn"

if [ ! -f "$MAVEN_CMD" ]; then
  echo "Downloading Maven $MAVEN_VERSION..."
  mkdir -p "$HOME/.m2/wrapper"
  curl -sL "$MAVEN_URL" -o /tmp/maven.zip
  unzip -q /tmp/maven.zip -d "$HOME/.m2/wrapper"
  rm -f /tmp/maven.zip
fi

exec "$MAVEN_CMD" "$@"
