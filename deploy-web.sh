#!/usr/bin/env bash
# Локальний деплой фронта на GitHub Pages БЕЗ білду в CI.
# Збирає прод-дист на цьому компі й force-push'ить його у гілку gh-pages.
#
# Передумова (один раз): Settings → Pages → Source = "Deploy from a branch" → gh-pages → /(root).
set -euo pipefail

DOMAIN="cashpilot.qlodi.app"   # ← custom-домен Pages (CNAME → nonsense898.github.io)
DIST="composeApp/build/dist/wasmJs/productionExecutable"

cd "$(dirname "$0")"
ORIGIN="$(git remote get-url origin)"

echo "▶ Building production wasm…"
./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon

echo "▶ Preparing artifact…"
echo "$DOMAIN" > "$DIST/CNAME"
touch "$DIST/.nojekyll"

echo "▶ Force-pushing to gh-pages…"
pushd "$DIST" >/dev/null
rm -rf .git
git init -q
git checkout -q -b gh-pages
git add -A
git commit -q -m "deploy $(date -u +%FT%TZ)"
git push -q -f "$ORIGIN" gh-pages
rm -rf .git
popd >/dev/null

echo "✓ Deployed → https://$DOMAIN  (Pages оновиться за ~1 хв)"
