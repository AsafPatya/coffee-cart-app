# Coffee Cart — Architecture Design

**Date:** 2026-08-13
**Status:** Approved

## Context

This repo was empty at the time of writing (README + `.idea` only). The original README described a single-shop POS/KDS terminal application — that is **not** the product being built. The actual product:

> A multi-vendor consumer app: a directory of coffee carts. Each cart has an info page and an "Order Now" flow (menu → basket → submit). The order goes to a backend, which delivers it to a device at the shop.

The app has **app modes** — the same binary presents a different shell depending on the signed-in account's role:

| Mode | Who | Phase |
|---|---|---|
| Customer | End users browsing and ordering | **Phase 1** |
| Kitchen | Shop device receiving live orders | Phase 2 |
| Owner | Shop owners editing their menus | Phase 3 |

**Phase 1 goal:** a customer can sign in with Google or Apple, browse coffee carts, open one, build a basket from its menu, and submit an order that lands in the backend.

### Locked decisions

| Decision | Choice | Why |
|---|---|---|
| Platforms | Android, iOS, Web (Wasm) | Compose Multiplatform across all three |
| Backend | Ktor module in this repo | Shares DTOs with clients — contract cannot drift |
| Database | PostgreSQL + Exposed + Flyway | Relational data, migrations from commit one |
| Auth | Google + Apple sign-in | No SMS cost; Apple is required by App Store if any social login is offered |
| Real-time | Polling now, WebSocket later | Hidden behind a repository interface; swap costs one class |
| First build | Customer mode only | Smallest path to a working product |

### Known environment constraints

- **Xcode is installed and selected.** `iosApp/` is a hand-authored Xcode project (no XcodeGen/Tuist available) whose Run Script phase calls `:composeApp:embedAndSignAppleFrameworkForXcode`. Its `Info.plist` must set `CADisableMinimumFrameDurationOnPhone` to `true` — Compose Multiplatform's `ComposeUIViewController` throws at launch without it.
- JDK 21 is available via Android Studio's bundled runtime; `JAVA_HOME` must point at it.
- Apple sign-in requires a paid Apple Developer account with a configured Service ID and key. Start that paperwork early — it gates all Apple auth testing.

---

## Repo layout

A single Gradle multi-project build. The critical property: **`:shared` is consumed by both the clients and the server**, so an API contract cannot drift between the two — a breaking change fails compilation rather than failing in production.

```
CoffeCartSolution/
├── settings.gradle.kts
├── gradle/libs.versions.toml        # version catalog — single source of dependency truth
├── build-logic/                     # convention plugins (shared Kotlin/Compose config)
├── shared/                          # KMP library: model, contract, data, feature logic
├── composeApp/                      # Compose Multiplatform UI + platform entry points
│   ├── commonMain/ androidMain/ iosMain/ wasmJsMain/
├── iosApp/                          # Xcode project wrapping the iOS framework
└── server/                          # JVM-only Ktor backend
```

### `:shared` internal structure

Each package has one job and a defined boundary:

- **`model`** — pure domain types. No serialization annotations, no framework types, no dependencies. The vocabulary everything else speaks.
- **`contract`** — the wire format. `@Serializable` DTOs, route constants, error envelope, and `Dto ↔ model` mappers. **Compiled into the server too** — this is the anti-drift mechanism.
- **`data`** — `remote` (Ktor client), `local` (SQLDelight, token storage), `repository` (implementations).
- **`domain`** — repository *interfaces* (`CartRepository`, `MenuRepository`, `OrderRepository`, `AuthRepository`) plus logic that isn't a passthrough (basket totals, validation). UI depends on these interfaces, never on `data`.
- **`feature`** — one ViewModel per screen, exposing a single immutable `UiState` via `StateFlow`. Platform-agnostic.
- **`di`** — Koin modules; `expect fun platformModule()` supplies per-platform pieces.

Dependency direction is strictly one-way: `feature → domain ← data`, `model` at the bottom, nothing depends on `feature`.

---

## Data model

