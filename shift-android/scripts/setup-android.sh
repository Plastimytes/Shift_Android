#!/usr/bin/env bash
# One-time setup for a Codespace / fresh Linux box. Needs internet. UNTESTED: the cmdline-tools filename below is from memory;
# if the download 404s, get the current "Command line tools only" Linux link from https://developer.android.com/studio and edit CMDLINE_ZIP.
set -euo pipefail
SDK="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_ZIP="commandlinetools-linux-11076708_latest.zip"
GRADLE_VERSION="8.9"
mkdir -p "$SDK/cmdline-tools" && cd "$SDK"
if [ ! -d cmdline-tools/latest ]; then
  curl -fsSL -o cmdline.zip "https://dl.google.com/android/repository/$CMDLINE_ZIP"
  unzip -q cmdline.zip -d cmdline-tools && mv cmdline-tools/cmdline-tools cmdline-tools/latest && rm cmdline.zip
fi
export PATH="$SDK/cmdline-tools/latest/bin:$PATH"
yes | sdkmanager --licenses >/dev/null || true
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
cd - >/dev/null
echo "sdk.dir=$SDK" > local.properties
if [ ! -f gradlew ]; then
  curl -fsSL -o /tmp/gradle.zip "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  unzip -q -o /tmp/gradle.zip -d "$HOME/gradle" && "$HOME/gradle/gradle-$GRADLE_VERSION/bin/gradle" wrapper --gradle-version "$GRADLE_VERSION"
fi
echo "Setup done. Next: ./gradlew testDebugUnitTest assembleDebug"
