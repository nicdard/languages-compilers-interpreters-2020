#!/bin/bash

echo "Run test suite"

for test in ./test/*.mc; do
    ./microcc $test &> "$test.dump"
done