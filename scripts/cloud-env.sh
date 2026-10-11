#!/usr/bin/env bash
# Source this file in cloud tasks. Android Studio uses its own SDK/JDK settings.
export DINO_TOOLING_ROOT="${DINO_TOOLING_ROOT:-/workspace/tooling}"
export JAVA_HOME="$DINO_TOOLING_ROOT/jdk-21.0.12.1+1"
export ANDROID_HOME="$DINO_TOOLING_ROOT/android-sdk"
export ANDROID_USER_HOME="$DINO_TOOLING_ROOT/.android"
export ANDROID_SDK_HOME="$DINO_TOOLING_ROOT"
export GRADLE_USER_HOME="$DINO_TOOLING_ROOT/gradle-home"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
