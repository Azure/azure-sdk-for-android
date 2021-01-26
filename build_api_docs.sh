#!/bin/sh
set -e
./gradlew -c release-settings.gradle clean
rm -rf build/dokka
./gradlew -c release-settings.gradle dokkaHtmlMultiModule
cp build/dokka/htmlMultiModule/-modules.html build/dokka/htmlMultiModule/index.html
cp eng/docs/assets/docs_logo.svg build/dokka/htmlMultiModule/images/docs_logo.svg
