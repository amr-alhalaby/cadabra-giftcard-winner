#!/bin/bash

# Run the batch job via Docker
# Usage: ./run.sh [csv-filename] [--keep-db-up]
# The file must exist inside the local data/ folder
# Example: ./run.sh purchases-external.csv --keep-db-up

CSV_FILE="${1:-purchases-external.csv}"
DATA_DIR="$(pwd)/data"
KEEP_DB_UP=false
RERUN=false

for arg in "$@"; do
  if [ "$arg" == "--keep-db-up" ]; then
    KEEP_DB_UP=true
  fi
  if [ "$arg" == "--rerun" ]; then
    RERUN=true
  fi
done

# Validate file exists
if [ ! -f "$DATA_DIR/$CSV_FILE" ]; then
  echo "ERROR: File not found: $DATA_DIR/$CSV_FILE"
  echo "Place your CSV file inside the data/ folder and try again."
  exit 1
fi

echo "Running batch job with CSV: data/$CSV_FILE"

if [ "$RERUN" == "true" ]; then
  # Postgres is already up — just rerun the app container
  CSV_FILE=$CSV_FILE docker compose run --rm --build app "purchases.csv.path=/app/data/$CSV_FILE"
elif [ "$KEEP_DB_UP" == "true" ]; then
  CSV_FILE=$CSV_FILE docker compose up --build --attach app
  echo "Postgres is still running — connect at localhost:5432 to inspect the DB"
  echo "Run 'docker compose down' when done"
else
  CSV_FILE=$CSV_FILE docker compose up --build --attach app --abort-on-container-exit --exit-code-from app
  docker compose down
fi
