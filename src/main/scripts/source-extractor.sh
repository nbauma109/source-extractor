#!/bin/bash
SCRIPT_DIR=$(dirname "$0")
"$SCRIPT_DIR/bin/java-source-extractor" "$@"
"$SCRIPT_DIR/bin/scala-source-extractor" "$@"
