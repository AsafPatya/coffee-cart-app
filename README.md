# ☕ Coffee Cart

A multi-vendor coffee ordering app: browse a directory of coffee carts, open one, build a basket
from its menu, and place an order. Built with **Kotlin Multiplatform** and **Compose
Multiplatform**, targeting Android, iOS, and Web (Wasm), with a Ktor backend in the same repo.

## App modes

The same app presents a different shell depending on the signed-in account's role.

| Mode | Who | Status |
|---|---|---|
| Customer | End users browsing and ordering | Phase 1 — in progress |
| Kitchen | Shop device receiving live orders | Phase 2 |
| Owner | Shop owners editing their menus | Phase 3 |

## Modules

| Module | What it is |
|---|---|
| `shared` | KMP library — domain model, API contract, data layer, ViewModels |
| `composeApp` | All Compose UI, shared across every platform |
| `androidApp` | Thin Android launcher (`MainActivity`) |
| `iosApp` | Xcode project wrapping the iOS framework |
| `server` | Ktor backend (JVM) |

`shared` is compiled into both the clients **and** the server, so the API contract cannot drift
between them — a breaking change fails the build rather than failing in production.

## Tech stack

Kotlin 2.4 · Compose Multiplatform 1.11 · Ktor 3.5 · Koin · SQLDelight · Exposed + PostgreSQL ·
AGP 9 · Gradle 9.7

## Getting started

### Prerequisites

- **JDK 21.** Android Studio bundles one; point `JAVA_HOME` at it:
  ```bash
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ```
- **Android SDK** with platform 36 (path goes in `local.properties` as `sdk.dir`).
- **Xcode**, selected via `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`, plus
  an iOS Simulator runtime (`xcodebuild -downloadPlatform iOS`) — required only for the iOS
  target. Not needed for Android, Web, or the server.

### Run the Android app

```bash
./gradlew :androidApp:installDebug
```

### Run the iOS app

Open `iosApp/iosApp.xcodeproj` in Xcode and run, or from the command line:

```bash
cd iosApp && xcodebuild -project iosApp.xcodeproj -target iosApp -sdk iphonesimulator -configuration Debug build
xcrun simctl install booted build/Debug-iphonesimulator/iosApp.app
xcrun simctl launch booted com.coffeecart.app
```

The build's Run Script phase calls `:composeApp:embedAndSignAppleFrameworkForXcode` automatically,
so the Kotlin framework is always rebuilt from source — no separate Gradle step needed first.

### Run the web app

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Open `http://localhost:8080`. To preview it at phone size, use your browser's device toolbar
(Chrome: `Cmd+Shift+M`) rather than an Android emulator — Compose Multiplatform's Wasm canvas
relies on WebGL, which Android emulators' virtual GPUs frequently fail to render correctly.

### Run the server

```bash
./gradlew :server:run
```

Then check it responds:

```bash
curl http://localhost:8080/health
```

### Build and test everything

```bash
./gradlew build
```

## Current status

Scaffolding is complete and verified on all four targets: the app builds and launches on
**Android**, **iOS**, and **Web (Wasm)**, the server responds on `/health`, and tests pass on JVM
and Wasm.

Not yet done: domain model, API endpoints, database, auth, and all real screens — see the design
doc below.

## Design

Full architecture and the phased implementation plan:
[`docs/superpowers/specs/2026-08-13-coffee-cart-architecture-design.md`](docs/superpowers/specs/2026-08-13-coffee-cart-architecture-design.md)
