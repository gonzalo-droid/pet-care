#!/usr/bin/env bash
# Instala la app en un simulador de iPhone, la abre y verifica que siga viva tras unos segundos.
# Deja capturas en $1 (directorio).
set -euo pipefail
OUT="${1:-build/ios-smoke}"
mkdir -p "$OUT"
APP=$(find iosApp/build -name "BestID.app" -path "*iphonesimulator*" | head -1)
[ -n "$APP" ] || { echo "No se encontró BestID.app"; exit 1; }

DEVICE=$(xcrun simctl list devices available -j | python3 -c '
import json, sys
devices = json.load(sys.stdin)["devices"]
phones = [d for runtime, ds in devices.items() if "iOS" in runtime for d in ds if d["name"].startswith("iPhone")]
print(phones[-1]["udid"])')
echo "Simulador: $DEVICE"
xcrun simctl boot "$DEVICE" || true
xcrun simctl bootstatus "$DEVICE" -b
xcrun simctl install "$DEVICE" "$APP"
xcrun simctl launch "$DEVICE" com.bestid.app
sleep 15
xcrun simctl io "$DEVICE" screenshot "$OUT/arranque.png"
if xcrun simctl spawn "$DEVICE" launchctl list | grep -q "com.bestid.app"; then
  echo "La app sigue abierta después de 15 segundos."
else
  echo "La app se cerró."
  xcrun simctl spawn "$DEVICE" log show --last 2m --predicate 'process == "BestID"' | tail -80 || true
  exit 1
fi
