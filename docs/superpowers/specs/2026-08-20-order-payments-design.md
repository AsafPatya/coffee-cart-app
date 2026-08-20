# Order Payments (Rapyd) — Design

**Date:** 2026-08-20
**Status:** Draft — pending review

## Context

Today, "Place Order" on [`MyOrderScreen`](../../../composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/MyOrderScreen.kt) submits the basket straight to the server and the order appears immediately on the coffee cart's live dashboard ([`OrderDashboardScreen`](../../../composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/profile/OrderDashboardScreen.kt)) with no payment step at all.

This design adds real payment — card, Apple Pay, Google Pay — in front of that flow, using **Rapyd** as the payment processor. An order only becomes visible on the dashboard once payment is confirmed.

### Why Rapyd

Researched several Israeli-market options. Requirements were: card + Apple Pay + Google Pay, fully in-app (no external redirect), and — critically — **money must go directly to each coffee cart owner's own account**, not sit in a central account the platform holds.

- **Stripe** was ruled out: Israel is not a Stripe-supported country for opening an account (confirmed against Stripe's own country list). Workarounds require a fake US business presence for the platform *and* for every individual coffee cart owner — not viable.
- **PayPlus / Tranzila** support card + Apple Pay + Google Pay + Bit, but neither publicly documents true marketplace/sub-merchant split payouts. A real-world forum thread of another Israeli marketplace builder hitting the same wall (tried Rapyd, Meshulam, PayMe, Summit, BlueSnap, MangoPay — no confirmed answer) corroborates this gap.
- **Rapyd** is directly licensed for card acquiring in Israel, and its documented **Wallets** product gives self-service seller onboarding (a generated link the seller completes themselves — the platform never sees their KYC/bank details) plus a **Checkout Page** API that routes/splits a payment directly to a specific seller's wallet. This is the actual feature the others don't confirm.
- Bit is dropped from scope (by request) since it isn't confirmed for Rapyd in Israel; card + Apple Pay + Google Pay covers the requirement.

"No redirect" is satisfied practically, not literally: Rapyd's Checkout Page is a hosted page, opened inside an **embedded WebView** rather than the external browser. Tapping Apple Pay/Google Pay inside that page still triggers the real native OS payment sheet (that's how Apple Pay JS / Google Pay JS work even inside a WKWebView/Android WebView) — the user never leaves the app's own UI shell.

### Locked decisions

| Decision | Choice | Why |
|---|---|---|
| Processor | Rapyd | Only option found with confirmed Israel licensing + self-service seller sub-accounts + direct split payout |
| Payout model | Direct split via Rapyd wallet per cart | Explicitly requested — platform never holds/touches order funds |
| Payment methods | Card, Apple Pay, Google Pay | Bit dropped — not confirmed for Rapyd Israel; acceptable per request |
| "No redirect" | Embedded WebView hosting Rapyd's Checkout Page | True fully-native, no-webview checkout isn't offered by Rapyd (or any researched Israeli PSP); this is the closest fit — native payment sheets still trigger from inside it |
| Order visibility | Order is created with `paymentStatus = PENDING` and only flips to `PAID` (and appears on the dashboard) once Rapyd's webhook confirms payment | Matches "after it succeeds, start the flow of sending the order to the coffee cart" |
| Vendor KYC/bank details | Never touch the app/server — collected entirely on Rapyd's Hosted IDV Page | Avoids the platform holding sensitive PII or banking data |
| Request signing | Custom HMAC-SHA256 signer per Rapyd's documented algorithm | Required for every Rapyd API call and to verify incoming webhooks; no official Kotlin SDK exists |

---

## Data model changes

```kotlin
// shared/model/CoffeeCart.kt — add:
val paymentAccountId: String? = null   // Rapyd ewallet_ id once the owner completes onboarding
val paymentAccountVerified: Boolean = false

// shared/model/Order.kt — add:
enum class PaymentStatus { PENDING, PAID, FAILED }
val paymentStatus: PaymentStatus = PaymentStatus.PENDING
val checkoutUrl: String? = null   // set once a checkout is created; null after payment completes
```

`GET /carts/{id}/orders` (used by the dashboard) filters to `paymentStatus == PAID` — a pending/unpaid order simply doesn't exist yet from the dashboard's point of view.

---

## Server

### Rapyd request signing (new shared utility)

Every Rapyd API call and every incoming webhook needs this. One function, used both ways:

```kotlin
// server/.../rapyd/RapydSigner.kt
fun sign(method: String, urlPath: String, salt: String, timestamp: Long, body: String, accessKey: String, secretKey: String): String {
    val toSign = "${method.lowercase()}$urlPath$salt$timestamp$accessKey$secretKey$body"
    val hmac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")) }
    val hex = hmac.doFinal(toSign.toByteArray()).joinToString("") { "%02x".format(it) }
    return Base64.getEncoder().encodeToString(hex.toByteArray())
}
```

