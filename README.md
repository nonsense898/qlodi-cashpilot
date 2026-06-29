# Qlodi CashPilot

Повноцінна бухгалтерія (аналог QuickBooks) для SaaS-фаундерів — облік подвійного
запису з мультиюрисдикційністю **UA / EU / US**. Частина екосистеми Qlodi
(Business · CashPilot · Personal). CashPilot — **system of record**: веде леджер і
годує Qlodi Business живими даними.

> Статус: **скелет** (Compose Multiplatform). Архітектура за ТЗ; функціонал додається фазами.

## Стек
Kotlin Multiplatform + Compose Multiplatform (як frc-business / frc-personal).
Таргети: **android · iOS · wasmJs**. Спільний бекенд — `api.qlodi.app` (Ktor).

## Структура модулів
```
composeApp/        # застосунок: android / iOS / wasmJs точки входу + App (ліва навігація)
core/domain/       # доменна модель леджера (Account, JournalEntry, JournalLine, періоди)
shared/ui/         # бренд-тема (dark + cyan) + навігація
```
Далі за патерном frc-business додаються `core/data` (API-клієнт до `api.qlodi.app`)
та `features/*` (ledger, banking, invoices, reports, taxes…).

## Запуск
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun   # web (dev)
./gradlew :composeApp:installDebug                  # android
```

## Деплой (web → GitHub Pages)
```bash
./deploy-web.sh    # build → push gh-pages → cashpilot.qlodi.app
```
DNS: `CNAME cashpilot → nonsense898.github.io`. Pages: Deploy from branch → `gh-pages`.

## Архітектура (з ТЗ)
- **Одне jurisdiction-agnostic ядро** подвійного запису + **Jurisdiction Packs** (UA/EU/US).
- **Event-sourced immutable ledger** — виправлення лише сторно; усі суми — decimal.
- Звіти деривуються з леджера: P&L, Balance Sheet, Cash Flow, Trial Balance, GL.
- Інтеграція з Business через події `JournalPosted` (аналітика на живих даних).

Документація — `Qlodi_CashPilot_Package/` (повне ТЗ, Фаза 0, UA/EU паки, payroll, інтеграція, design-бриф).
