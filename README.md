# Parados Android

Android App für die [Parados Board Games](https://game.ywesee.com/parados/) von Walter Prossnitz.

## Features

- Alle Parados-Spiele nativ auf Android spielbar (WebView)
- Offline-Gameplay — kein Internet nötig
- Remote-Multiplayer-Spiele öffnen im System-Browser (PeerJS/WebRTC benötigt HTTPS)
- Spiele-Updates direkt von GitHub via Menü
- Optimiert für verschiedene Android-Bildschirmgrössen
- Vollbild-Modus beim Spielen — Zurück-Button blendet sich nach 3s aus, Tap zum Einblenden
- Swipe vom linken Rand nach rechts zum Zurückkehren ins Menü
- Spielstand bleibt erhalten beim Wechsel ins Menü
- App-Icon: Känguru (kangy) — identisch mit der iOS-Version

## Spiele

- **DUK — The Impatient Kangaroo** (DE, EN, JP, CN, UA)
- **Capovolto** — Othello on steroids
- **Divided Loyalties** — Connect 4 mit 6 Farben
- **Democracy in Space** (Local, Remote)
- **Frankenstein** — Memory für 1–4 Spieler
- **Rainbow Blackjack** (DE, EN, Remote)
- **MAKA LAINA** (Local, Remote)

## Build

```bash
ANDROID_HOME=~/Android/Sdk ./gradlew assembleDebug
```

## Lizenz

GPLv3 — siehe [LICENSE](LICENSE)
