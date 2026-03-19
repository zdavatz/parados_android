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

# Release build
ANDROID_HOME=~/Android/Sdk ./gradlew assembleRelease

# Clean
ANDROID_HOME=~/Android/Sdk ./gradlew clean
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Architecture

- **Two Activities**: `MainActivity` (game list with RecyclerView) → `GameActivity` (full-screen WebView with floating back button)
- **GameRepository**: Copies bundled HTML from `assets/games/` to internal storage; re-copies when `versionCode` changes; handles GitHub update downloads
- **GameInfo**: Hardcoded game catalog with titles, descriptions, and language/mode variants
- **GameAdapter**: RecyclerView adapter rendering game cards with equally-sized colored variant buttons
- **Offline-first**: Games run locally via `file://` URIs. Internet only needed for "Spiele aktualisieren" (menu) which fetches from `raw.githubusercontent.com/zdavatz/parados/main/`
- **Remote multiplayer games** (democracy_remote, makalaina_remote, rainbow_blackjack_remote) need internet during gameplay

## Key Design Decisions

- Game HTML files live in `app/src/main/assets/games/` and get copied to `filesDir/games/` so they can be updated at runtime
- Assets are re-copied whenever `versionCode` in `build.gradle.kts` is bumped — so always increment `versionCode` when updating bundled HTML files
- Game catalog is hardcoded in `GameInfo.kt` (not parsed from index.html) — update this file when adding new games
- No third-party HTTP library — uses `HttpURLConnection` for GitHub downloads
- minSdk 24, targetSdk 34, package `com.ywesee.parados`
- Game HTML files must have `<meta name="viewport">` and use responsive sizing (CSS variables + JS `calcCellSize()`) — see `divided_loyalties.html` and `makalaina.html` for the pattern
