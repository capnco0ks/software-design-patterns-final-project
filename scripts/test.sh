#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/test-classes
find src/main/java src/test/java -name '*.java' -print > build/test-sources.txt
javac --release 21 --add-modules jdk.httpserver -encoding UTF-8 -d build/test-classes @build/test-sources.txt
java --add-modules jdk.httpserver -cp build/test-classes com.eventstudio.application.EventCatalogTest
java --add-modules jdk.httpserver -cp build/test-classes com.eventstudio.application.EventPlanningFacadeTest
java --add-modules jdk.httpserver -cp build/test-classes com.eventstudio.application.BookingServiceTest
