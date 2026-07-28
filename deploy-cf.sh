#!/usr/bin/env bash
# Деплой фронта на Cloudflare Pages (альтернатива deploy-web.sh / GitHub Pages).
#
# Навіщо: GH Pages віддає max-age=600 і лише gzip. Cloudflare дає brotli
# (skiko 3.16 → ~2.5 МБ) і immutable-кеш на хешовані ассети — повторний
# захід стає майже миттєвим (див. _headers у resources).
#
# Разово: npx wrangler login   (відкриє браузер для авторизації)
set -euo pipefail

PROJECT="qlodi-cashpilot"
DIST="composeApp/build/dist/wasmJs/productionExecutable"

cd "$(dirname "$0")"

echo "▶ Building production wasm…"
./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon

# Source map у прод не потрібен (1.7 МБ мертвої ваги в деплої).
rm -f "$DIST"/*.js.map

# Проєкт створюється лише раз; deploy у неіснуючий проєкт падає з помилкою.
if ! npx --yes wrangler@latest pages project list 2>/dev/null | grep -qw "$PROJECT"; then
  echo "▶ Creating Pages project $PROJECT…"
  npx --yes wrangler@latest pages project create "$PROJECT" --production-branch main
fi

echo "▶ Deploying to Cloudflare Pages ($PROJECT)…"
npx --yes wrangler@latest pages deploy "$DIST" --project-name "$PROJECT" --commit-dirty=true

echo "✓ Deployed. Кастомний домен cashpilot.qlodi.app прив'язується один раз:"
echo "   Cloudflare → Workers & Pages → $PROJECT → Custom domains"