Applies to: `POST /v1/ewallets`, the Hosted IDV Page creation call, `POST /v1/checkout`, and webhook verification (same formula, minus the HTTP method component per Rapyd's webhook variant of the formula — confirmed against their docs at implementation time).

Config: `RAPYD_ACCESS_KEY`, `RAPYD_SECRET_KEY`, `RAPYD_BASE_URL` (sandbox vs live) as Railway env vars, following the same `local.properties`/env-var pattern already used for `MAPS_API_KEY`.

### New endpoints

- `POST /carts/{id}/payment-account` — creates a Rapyd wallet (`POST /v1/ewallets`) for that cart if it doesn't have one, then creates a Hosted IDV Page for it. Returns the onboarding URL. Stores the resulting `ewallet_` id on the cart as unverified.
- `POST /carts/{id}/orders/{orderId}/checkout` — creates a Rapyd Checkout Page (`POST /v1/checkout`) with `ewallet` set to that cart's wallet id, `amount`/`currency: "ILS"` from the order, and `payment_method_types_include` limited to card/Apple Pay/Google Pay IL codes. Stores the checkout url/id on the order, returns it to the client.
- `POST /webhooks/rapyd` — verifies the signature, then:
  - on a payment-completed event for a known order → sets `paymentStatus = PAID`, clears `checkoutUrl` (order now shows on the dashboard).
  - on a wallet-enabled event for a known cart → sets `paymentAccountVerified = true`.

---

## Client (KMP)

```kotlin
// composeApp/.../ui/payment/CheckoutHost.kt (commonMain)
@Composable
expect fun CheckoutWebView(url: String, onComplete: () -> Unit, onError: (String) -> Unit, modifier: Modifier)
```

- **Android actual**: `android.webkit.WebView` wrapped via `AndroidView`, with a `WebViewClient` watching for navigation to the configured `complete_payment_url`/`error_payment_url`.
- **iOS actual**: `WKWebView` via `UIKitView` interop, same URL-watching approach via `WKNavigationDelegate`.
- **wasmJs actual**: already running in a browser — either an `<iframe>` DOM overlay (same technique used for the earlier web map fallback) or a plain full-page redirect/return, since there's no "leaving the app" concern on web the way there is on mobile.

### Flow

1. `MyOrderScreen`'s "Place Order" now: creates the order (`PENDING`) → calls the new checkout endpoint → gets a URL → shows `CheckoutWebView` full-screen (same pattern as the existing map picker: a plain full-screen composable at the app root, **not** a `Dialog`/`ModalBottomSheet` — we already learned those don't route touch/payment-sheet gestures correctly on iOS).
2. On `onComplete`, the screen shows a "payment received" state (actual confirmation still comes from the webhook — this is just the UI's cue to stop showing the checkout and start polling/waiting).
3. Server-side webhook is the source of truth for `PAID`; client can poll `GET /carts/{id}/orders/{orderId}` briefly after `onComplete` to confirm before declaring success, in case the webhook lags slightly behind the WebView's redirect.

### Vendor onboarding UI

- `ProfileScreen`/`EditCartScreen` gets a "Connect Payment Account" action (same cart-picker pattern already used for Edit/Remove/Add Category/View Orders): calls the new payment-account endpoint, opens the returned Hosted IDV Page URL the same way (full-screen WebView), no special handling needed since it's a one-time setup flow, not a payment sheet.

---

## Testing

- `RapydSigner` — unit test against a known input/output pair (once we have real sandbox test vectors, or Rapyd's documented example if they provide one).
- Server webhook handler — unit test with a valid and an invalid signature, confirming only the valid one updates order/cart state.
- Manual, end-to-end in Rapyd **sandbox** only: connect a test cart's payment account, place a test order, pay with a sandbox card and (if testable in sandbox) Apple Pay/Google Pay, confirm the order appears on the dashboard only after payment, confirm a cancelled/failed payment does **not** create a visible order.

---

## Out of scope

- Bit (dropped per request).
- Refunds/disputes handling.
- Displaying payout history/balance to cart owners.
- Push notifications on order status change (explicitly phase 2, tracked separately).
- Automatic retry/reconciliation if a webhook is missed — for now, a stuck `PENDING` order needs manual investigation via the Rapyd dashboard.

---

## Open items before implementation starts

- Exact IL payment-method type codes for `payment_method_types_include` (e.g. `il_visa_card`, `il_apple_pay` — naming to confirm against the sandbox once real credentials are in hand).
- Exact webhook signature formula variant (whether it includes the HTTP method component like the request-signing formula, or omits it as some Rapyd docs excerpts suggested) — to confirm empirically against a real sandbox webhook payload.
- Real sandbox `access_key`/`secret_key` from the account already created.
