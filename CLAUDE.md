# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Parados Android — a WebView-based Android app that plays HTML board games by Walter Prossnitz offline on Android devices. Games are self-contained HTML files (CSS/JS inline) loaded in a full-screen WebView. Licensed under GPLv3.

## Build & Install

```bash
# Debug build
ANDROID_HOME=~/Android/Sdk ./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Release bundle (for Google Play)
ANDROID_HOME=~/Android/Sdk ./gradlew bundleRelease

# Upload to Google Play
./apkup_bundle

# Clean
ANDROID_HOME=~/Android/Sdk ./gradlew clean
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`
AAB output: `app/build/outputs/bundle/release/app-release.aab`

## Release Signing

Release signing uses `signing.properties` (gitignored) with `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. The keystore file and Google Play service account JSON (`parados.json`) are also gitignored. The `apkup_bundle` script builds the AAB and uploads via `android-bundle-uploader` directly as a completed production release (no manual approval needed in Play Console).

## Architecture

- **Two Activities** (both `singleTop`): `MainActivity` (game list with RecyclerView) → `GameActivity` (per-game WebViews in a FrameLayout container). Navigation uses `FLAG_ACTIVITY_REORDER_TO_FRONT` to preserve game state — GameActivity is not finished when returning to menu
- **ParadosApp (Application singleton)**: WebView instances are stored in `ParadosApp.webViews` (Application-level `Map<String, WebView>`) so they survive Activity destruction (e.g. Android killing a backgrounded Activity due to memory pressure or network changes). GameActivity accesses them via `(application as ParadosApp).webViews`
- **Per-game WebViews**: Each game gets its own WebView instance. Switching games hides the current WebView and shows (or creates) the one for the target game, preserving full JS game state across game switches. WebViews are created with `applicationContext` to avoid Activity leaks
- **Back button**: Auto-hides after 3s with fade-out, reappears on tap. Left-edge swipe (start <50px, drag >200px, strict direction) also returns to menu. Android back button checks for open rules modals (`rulesModal` / `rules-modal`) via JS and closes them instead of navigating away
- **Injected CSS/JS**: All games receive `padding-top: 56px` via injected CSS so content doesn't hide behind the back FAB button. Status bar stays visible (black) while navigation bar is hidden
- **GameRepository**: Copies bundled HTML from `assets/games/` to internal storage; re-copies when `versionCode` changes; handles GitHub update downloads
- **GameInfo**: Hardcoded game catalog with titles, descriptions, and language/mode variants. Remote variants use `GameVariant.url` to open in system browser
- **GameAdapter**: RecyclerView adapter rendering game cards with equally-sized colored variant buttons
- **Offline-first**: Games run locally via `file://` URIs. Internet only needed for "Spiele aktualisieren" (menu) which fetches from `raw.githubusercontent.com/zdavatz/parados/main/`
- **Remote multiplayer**: Remote variants (democracy_remote, makalaina_remote, rainbow_blackjack_remote) open in the system browser at `game.ywesee.com/parados/` because PeerJS/WebRTC requires HTTPS and won't work in a `file://` WebView. Games with both local and remote variants (Democracy in Space, MAKA LAINA, Rainbow Blackjack) show separate buttons for each mode
- **CSV export**: Injected JS intercepts `<a download>` blob clicks and routes CSV data through a `JavascriptInterface` bridge (`AndroidCsvExport`) to share via Android's Share Sheet using `FileProvider`
- **CSV import**: `WebChromeClient.onShowFileChooser` delegates to `ActivityResultLauncher` so games can open a file picker to load CSV files

## Key Design Decisions

- Game HTML files live in `app/src/main/assets/games/` and get copied to `filesDir/games/` so they can be updated at runtime
- Assets are re-copied whenever `versionCode` in `build.gradle.kts` is bumped — so always increment `versionCode` when updating bundled HTML files
- Game catalog is hardcoded in `GameInfo.kt` (not parsed from index.html) — update this file when adding new games
- No third-party HTTP library — uses `HttpURLConnection` for GitHub downloads
- minSdk 24, targetSdk 35, package `com.ywesee.parados`
- Game HTML files must have `<meta name="viewport">` and use responsive sizing (CSS variables + JS `calcCellSize()`) — see `divided_loyalties.html` and `makalaina.html` for the pattern
- App icon uses the kangaroo image (kangy.jpg) from the iOS project, generated as PNG at all Android mipmap densities. Adaptive icon uses PNG foreground (`@mipmap/ic_launcher_foreground`) with a beige background (`#F5F0E8`)
- Google Play Store icon (512×512) is at `screenshots/play_store_icon_512x512.png`, generated from the xxxhdpi foreground on beige background
- CSV export uses `FileProvider` with cache directory (`csv_exports/`) — config in `res/xml/file_paths.xml` and declared in `AndroidManifest.xml`
