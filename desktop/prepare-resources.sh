#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

JRE_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.19%2B10/OpenJDK17U-jre_aarch64_mac_hotspot_17.0.19_10.tar.gz"

(cd .. && ./gradlew bootJar)
cp ../build/libs/pm-management-tool-0.0.1-SNAPSHOT.jar resources/backend.jar

if [ ! -x resources/jre/Contents/Home/bin/java ]; then
	rm -rf resources/jre
	curl -sL "$JRE_URL" -o /tmp/temurin-jre.tar.gz
	tar xzf /tmp/temurin-jre.tar.gz -C resources
	mv resources/jdk-17*-jre resources/jre
	rm /tmp/temurin-jre.tar.gz
fi
