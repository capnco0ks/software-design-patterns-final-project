#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/classes
find src/main/java -name '*.java' -print > build/sources.txt
javac --release 21 --add-modules jdk.httpserver -encoding UTF-8 -d build/classes @build/sources.txt
java --add-modules jdk.httpserver -cp build/classes com.eventstudio.App
