#!/bin/bash
echo "Create XML Plugin file lists..."

find ./src/main/java/ -name "*.xml" > ./src/main/resources/plugins.txt

echo "READY"
