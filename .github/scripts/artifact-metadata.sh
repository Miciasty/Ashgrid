#!/usr/bin/env bash
set -euo pipefail

mvn -B -q org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate \
  -Dexpression=project.build.finalName -Doutput=target/artifact-name.txt
final_name=$(cat target/artifact-name.txt)
test -s "target/$final_name.jar"
test -s "target/$final_name-sources.jar"
test -s "target/$final_name-javadoc.jar"
echo "finalName=$final_name" >> "$GITHUB_OUTPUT"
