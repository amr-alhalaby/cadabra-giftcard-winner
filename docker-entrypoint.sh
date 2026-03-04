#!/bin/sh

# Append a unique timestamp as a non-identifying job parameter
# so Spring Batch allows the same CSV to be run multiple times
RUN_AT=$(date +%s)

exec java -jar app.jar "$@" "-run.at=$RUN_AT"

