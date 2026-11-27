#!/usr/bin/env bash
# Flujo E2E con Maestro (TST-08) en el emulador o simulador que esté abierto. Instala el build de debug
# si se indica la plataforma. Uso: .github/scripts/e2e.sh [android|ios]
set -euo pipefail
command -v maestro >/dev/null || { echo "Falta Maestro: https://docs.maestro.dev/getting-started/installing-maestro"; exit 1; }
mkdir -p build/e2e
case "${1:-}" in
  android) adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk ;;
  ios)
    APP=$(find iosApp/build -name "BestID.app" -path "*iphonesimulator*" | head -1)
    [ -n "$APP" ] || { echo "No se encontró BestID.app (compila iOS primero)"; exit 1; }
    xcrun simctl install booted "$APP" ;;
esac
maestro test .maestro/smoke.yaml
