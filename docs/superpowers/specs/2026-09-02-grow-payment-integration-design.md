# Grow Payment Integration (via Make.com)

## Goal

Add Grow as a payment provider, used exclusively going forward for checkout (Rapyd stays in the codebase, unused). Payment links are generated through a Make.com scenario per cart, not by calling Grow's REST API directly (cost).

## Architecture

Per cart, a one-time manual setup in Make.com produces a webhook URL, stored on the cart as `growWebhookUrl`. Checkout always uses that URL — no Rapyd fallback.

```
App (MyOrderViewModel.placeOrder)
  -> Server: POST orderCheckout(cartId, orderId)
       -> looks up cart.growWebhookUrl
       -> GrowClient.createPaymentLink(webhookUrl, orderId, amount, completeUrl)
            -> POST {orderId, amount, completeUrl} to Make webhook
            -> Make scenario: Webhook trigger -> Grow "Create Payment Link" -> Webhook response
            -> returns {"url": "..."}
       -> CheckoutResponse(url = ...)
  -> App opens url in WebView (unchanged)
```

## One-time setup per cart (manual, in Make.com)

1. Connect that cart owner's Grow account in Make (business ID + phone OTP) — reusable per Grow account.
2. Build a scenario:
   - **Webhook trigger** (custom webhook, generates a unique URL)
   - **Grow: Create Payment Link**, mapped from the webhook's incoming fields:
     - `title`: `"Coffee Cart Order {{orderId}}"`
     - `products.data[0]`: `name: "Coffee Cart Order"`, `price: {{amount}}`, `vatType: 1`, `quantity: 1`
     - `sendingMode: 3` (None — the app shows the link itself, Grow must not also SMS/email it)
     - `paymentTypes`: `type: payments`, `maxOrCustom: custom`, `paymentsPaymentNum: 1`
     - `paymentLinkType: 2` (single payment)
     - `successUrl`: `{{completeUrl}}`
     - `pageFieldSettings`: `fullName: "Guest Name"` (placeholder — must be 2+ words, Grow validates), `phone: "0500000000"` (placeholder), `invoiceName: 0`, `invoiceLicenseNumber: 0`
     - **No `messageText`** field — Grow rejects it when `sendingMode` is None.
   - **Webhook response**: `status: 200`, `body: {"url": "{{grow_module.data.url}}"}`
3. Copy the scenario's webhook URL into that cart's `growWebhookUrl`.

Verified end-to-end against the real Grow Sandbox (userId `10633755`, pageCode `c34d1f4a546f`):
```
POST <webhook url>
Body: {"orderId": "test-order-3", "amount": 12.5, "completeUrl": "https://example.com/payments/complete"}
-> 200 {"url": "https://sandbox.grow.link/1f959c71dae70765b7a3f352c5499be2-NjczOTk"}
```

## Data model changes

- `CoffeeCart` / `CoffeeCartDto` (shared contract module): add `growWebhookUrl: String? = null`.
- DB migration: add nullable column `grow_webhook_url TEXT` to the carts table.
- `PostgresCartStore`: read/write the new column.

## Server changes

1. **New file** `server/src/main/kotlin/com/coffeecart/server/grow/GrowClient.kt`:
   ```kotlin
   class GrowClient(private val client: HttpClient) {
       suspend fun createPaymentLink(
           webhookUrl: String,
           orderId: String,
           amount: Double,
           completeUrl: String,
       ): String {
           // POST JSON {orderId, amount, completeUrl} to webhookUrl
           // parse response body for "url"; non-200 or missing "url" -> error()
       }
   }
   ```
2. **`Application.kt`**, `Endpoints.orderCheckout` route:
   - Remove the `rapydClient.createCheckout(...)` call from this route.
   - Look up the cart; if `cart.growWebhookUrl == null`, respond with an error (e.g. 422 + message "This cart has no payment method configured").
   - Otherwise call `growClient.createPaymentLink(cart.growWebhookUrl, order.id, amount, completeUrl)` and return `CheckoutResponse(url = ...)`, same as today.
   - `completeUrl`/`errorUrl` construction stays as today (`$publicBaseUrl/payments/complete`, `.../error`); `errorUrl` is currently unused by `GrowClient` (Grow's module only takes a `successUrl` — no error URL field surfaced yet).
3. Leave `RapydClient.kt`, the `paymentAccount` route, and `RAPYD_WEBHOOK` route untouched but disconnected from checkout.

## Client changes

None. `MyOrderViewModel.placeOrder()` and the WebView checkout flow are provider-agnostic already — they just take a URL.

## Error handling

- Missing `growWebhookUrl` on the cart → checkout fails with a clear message, surfaced via the existing `_snackBarMessages` flow in `MyOrderViewModel` (same catch block as today's "Failed to start payment.").
- Make webhook non-200 or malformed response → `GrowClient` throws, caught by the same `try/catch` in the `orderCheckout` route that already wraps Rapyd errors today, logged and returned as 500 with the error message.

## Out of scope (deferred)

- Payment confirmation via webhook (marking orders `PAID` reliably, like `RAPYD_WEBHOOK` does for Rapyd) is not covered by this pass. For now, the app's existing `onCheckoutComplete`/`onCheckoutError` client-side callbacks (driven by the WebView's redirect to `completeUrl`/`errorUrl`) are the only signal — same trust model as blindly trusting the redirect, no server-side verification yet.
- Collecting real customer name/phone at checkout (currently placeholder "Guest Name" / fake phone) is deferred.
- An admin/UI-facing way to set `growWebhookUrl` per cart is deferred — for now it's set directly (e.g. via DB or an ad hoc update).
