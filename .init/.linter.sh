#!/bin/bash
cd /home/kavia/workspace/code-generation/android-tv-tic-tac-toe-220468-220477/android_tv_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

