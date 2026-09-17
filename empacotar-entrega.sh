#!/usr/bin/env bash
# Gera os zips da entrega em entrega/: backend, app (com o APK) e a documentação.
# Uso: ./empacotar-entrega.sh
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SAIDA="$RAIZ/entrega"
APK="$RAIZ/app/app/build/outputs/apk/debug/app-debug.apk"

command -v zip >/dev/null || { echo "Instale o zip: sudo apt install zip"; exit 1; }

echo "==> Limpando saída anterior"
rm -rf "$SAIDA"
mkdir -p "$SAIDA"

echo "==> Backend"
cd "$RAIZ"
zip -qr "$SAIDA/backend.zip" backend \
  -x 'backend/target/*' 'backend/.env' 'backend/.idea/*' 'backend/**/.DS_Store'
echo "    $(du -h "$SAIDA/backend.zip" | cut -f1)"

echo "==> App"
if [ ! -f "$APK" ]; then
  echo "    APK não encontrado; gerando..."
  (cd "$RAIZ/app" && ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}" ./gradlew assembleDebug -q)
fi
TEMP_APK="$RAIZ/app/app-debug.apk"
cp "$APK" "$TEMP_APK"
zip -qr "$SAIDA/app.zip" app \
  -x 'app/app/build/*' 'app/build/*' 'app/.gradle/*' 'app/local.properties' 'app/.idea/*' 'app/**/.DS_Store'
rm -f "$TEMP_APK"
cp "$APK" "$SAIDA/app-debug.apk"
echo "    $(du -h "$SAIDA/app.zip" | cut -f1) (APK incluído dentro do zip e solto em entrega/)"

echo "==> Documentação"
zip -qr "$SAIDA/documentacao.zip" docs README.md PLANEJAMENTO.md
echo "    $(du -h "$SAIDA/documentacao.zip" | cut -f1)"

echo
echo "Pronto. Arquivos em entrega/:"
ls -lh "$SAIDA" | tail -n +2 | awk '{print "  " $9 " (" $5 ")"}'
echo
echo "Falta anexar a apresentação (PDF/PPT) — veja docs/APRESENTACAO.md"
