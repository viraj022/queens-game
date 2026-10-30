# Queens

LinkedIn-style **Queens** puzzle for Android — Kotlin 2.x, Jetpack Compose, Material 3.

**Package:** `com.viraj022.queens`  
**Unlimited New Game** (no daily lock). Puzzles are generated on-device with backtracking.

## Rules

- Place exactly one queen in every **row**, **column**, and **color region**
- Queens may not touch — including diagonally (8-neighbor adjacency)
- Tap cycle: empty → X → queen

## Stack

| Item | Version |
|------|---------|
| Kotlin | 2.4.21 |
| AGP | 8.13.2 |
| Compose BOM | 2026.06.01 (pinned; 2026.09.00 needs AGP 9.1 + SDK 37) |
| minSdk / targetSdk | 26 / 36 |
| Architecture | Single-module MVVM (`QueensViewModel` + immutable `GameUiState`) |

## Build

```bash
./gradlew test
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Features (MVP)

- Pastel region palette, live conflict highlighting, solved banner
- Vector Canvas crown (no emoji)
- New Game / Reset / Undo, elapsed timer, Auto-X toggle, haptics
- Sample 6×6 seed + generator for sizes 6–8
