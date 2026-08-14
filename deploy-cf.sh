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

# Preload wasm-модулів. Без цього браузер дізнається про них лише коли
# завантажить і виконає composeApp.js — а це 5 МБ, які могли б уже їхати.
# Імена хешовані щобілду, тож теги генеруємо тут, а не тримаємо в index.html.
python3 - "$DIST" <<'PY'
import os, sys
dist = sys.argv[1]
wasm = sorted((f for f in os.listdir(dist) if f.endswith(".wasm")),
              key=lambda f: -os.path.getsize(os.path.join(dist, f)))
if not wasm:
    sys.exit("no .wasm in dist — білд зламався?")
tags = "\n".join(
    f'    <link rel="preload" href="{f}" as="fetch" type="application/wasm" crossorigin />'
    for f in wasm
)
p = os.path.join(dist, "index.html")
html = open(p).read()
if "rel=\"preload\"" in html:
    sys.exit(0)
html = html.replace("</head>", tags + "\n</head>", 1)
open(p, "w").write(html)
print(f"  + preload: {', '.join(wasm)}")
PY

# Проєкт створюється лише раз; deploy у неіснуючий проєкт падає з помилкою.
if ! npx --yes wrangler@latest pages project list 2>/dev/null | grep -qw "$PROJECT"; then
  echo "▶ Creating Pages project ${PROJECT}…"
  npx --yes wrangler@latest pages project create "$PROJECT" --production-branch main
fi

echo "▶ Deploying to Cloudflare Pages ($PROJECT)…"
npx --yes wrangler@latest pages deploy "$DIST" --project-name "$PROJECT" --branch main --commit-dirty=true

echo "✓ Deployed. Кастомний домен cashpilot.qlodi.app прив'язується один раз:"
echo "   Cloudflare → Workers & Pages → $PROJECT → Custom domains"
