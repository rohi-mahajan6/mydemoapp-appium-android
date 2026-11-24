#!/usr/bin/env bash
# Runs inside reactivecircus/android-emulator-runner once the emulator has booted.
# The runner executes each line of its `script` in a separate shell, so a background Appium server
# started there would not survive to the next line: everything that must share a lifetime lives here.
#
#   1. start logcat and the Appium server in the background, logging to artifacts/
#   2. wait until Appium answers on /status
#   3. run the TestNG suite with Maven
#   4. stop the background processes and exit with Maven's exit code
set -euo pipefail

APPIUM_HOST="${APPIUM_HOST:-127.0.0.1}"
APPIUM_PORT="${APPIUM_PORT:-4723}"
APPIUM_URL="http://${APPIUM_HOST}:${APPIUM_PORT}"
APK_PATH="${APK_PATH:?APK_PATH must point at the downloaded APK}"
ARTIFACTS="${ARTIFACTS:-artifacts}"

mkdir -p "$ARTIFACTS"
[ -f "$APK_PATH" ] || { echo "::error::APK not found at $APK_PATH"; exit 1; }

APPIUM_PID=""
LOGCAT_PID=""
cleanup() {
  [ -n "$APPIUM_PID" ] && kill "$APPIUM_PID" 2>/dev/null || true
  [ -n "$LOGCAT_PID" ] && kill "$LOGCAT_PID" 2>/dev/null || true
}
trap cleanup EXIT

echo "Device: $(adb shell getprop ro.product.model | tr -d '\r'), Android $(adb shell getprop ro.build.version.release | tr -d '\r') (API $(adb shell getprop ro.build.version.sdk | tr -d '\r'))"

adb logcat -c || true
adb logcat -v threadtime > "$ARTIFACTS/logcat.txt" 2>&1 &
LOGCAT_PID=$!

appium --address "$APPIUM_HOST" --port "$APPIUM_PORT" \
  --log "$ARTIFACTS/appium.log" --log-timestamp --log-no-colors --local-timezone \
  > "$ARTIFACTS/appium-stdout.log" 2>&1 &
APPIUM_PID=$!

for i in $(seq 1 60); do
  if curl -fsS "$APPIUM_URL/status" > /dev/null 2>&1; then
    echo "Appium is up after ${i} s: $(curl -fsS "$APPIUM_URL/status")"
    break
  fi
  if ! kill -0 "$APPIUM_PID" 2>/dev/null; then
    echo "::error::Appium exited during startup"; cat "$ARTIFACTS/appium-stdout.log"; exit 1
  fi
  if [ "$i" -eq 60 ]; then
    echo "::error::Appium did not answer on $APPIUM_URL/status within 60 s"; cat "$ARTIFACTS/appium-stdout.log"; exit 1
  fi
  sleep 1
done

set +e
mvn -B -ntp test -Dapk.path="$APK_PATH" -Dappium.url="$APPIUM_URL"
STATUS=$?
set -e

echo "Maven exited with $STATUS"
exit "$STATUS"