```kotlin
data class Money(val minorUnits: Long, val currency: String)   // never Double for money

data class CoffeeCart(
    val id: CartId, val name: String, val description: String,
    val imageUrl: String?, val location: Location,
    val openingHours: List<OpeningWindow>, val isAcceptingOrders: Boolean,
)

data class MenuCategory(val id: CategoryId, val name: String, val sortOrder: Int)

data class MenuItem(
    val id: MenuItemId, val cartId: CartId, val categoryId: CategoryId,
    val name: String, val description: String, val imageUrl: String?,
    val basePrice: Money, val isAvailable: Boolean,
    val optionGroups: List<OptionGroup>,           // "Size", "Milk", "Extras"
)

data class OptionGroup(
    val id: OptionGroupId, val name: String,
    val minSelections: Int, val maxSelections: Int,  // 1..1 = radio, 0..N = checkboxes
    val options: List<Option>,
)
data class Option(val id: OptionId, val name: String, val priceDelta: Money)

data class OrderLine(
    val menuItemId: MenuItemId, val quantity: Int,
    val selectedOptionIds: List<OptionId>, val note: String?,
    val lineTotal: Money,                            // server-authoritative
)

data class Order(
    val id: OrderId, val cartId: CartId, val userId: UserId,
    val lines: List<OrderLine>, val total: Money,
    val status: OrderStatus, val placedAt: Instant, val pickupCode: String,
)

enum class OrderStatus { PLACED, ACCEPTED, IN_PREPARATION, READY, COLLECTED, CANCELLED }
```

### Design notes

- **Money is `Long` minor units**, never `Double`. Floating-point money produces rounding errors that become real money discrepancies.
- **The option-group model is the load-bearing decision.** Coffee ordering is overwhelmingly about modifiers (size, milk, shots, syrup). Modeling them generically from day one makes "add oat milk" a data change, not a code change. Getting this wrong forces a schema migration once real orders exist.
- **Prices are server-authoritative.** The client computes a total for display; the server recomputes from its own menu data on submit and rejects mismatches. A client-trusted total is a way to buy free coffee.
- **`OrderStatus` includes states Phase 1 never sets.** The customer app only ever sees `PLACED`; kitchen mode drives the rest. Defining the full lifecycle now makes Phase 2 an addition rather than a migration.
- **`pickupCode`** — a short human-readable code so the barista can call the order out.

---

## API contract (Phase 1)

REST + JSON over Ktor, all under `/api/v1`.

```
POST /auth/social       { provider, idToken }    -> { accessToken, refreshToken, user }
POST /auth/refresh      { refreshToken }         -> { accessToken, refreshToken }

GET  /carts             ?lat&lng&q               -> [CartSummaryDto]
GET  /carts/{id}                                 -> CartDetailDto
GET  /carts/{id}/menu                            -> MenuDto

POST /orders            CreateOrderRequest       -> OrderDto     [auth]
GET  /orders/{id}                                -> OrderDto     [auth]
GET  /orders            ?status                  -> [OrderDto]   [auth]
```

Every error returns the same envelope — `{ code, message, details? }` with a stable machine-readable `code` — so client error handling is one mapper rather than per-endpoint special cases.

`GET /orders/{id}` is what the customer polls to watch order status. **The polling detail is confined to the repository implementation**: the interface is `OrderRepository.observeOrder(id): Flow<Order>`, so swapping polling for a WebSocket later changes one class and touches no UI or ViewModel code. That is the entire reason to start with polling.

---

## Server

Ktor on Netty, layered `routes → service → repository → database`.

**Auth.** Social sign-in exchanged for our own JWTs: short-lived access token, long-lived refresh token, refresh rotation.

Token verification is the security-critical part. `POST /auth/social` receives a provider ID token and must verify it properly — fetch the provider's JWKS (cached, honoring key rotation), validate signature, `iss`, `aud` (our client IDs), and `exp`. Only then trust the `sub` claim as the provider's stable user identifier. **Never trust client-supplied user data**: email and name come from the verified token's claims, not the request body, or anyone can sign in as anyone.

`SocialTokenVerifier` is an interface with `GoogleTokenVerifier` and `AppleTokenVerifier` implementations, so a third provider is a new class rather than a change to the auth route.

Users are keyed by `(provider, providerUserId)`. **Account linking** — someone who signs in with Google and later with Apple using the same verified email is the same person — must be handled deliberately rather than silently creating a duplicate account.

Two Apple specifics that bite: Apple returns the user's name **only on the very first authorization** and never again, so persist it immediately; and Apple private-relay addresses (`@privaterelay.appleid.com`) are real deliverable addresses but must not be treated as normal emails for matching.

**Order submission** is the one genuinely tricky endpoint: validate the cart is accepting orders, re-resolve every menu item and option from the database, verify availability, recompute the total server-side, reject on mismatch, then persist — all in one transaction.

**Local dev:** `docker-compose.yml` with Postgres so the stack starts with one command.

---

