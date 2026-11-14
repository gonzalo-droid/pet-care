#!/usr/bin/env bash
# Reglas de CLAUDE.md que se pueden revisar sin compilar. Corre en el CI (Linux) y en local.
set -uo pipefail
cd "$(dirname "$0")/../.."

fail=0
check() {
  local title="$1" pattern="$2"; shift 2
  local hits
  hits=$(grep -rnE "$pattern" "$@" --include=*.kt 2>/dev/null | grep -v "/build/" || true)
  if [ -n "$hits" ]; then
    echo "✗ $title"
    echo "$hits" | sed 's/^/    /'
    fail=1
  else
    echo "✓ $title"
  fi
}

UI=(feature composeApp/src/commonMain core/ui/src/commonMain)

# Regla 1: colores, tamaños y tipografía solo desde los tokens del design system.
check "Regla 1: sin colores sueltos fuera del design system" 'Color\(0x|Color\.(Red|Blue|Green|Black|White|Gray)' "${UI[@]}"
check "Regla 1: sin tamaños sueltos (.dp/.sp) fuera del design system" '[0-9]\.(dp|sp)\b' "${UI[@]}"
# Regla 2: ningún texto bajo 13 sp en los tokens.
small=$(grep -oE 'style\(([0-9]+),' core/designsystem/src/commonMain/kotlin/com/bestid/core/designsystem/theme/Type.kt | grep -oE '[0-9]+' | awk '$1 < 13')
if [ -n "$small" ]; then echo "✗ Regla 2: hay estilos de texto bajo 13 sp: $small"; fail=1; else echo "✓ Regla 2: ningún estilo de texto bajo 13 sp"; fi
# Regla 3: textos en Compose Resources, no escritos en el código.
check "Regla 3: sin Text(\"...\") escrito en el código" 'Text\(\s*"[^"]' "${UI[@]}"
check "Regla 3: sin contentDescription escrito en el código" 'contentDescription = "[^"]' "${UI[@]}" core/designsystem/src/commonMain
# Regla 6: sin datos personales en logs.
check "Regla 6: sin println ni logs en el código de la app" '\bprintln\(|Log\.(d|i|w|e|v)\(|NSLog\(' feature composeApp/src/commonMain core/*/src/commonMain core/*/src/androidMain core/*/src/iosMain
# Regla 6 y 7: archivos que nunca se suben.
for f in google-services.json GoogleService-Info.plist; do
  if git ls-files --error-unmatch "**/$f" "$f" >/dev/null 2>&1; then echo "✗ Regla 6: $f está en el repositorio"; fail=1; fi
done
if git ls-files testdata/private | grep -q .; then echo "✗ Regla 7: hay archivos de testdata/private en el repositorio"; fail=1; else echo "✓ Regla 7: testdata/private fuera del repositorio"; fi
# D1: un feature nunca depende de otro feature.
cross=$(grep -rnE 'import com\.bestid\.feature\.([a-z]+)' feature --include=*.kt | grep -v /build/ | awk -F'feature/' '{split($2,a,"/"); mod=a[1]; if (match($0, /com\.bestid\.feature\.[a-z]+/)) { imp=substr($0, RSTART+19, RLENGTH-19); if (imp != mod) print }}')
if [ -n "$cross" ]; then echo "✗ D1: un feature importa otro feature"; echo "$cross" | sed 's/^/    /'; fail=1; else echo "✓ D1: ningún feature importa otro"; fi

exit $fail
