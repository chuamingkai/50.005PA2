#!/bin/bash
javac Client"$1".java
java Client"$1" "${@:2}"