# CoffeeCartList Screen — Design

**Date:** 2026-08-15
**Status:** Approved

## Context

The customer-facing "browse coffee carts" list screen currently exists only as a placeholder — [`composeApp/.../screens/CoffeeCartListScreen.kt`](../../../composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListScreen.kt) renders a `LazyColumn` of hardcoded cart names in a `Card`, with no ViewModel and no data layer.

This design replaces it with a real screen backed by a `ViewModel`, following the module layout locked in [`2026-08-13-coffee-cart-architecture-design.md`](2026-08-13-coffee-cart-architecture-design.md) (`:shared` holds `model` / `domain` / `data` / `feature` / `di`; `composeApp` holds platform-facing Compose UI). There is no backend yet, so the data layer is a fake/in-memory repository for now; owner-side cart management (adding/editing carts) is explicitly out of scope.

Each list item shows: **name**, **isOpen** status, **address**, and an **image** — name/isOpen/address in a column on the left, image pinned to the end of the row.

### Locked decisions

| Decision | Choice | Why |
|---|---|---|
| Data source | Fake in-memory repository (`FakeCoffeeCartRepository`) | No backend exists yet; ship the UI now, swap the repo binding later |
| Repository seam | `CoffeeCartRepository` interface in `:shared/domain` | Swapping fake → real Ktor implementation later changes one Koin binding, nothing else |
| DI | Koin | Already a declared dependency, pure Kotlin, first-class KMP + Compose support (`koinViewModel()`). Hilt is Android-only and cannot run in `commonMain`. Koin is not yet initialized anywhere in the app — this work bootstraps it |
| Image loading | Coil3 (`coil3.compose.AsyncImage`) | Standard image library for Compose Multiplatform; no image library exists in the project yet — new dependency |
| `isOpen` indicator | Small colored dot (green/red) next to the address | User preference |
| State shape | Sealed `CoffeeCartListUiState` (`Loading` / `Success` / `Error`) exposed via `StateFlow` | Matches MVVM/unidirectional-data-flow pattern from the architecture doc |
| Owner features | Out of scope | Explicitly deferred by request |

---

## Data model

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/model/CoffeeCart.kt
data class CoffeeCart(
    val id: String,
    val name: String,
    val isOpen: Boolean,
    val address: String,
    val imageUrl: String,
)
```

Plain domain model — no serialization annotations. There's no real network response to map yet; a `@Serializable` DTO + mapper is added in `:shared/contract` when the real backend/API shape exists.

---

## Repository (fake)

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/domain/CoffeeCartRepository.kt
interface CoffeeCartRepository {
    suspend fun getCoffeeCarts(): List<CoffeeCart>
}
```

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepository.kt
class FakeCoffeeCartRepository : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> {
        delay(500) // simulate network latency
        return listOf(
            CoffeeCart(id = "1", name = "Downtown Espresso Cart", isOpen = true, address = "123 Main St", imageUrl = "https://picsum.photos/seed/1/200"),
            CoffeeCart(id = "2", name = "Riverside Brew", isOpen = false, address = "45 River Rd", imageUrl = "https://picsum.photos/seed/2/200"),
            CoffeeCart(id = "3", name = "Central Park Coffee", isOpen = true, address = "9 Park Ave", imageUrl = "https://picsum.photos/seed/3/200"),
        )
    }
}
```

The interface is the seam: a future `KtorCoffeeCartRepository` implements the same interface against the real `GET /carts` endpoint, and only the Koin binding changes — `CoffeeCartListViewModel` and the UI are untouched.

---

## ViewModel

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModel.kt
sealed interface CoffeeCartListUiState {
    data object Loading : CoffeeCartListUiState
    data class Success(val carts: List<CoffeeCart>) : CoffeeCartListUiState
    data class Error(val message: String) : CoffeeCartListUiState
}

class CoffeeCartListViewModel(
    private val repository: CoffeeCartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CoffeeCartListUiState>(CoffeeCartListUiState.Loading)
    val uiState: StateFlow<CoffeeCartListUiState> = _uiState.asStateFlow()

    init { loadCarts() }

    fun loadCarts() {
        viewModelScope.launch {
            _uiState.value = CoffeeCartListUiState.Loading
            _uiState.value = try {
                CoffeeCartListUiState.Success(repository.getCoffeeCarts())
            } catch (e: Exception) {
                CoffeeCartListUiState.Error(e.message ?: "Failed to load coffee carts")
            }
        }
    }
}
```

