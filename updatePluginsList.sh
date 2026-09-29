#!/bin/bash
echo "Create XML Plugin file lists..."

# Sorted so the list does not depend on the file system; CI compares it with
# the committed file.
find ./src/main/java/ -name "*.xml" | LC_ALL=C sort > ./src/main/resources/plugins.txt

echo "READY"
