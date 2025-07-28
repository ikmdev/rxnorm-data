#!/bin/bash

# Check arguments

if [ "$#" -ne 3 ]; then

  echo "Usage: ./run-data-pipeline.sh [profile1|profile2] [dataStoreLocation] [dataStore]"

  exit 1

fi

PROFILE="$1"
DATA_STORE_LOCATION="$2"
DATA_STORE="$3"

BASE_DIR="$HOME/GIT"
RXNORM_DIR="$BASE_DIR/rxnorm-data"
SNOMED_DIR="$BASE_DIR/snomed-ct-loinc-data"

if [ "$PROFILE" == "profile1" ]; then

  echo "=== Running profile1 ==="



  for module in plugin rxnorm-origin rxnorm-pipeline rxnorm-starterdata; do

    echo "Running: rxnorm-data/$module"

    cd "$RXNORM_DIR/$module" && mvn clean install -DdataStoreLocation="$DATA_STORE_LOCATION" -DdataStore="$DATA_STORE"

  done



  for module in plugin snomed-ct-loinc-origin snomed-ct-loinc-starterdata snomed-ct-loinc-pipeline snomed-ct-loinc-integration snomed-ct-loinc-export; do

    echo "Running: snomed-ct-loinc-data/$module"

    cd "$SNOMED_DIR/$module" && mvn clean install -DdataStoreLocation="$DATA_STORE_LOCATION" -DdataStore="$DATA_STORE"

  done

elif [ "$PROFILE" == "profile2" ]; then

  echo "=== Running profile2 ==="

  for module in rxnorm-owl-transform rxnorm-reasoner; do
    echo "Running: rxnorm-data/$module"
    cd "$RXNORM_DIR/$module" && mvn clean install -DdataStoreLocation="$DATA_STORE_LOCATION" -DdataStore="$DATA_STORE"
  done

  for module in snomed-ct-loinc-owl-transform snomed-ct-loinc-reasoner; do
    echo "Running: snomed-ct-loinc-data/$module"
    cd "$SNOMED_DIR/$module" && mvn clean install -DdataStoreLocation="$DATA_STORE_LOCATION" -DdataStore="$DATA_STORE"
  done

else

  echo "Invalid profile: $PROFILE"

  echo "Please use either profile1 or profile2."

  exit 1

fi