## Client architecture

**MVVM with unidirectional data flow.** Each screen has a `ViewModel` exposing one immutable `UiState` through `StateFlow`, and a `Composable` that renders it and emits events. No business logic in composables — that keeps logic testable without a UI harness and identical across all three platforms.

Navigation uses a `Screen` sealed hierarchy. The root composable selects a shell based on the session's role. Phase 1 has only `CustomerShell`, but the branch point exists so kitchen and owner modes are additions rather than restructuring.

**Phase 1 screens:** Splash/session-check → Sign-in → Cart list → Cart detail → Menu → Item customization → Basket → Order confirmation → Order status.

**Sign-in is one `expect`/`actual` seam.** `expect suspend fun signIn(provider): SocialIdToken` is implemented per platform; everything above it — ViewModel, token exchange, session storage — is shared code that neither knows nor cares which platform it runs on.

**Basket is client-local state**, SQLDelight-persisted so it survives app restart. No server round-trip while the user picks items.

**Offline:** SQLDelight caches carts and menus. List and detail screens read cache first, then refresh from network — the app opens instantly and degrades gracefully with no connection. Ordering itself requires connectivity.

### Platform-specific concerns

**Wasm is the constraint that shapes library choice.** Every dependency must be verified for `wasmJs` support *before* adoption, at setup time rather than assumed.

**Secure token storage** is `expect`/`actual`: Android → EncryptedSharedPreferences; iOS → Keychain; Wasm → `localStorage`, with the honest caveat that browser storage offers no real protection against XSS.

**Social sign-in is the least KMP-friendly part of the build.** Android: Credential Manager with Google ID, Apple via its web flow in a Custom Tab. iOS: `AuthenticationServices` for Apple (native, required by App Store review) and the Google SDK. Wasm: Google Identity Services and Apple JS via JS interop. Each platform needs its own OAuth client ID registered with the provider.

---

## Testing

**`commonTest` in `:shared`** carries most of the value: basket total calculation with option modifiers, DTO↔model mapping round-trips, ViewModel state transitions against fake repositories. Platform-independent and fast.

**Server:** Ktor `testApplication` integration tests per endpoint against a test database. Two endpoints get adversarial cases specifically — order submission (tampered client total, unavailable item, closed cart, invalid option combination) and social auth (forged signature, expired token, wrong `aud`, wrong `iss`), tested against a fake JWKS so the suite needs no network.

**Contract safety** comes primarily from `:shared` compiling into both sides — a changed DTO breaks the build, a stronger guarantee than any test.

---

## Implementation order

Each step ends somewhere runnable and verifiable.

1. **Scaffold** — Gradle multi-project, version catalog, convention plugins; all modules build, empty app launches. *Verify: Android, iOS, and Wasm each open a blank screen.* ✅ Done.
2. **Domain + contract** — `model` and `contract` packages, mappers, unit tests for mapping and money arithmetic.
3. **Server skeleton** — Ktor + Postgres via docker-compose, Flyway schema, health endpoint. *Verify: `docker-compose up`, server responds.*
4. **Catalog endpoints** — carts and menu endpoints over seeded data. *Verify: curl returns real JSON.*
5. **Catalog on the client** — Ktor client, SQLDelight cache, repositories, cart list/detail/menu screens. *Verify: seeded carts appear on each platform.*
6. **Auth** — `/auth/social` with real token verification, JWT issuance, secure storage, the `signIn` actuals, sign-in screen, session restore. Android + Google first, then Apple and remaining platforms. *Verify: sign in end to end; forged or expired tokens are rejected.*
7. **Basket** — item customization with option groups, local persistence, total calculation. *Verify: basket survives restart; totals correct with modifiers.*
8. **Order submission** — `POST /orders` with server-side recomputation, confirmation screen, status polling. *Verify: order in Postgres with correct total; tampered total rejected.*
9. **README rewrite** to describe the actual product.

---

## Phase 1 exclusions (deliberate)

Payments, kitchen mode, owner menu editing, push notifications, delivery, loyalty, ratings, extended order history, multi-language. The data model accommodates these; Phase 1 does not build them.

---

## Verification

- `./gradlew build` — all modules compile, all tests pass
- `./gradlew :server:test` — endpoint integration tests including adversarial auth and order cases
- `docker-compose up`, then the server; app runs on Android emulator, iOS simulator, and browser
- End-to-end by hand: sign in → browse → customize an item → basket → submit → confirmation, then confirm the row in Postgres with a server-computed total