Standard `androidx.lifecycle.ViewModel` + `viewModelScope`, sealed `UiState` for loading/success/error, matching the architecture doc's "one ViewModel per screen, one immutable `UiState` via `StateFlow`" rule.

---

## UI

Row layout: `Row` containing a `Column` (name, dot + address) that takes remaining width, with the image pinned at the end.

```kotlin
// composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListItem.kt
@Composable
fun CoffeeCartListItem(cart: CoffeeCart, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.Large.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cart.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.XXSmall.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (cart.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape,
                            ),
                    )
                    Spacer(Modifier.width(Spacing.XXSmall.dp))
                    Text(cart.address, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.width(Spacing.Small.dp))
            AsyncImage(
                model = cart.imageUrl,
                contentDescription = cart.name,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListScreen.kt
@Composable
fun CoffeeCartListScreen(
    onCartClick: (String) -> Unit,
    viewModel: CoffeeCartListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CoffeeCartListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is CoffeeCartListUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message) }
        is CoffeeCartListUiState.Success -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(Spacing.Large.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
        ) {
            items(state.carts, key = { it.id }) { cart ->
                CoffeeCartListItem(cart = cart, onClick = { onCartClick(cart.id) })
            }
        }
    }
}
```

Note: `onCartClick` changes from passing the cart **name** (current placeholder behavior) to passing the cart **id** — the nav route currently discards the argument anyway, so this is a safe cleanup while touching this code.

`Spacing.XXSmall`, `Spacing.Small`, and `Spacing.Large` all already exist in [`theme/Spacing.kt`](../../../composeApp/src/commonMain/kotlin/com/coffeecart/app/theme/Spacing.kt); the `8.dp` dot size and `64.dp` image size are element sizing, not spacing, so they stay as literals per that file's intent.

---

## DI (Koin)

Koin is declared as a dependency (`koin-core`, `koin-compose`, `koin-compose-viewmodel`) but not yet initialized anywhere in the app. This work bootstraps it.

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/di/CoffeeCartModule.kt
val coffeeCartModule = module {
    single<CoffeeCartRepository> { FakeCoffeeCartRepository() }
    viewModel { CoffeeCartListViewModel(get()) }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/com/coffeecart/app/App.kt
@Composable
fun App() {
    KoinApplication(application = { modules(coffeeCartModule) }) {
        MaterialTheme { /* existing nav host */ }
    }
}
```

Later, swapping to the real backend is changing the `single<CoffeeCartRepository>` binding to a Ktor-backed implementation — nothing else in this design changes.

---

## Testing

- `CoffeeCartListViewModel` — unit test in `shared/commonTest` using `FakeCoffeeCartRepository` (and a second fake that throws) verifying `uiState` transitions `Loading → Success` / `Loading → Error`.
- `CoffeeCartListItem` — Compose UI test verifying name/address/dot render, and correct dot color for `isOpen = true` vs `false`.
- No dedicated repository test needed yet — `FakeCoffeeCartRepository` has no logic beyond returning static data.

---

## Out of scope

Owner-side cart creation/editing, real backend/Ktor integration, authentication, pagination, search/filtering, pull-to-refresh. The repository interface seam means adding the real backend later requires no changes to the ViewModel or UI.

---

## Verification

- `./gradlew build` — `:shared` and `:composeApp` compile, unit tests pass
- Manual: launch the app on at least one target (Android emulator or the desktop/browser target used for dev), confirm the cart list loads after the simulated delay, shows the correct dot color per cart, and tapping a row navigates via `onCartClick(cart.id)`
