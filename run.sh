#!/bin/bash

./gradlew clean build -x test --no-daemon
java -jar build/libs/geektrust.jar sample_input/input2.txt