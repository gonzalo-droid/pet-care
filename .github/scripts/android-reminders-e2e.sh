#!/usr/bin/env bash
# Prueba de punta a punta del criterio de M2 en un emulador de Android:
#  1. el aviso llega con el proceso de la app muerto;
#  2. el aviso llega después de reiniciar el teléfono (lo reprograma RescheduleReceiver).
# Deja capturas de la bandeja de notificaciones en $1.
set -euo pipefail
OUT="${1:-build/android-e2e}"
mkdir -p "$OUT"
PKG=com.bestid.app
APK=androidApp/build/outputs/apk/debug/androidApp-debug.apk

tags() { adb shell dumpsys notification --noredact | grep -o "pkg=$PKG[^|]*tag=test:[0-9]*" | grep -o 'test:[0-9]*' | sort -u || true; }

wait_new_notification() { # $1 = etiquetas ya vistas, $2 = segundos máximos
  local seen="$1" deadline=$((SECONDS + $2))
  while [ $SECONDS -lt $deadline ]; do
    for t in $(tags); do
      if ! grep -qx "$t" <<<"$seen"; then echo "$t"; return 0; fi
    done
    sleep 5
  done
  return 1
}

shade() { # $1 = nombre de la captura
  adb shell cmd statusbar expand-notifications || true
  sleep 3
  adb exec-out screencap -p > "$OUT/$1.png"
  adb shell cmd statusbar collapse || true
}

wait_boot() {
  adb wait-for-device
  until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 3; done
  sleep 10
}

adb install -r "$APK"
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
adb shell am start -W -n "$PKG/.MainActivity"
sleep 5

echo "== 1. Aviso con la app cerrada"
seen="$(tags)"
adb shell am broadcast -n "$PKG/.DebugTestReminderReceiver" --el seconds 60
sleep 5
echo "Alarmas de la app: $(adb shell dumpsys alarm | grep -c "$PKG" || true)"
adb shell input keyevent KEYCODE_HOME
sleep 3
# Mata el proceso como lo haría el sistema por falta de memoria. No es "forzar detención",
# que además borraría las alarmas: aquí las alarmas deben seguir y despertar a la app.
adb shell am kill "$PKG" || true
sleep 2
if adb shell pidof "$PKG" > /dev/null; then
  adb shell run-as "$PKG" sh -c 'kill -9 $(pidof '"$PKG"')' || true
  sleep 2
fi
if adb shell pidof "$PKG" > /dev/null; then echo "El proceso sigue vivo"; exit 1; fi
echo "Proceso de la app terminado. Esperando el aviso..."
if tag=$(wait_new_notification "$seen" 1800); then
  echo "OK: llegó el aviso $tag con la app cerrada."
  shade 1-app-cerrada
else
  echo "FALLÓ: no llegó el aviso con la app cerrada."; shade 1-app-cerrada-fallo; exit 1
fi

echo "== 2. Aviso después de reiniciar"
seen="$(tags)"
adb shell am broadcast -n "$PKG/.DebugTestReminderReceiver" --el seconds 240
sleep 5
adb reboot
wait_boot
echo "Teléfono reiniciado. Esperando el aviso (sin abrir la app)..."
if tag=$(wait_new_notification "$seen" 1800); then
  echo "OK: llegó el aviso $tag después de reiniciar."
  shade 2-tras-reiniciar
else
  echo "FALLÓ: no llegó el aviso después de reiniciar."
  adb shell dumpsys alarm | grep -A3 "$PKG" | head -40 || true
  shade 2-tras-reiniciar-fallo; exit 1
fi
