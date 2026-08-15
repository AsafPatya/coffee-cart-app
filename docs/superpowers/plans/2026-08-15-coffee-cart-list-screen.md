# CoffeeCartList Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the placeholder `CoffeeCartListScreen` with a real screen backed by a `CoffeeCartListViewModel`, showing cart name/isOpen/address/image, sourced from a fake in-memory repository behind a swappable interface.

**Architecture:** Domain model, repository interface, fake repository implementation, ViewModel, and Koin module live in `:shared` (per the locked module layout). The Compose UI (list screen + row item) lives in `:composeApp`. Koin is bootstrapped for the first time as part of this work.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin 4.2.2 DI, Coil3 for image loading, kotlinx-coroutines, JetBrains `androidx.lifecycle` KMP ViewModel.

## Global Constraints

- Domain model has no serialization annotations — no real DTO/API shape exists yet (per spec).
- `CoffeeCartRepository` is the seam for a future real backend — ViewModel and UI must depend only on the interface, never on `FakeCoffeeCartRepository` directly.
- `isOpen` renders as a small colored dot (green `0xFF4CAF50` / red `0xFFF44336`), not text.
- Use `Spacing` enum values from `composeApp/.../theme/Spacing.kt` for all padding/gaps; raw `.dp` literals are only for element sizing (dot size, image size, corner radius).
- `onCartClick` passes the cart **id**, not the cart name.
- Owner-side cart management is out of scope — do not build it.

---

## File structure

- Create `shared/src/commonMain/kotlin/com/coffeecart/shared/model/CoffeeCart.kt` — domain model.
- Create `shared/src/commonMain/kotlin/com/coffeecart/shared/domain/CoffeeCartRepository.kt` — repository interface.
- Create `shared/src/commonMain/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepository.kt` — fake implementation.
- Create `shared/src/commonMain/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModel.kt` — `CoffeeCartListUiState` + `CoffeeCartListViewModel`.
- Create `shared/src/commonMain/kotlin/com/coffeecart/shared/di/CoffeeCartModule.kt` — Koin module.
- Create `shared/src/commonTest/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModelTest.kt` — ViewModel unit tests.
- Modify `shared/build.gradle.kts` — add `lifecycle-viewmodel` dependency to `commonMain`.
- Modify `gradle/libs.versions.toml` — add Coil3 version + library entries.
- Modify `composeApp/build.gradle.kts` — add Coil3 dependencies.
- Modify `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListScreen.kt` — replace placeholder with real screen wired to the ViewModel.
- Create `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListItem.kt` — row composable.
- Modify `composeApp/src/commonMain/kotlin/com/coffeecart/app/App.kt` — wrap root in `KoinApplication`.
- Modify `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/MainScreen.kt:47` — pass `cartId` through to `Destination.Orders.route` navigation instead of discarding it.

---

### Task 1: Domain model

**Files:**
- Create: `shared/src/commonMain/kotlin/com/coffeecart/shared/model/CoffeeCart.kt`
- Test: `shared/src/commonTest/kotlin/com/coffeecart/shared/model/CoffeeCartTest.kt`

**Interfaces:**
- Produces: `data class CoffeeCart(val id: String, val name: String, val isOpen: Boolean, val address: String, val imageUrl: String)`

- [ ] **Step 1: Write the failing test**

```kotlin
// shared/src/commonTest/kotlin/com/coffeecart/shared/model/CoffeeCartTest.kt
package com.coffeecart.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CoffeeCartTest {
    @Test
    fun `holds all fields as provided`() {
        val cart = CoffeeCart(
            id = "1",
            name = "Downtown Espresso Cart",
            isOpen = false,
            address = "123 Main St",
            imageUrl = "https://example.com/cart.png",
        )

        assertEquals("1", cart.id)
        assertEquals("Downtown Espresso Cart", cart.name)
        assertFalse(cart.isOpen)
        assertEquals("123 Main St", cart.address)
        assertEquals("https://example.com/cart.png", cart.imageUrl)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "com.coffeecart.shared.model.CoffeeCartTest"`
Expected: FAIL — compilation error, `CoffeeCart` is unresolved.

- [ ] **Step 3: Write minimal implementation**

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/model/CoffeeCart.kt
package com.coffeecart.shared.model

data class CoffeeCart(
    val id: String,
    val name: String,
    val isOpen: Boolean,
    val address: String,
    val imageUrl: String,
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "com.coffeecart.shared.model.CoffeeCartTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/coffeecart/shared/model/CoffeeCart.kt shared/src/commonTest/kotlin/com/coffeecart/shared/model/CoffeeCartTest.kt
git commit -m "feat: add CoffeeCart domain model"
```

---

### Task 2: Repository interface + fake implementation

**Files:**
- Create: `shared/src/commonMain/kotlin/com/coffeecart/shared/domain/CoffeeCartRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepository.kt`
- Test: `shared/src/commonTest/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepositoryTest.kt`

**Interfaces:**
- Consumes: `CoffeeCart` from Task 1 (`com.coffeecart.shared.model.CoffeeCart`)
- Produces: `interface CoffeeCartRepository { suspend fun getCoffeeCarts(): List<CoffeeCart> }`, `class FakeCoffeeCartRepository : CoffeeCartRepository`

- [ ] **Step 1: Write the failing test**

```kotlin
// shared/src/commonTest/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepositoryTest.kt
package com.coffeecart.shared.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeCoffeeCartRepositoryTest {
    @Test
    fun `returns three non-empty carts`() = runTest {
        val repository = FakeCoffeeCartRepository()

        val carts = repository.getCoffeeCarts()

        assertEquals(3, carts.size)
        assertTrue(carts.all { it.name.isNotBlank() && it.address.isNotBlank() && it.imageUrl.isNotBlank() })
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "com.coffeecart.shared.data.repository.FakeCoffeeCartRepositoryTest"`
Expected: FAIL — `FakeCoffeeCartRepository` is unresolved.

- [ ] **Step 3: Write minimal implementation**

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/domain/CoffeeCartRepository.kt
package com.coffeecart.shared.domain

import com.coffeecart.shared.model.CoffeeCart

interface CoffeeCartRepository {
    suspend fun getCoffeeCarts(): List<CoffeeCart>
}
```

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepository.kt
package com.coffeecart.shared.data.repository

import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.delay

class FakeCoffeeCartRepository : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> {
        delay(500)
        return listOf(
            CoffeeCart(
                id = "1",
                name = "Downtown Espresso Cart",
                isOpen = true,
                address = "123 Main St",
                imageUrl = "https://picsum.photos/seed/1/200",
            ),
            CoffeeCart(
                id = "2",
                name = "Riverside Brew",
                isOpen = false,
                address = "45 River Rd",
                imageUrl = "https://picsum.photos/seed/2/200",
            ),
            CoffeeCart(
                id = "3",
                name = "Central Park Coffee",
                isOpen = true,
                address = "9 Park Ave",
                imageUrl = "https://picsum.photos/seed/3/200",
            ),
        )
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "com.coffeecart.shared.data.repository.FakeCoffeeCartRepositoryTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/coffeecart/shared/domain/CoffeeCartRepository.kt shared/src/commonMain/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepository.kt shared/src/commonTest/kotlin/com/coffeecart/shared/data/repository/FakeCoffeeCartRepositoryTest.kt
git commit -m "feat: add CoffeeCartRepository interface and fake implementation"
```

---

### Task 3: ViewModel

**Files:**
- Create: `shared/src/commonMain/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModel.kt`
- Modify: `shared/build.gradle.kts` (add `lifecycle-viewmodel` to `commonMain.dependencies`)
- Test: `shared/src/commonTest/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModelTest.kt`

**Interfaces:**
- Consumes: `CoffeeCartRepository` (Task 2), `CoffeeCart` (Task 1)
- Produces:
  ```kotlin
  sealed interface CoffeeCartListUiState {
      data object Loading : CoffeeCartListUiState
      data class Success(val carts: List<CoffeeCart>) : CoffeeCartListUiState
      data class Error(val message: String) : CoffeeCartListUiState
  }
  class CoffeeCartListViewModel(repository: CoffeeCartRepository) : ViewModel() {
      val uiState: StateFlow<CoffeeCartListUiState>
      fun loadCarts()
  }
  ```

- [ ] **Step 1: Add the `lifecycle-viewmodel` dependency**

In `shared/build.gradle.kts`, inside `sourceSets { commonMain.dependencies { ... } }`, add:

```kotlin
            implementation(libs.lifecycle.viewmodel)
```

(The `lifecycle-viewmodel` alias already exists in `gradle/libs.versions.toml`; `:composeApp` already uses it, so no version catalog change is needed here.)

- [ ] **Step 2: Write the failing test**

```kotlin
// shared/src/commonTest/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModelTest.kt
package com.coffeecart.shared.feature.cartlist

import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class SucceedingRepository(private val carts: List<CoffeeCart>) : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> = carts
}

private class FailingRepository(private val exception: Exception) : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> = throw exception
}

class CoffeeCartListViewModelTest {
    private val sampleCart = CoffeeCart(
        id = "1",
        name = "Downtown Espresso Cart",
        isOpen = true,
        address = "123 Main St",
        imageUrl = "https://example.com/cart.png",
    )

    @Test
    fun `starts loading then succeeds with carts from repository`() = runTest {
        val viewModel = CoffeeCartListViewModel(SucceedingRepository(listOf(sampleCart)))

        val state = viewModel.uiState.value
        assertIs<CoffeeCartListUiState.Success>(state)
        assertEquals(listOf(sampleCart), state.carts)
    }

    @Test
    fun `becomes an error state when the repository throws`() = runTest {
        val viewModel = CoffeeCartListViewModel(FailingRepository(RuntimeException("boom")))

        val state = viewModel.uiState.value
        assertIs<CoffeeCartListUiState.Error>(state)
        assertEquals("boom", state.message)
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModelTest"`
Expected: FAIL — `CoffeeCartListViewModel` / `CoffeeCartListUiState` unresolved.

- [ ] **Step 4: Write minimal implementation**

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModel.kt
package com.coffeecart.shared.feature.cartlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    init {
        loadCarts()
    }

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

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModelTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add shared/build.gradle.kts shared/src/commonMain/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModel.kt shared/src/commonTest/kotlin/com/coffeecart/shared/feature/cartlist/CoffeeCartListViewModelTest.kt
git commit -m "feat: add CoffeeCartListViewModel with loading/success/error state"
```

---

### Task 4: Koin module and app-level wiring

**Files:**
- Create: `shared/src/commonMain/kotlin/com/coffeecart/shared/di/CoffeeCartModule.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/coffeecart/app/App.kt`

**Interfaces:**
- Consumes: `CoffeeCartRepository`/`FakeCoffeeCartRepository` (Task 2), `CoffeeCartListViewModel` (Task 3)
- Produces: `val coffeeCartModule: Module` — single binding for `CoffeeCartRepository`, `viewModelOf` binding for `CoffeeCartListViewModel`

This task has no unit test of its own — Koin module wiring is verified by the manual smoke test in Task 6 (the app would crash on launch with a missing-definition error if the module were wrong). Wire it directly and verify by building.

- [ ] **Step 1: Write the Koin module**

```kotlin
// shared/src/commonMain/kotlin/com/coffeecart/shared/di/CoffeeCartModule.kt
package com.coffeecart.shared.di

import com.coffeecart.shared.data.repository.FakeCoffeeCartRepository
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val coffeeCartModule = module {
    single<CoffeeCartRepository> { FakeCoffeeCartRepository() }
    viewModelOf(::CoffeeCartListViewModel)
}
```

- [ ] **Step 2: Bootstrap Koin in `App.kt`**

Replace the contents of `composeApp/src/commonMain/kotlin/com/coffeecart/app/App.kt` with:

```kotlin
package com.coffeecart.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.coffeecart.app.screens.MainScreen
import com.coffeecart.shared.di.coffeeCartModule
import org.koin.compose.KoinApplication

/** Root composable, shared by every platform. */
@Composable
fun App() {
    KoinApplication(application = { modules(coffeeCartModule) }) {
        MaterialTheme {
            Surface {
                MainScreen()
            }
        }
    }
}
```

- [ ] **Step 3: Build to verify the module resolves**

Run: `./gradlew :shared:compileKotlinJvm :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL (no unresolved reference / Koin definition errors). This module isn't consumed by any screen yet, so nothing observable changes.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/coffeecart/shared/di/CoffeeCartModule.kt composeApp/src/commonMain/kotlin/com/coffeecart/app/App.kt
git commit -m "feat: bootstrap Koin and register CoffeeCartModule"
```

---

### Task 5: Add Coil3 dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `composeApp/build.gradle.kts`

**Interfaces:**
- Produces: `coil3.compose.AsyncImage` composable available in `:composeApp` commonMain.

- [ ] **Step 1: Add Coil3 version and library aliases**

In `gradle/libs.versions.toml`, under `[versions]`, add (alongside `navigation = "2.9.2"`):

```toml
coil = "3.3.0"
```

Under `[libraries]`, add a new `# --- images ---` section (after the `# --- compose / androidx ---` section):

```toml
# --- images ---
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor3 = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }
```

- [ ] **Step 2: Add the dependencies to `:composeApp`**

In `composeApp/build.gradle.kts`, inside `sourceSets { commonMain.dependencies { ... } }`, add (after the `koin.*` lines):

```kotlin
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
```

- [ ] **Step 3: Build to verify the dependency resolves**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL — dependency resolves and downloads without error.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml composeApp/build.gradle.kts
git commit -m "build: add Coil3 for image loading"
```

---

### Task 6: List item row composable

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListItem.kt`

**Interfaces:**
- Consumes: `CoffeeCart` (Task 1), `Spacing`/`Spacing.dp` from `com.coffeecart.app.theme`
- Produces: `@Composable fun CoffeeCartListItem(cart: CoffeeCart, onClick: () -> Unit)`

No unit test — this is a leaf UI composable verified visually in Task 8's manual smoke test, consistent with the rest of the screens in this codebase (none have Compose UI tests today).

- [ ] **Step 1: Write the composable**

```kotlin
// composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListItem.kt
package com.coffeecart.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.model.CoffeeCart

private val openDotColor = Color(0xFF4CAF50)
private val closedDotColor = Color(0xFFF44336)

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
                                color = if (cart.isOpen) openDotColor else closedDotColor,
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

- [ ] **Step 2: Build to verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListItem.kt
git commit -m "feat: add CoffeeCartListItem row composable"
```

---

### Task 7: Wire CoffeeCartListScreen to the ViewModel

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListScreen.kt` (replace entire placeholder body)
- Modify: `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/MainScreen.kt:47`

**Interfaces:**
- Consumes: `CoffeeCartListViewModel`, `CoffeeCartListUiState` (Task 3), `CoffeeCartListItem` (Task 6)
- Produces: `@Composable fun CoffeeCartListScreen(onCartClick: (String) -> Unit, viewModel: CoffeeCartListViewModel = koinViewModel())`

- [ ] **Step 1: Replace the placeholder screen**

Replace the full contents of `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListScreen.kt` with:

```kotlin
package com.coffeecart.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartlist.CoffeeCartListUiState
import com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoffeeCartListScreen(
    onCartClick: (String) -> Unit,
    viewModel: CoffeeCartListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CoffeeCartListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is CoffeeCartListUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(state.message)
        }
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

(The old `@Preview` composable is removed — it depended on the no-arg placeholder signature and previewing a live-Koin-injected `ViewModel` isn't meaningful without a preview-time Koin context. Task 6's `CoffeeCartListItem` is the previewable unit if one is wanted later.)

- [ ] **Step 2: Pass the cart id through navigation**

In `composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/MainScreen.kt`, change line 47 from:

```kotlin
            composable(Destination.CoffeeCart.route) {
                CoffeeCartListScreen(onCartClick = { navController.navigate(Destination.Orders.route) })
            }
```

to:

```kotlin
            composable(Destination.CoffeeCart.route) {
                CoffeeCartListScreen(onCartClick = { cartId -> navController.navigate(Destination.Orders.route) })
            }
```

(This makes the discarded parameter explicit as `cartId` rather than the previous unnamed `it`/string. The route itself still doesn't carry the id — passing it through the nav graph as an argument is out of scope for this plan, since there's no cart detail screen yet to receive it.)

- [ ] **Step 3: Build to verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/CoffeeCartListScreen.kt composeApp/src/commonMain/kotlin/com/coffeecart/app/screens/MainScreen.kt
git commit -m "feat: wire CoffeeCartListScreen to CoffeeCartListViewModel"
```

---

### Task 8: Manual verification

No new files. This task confirms the whole slice works end to end on a real target.

- [ ] **Step 1: Run the full test suite**

Run: `./gradlew :shared:jvmTest`
Expected: All tests from Tasks 1–3 pass (BUILD SUCCESSFUL).

- [ ] **Step 2: Launch the app on Android**

Run: `./gradlew :androidApp:installDebug` and open the app on an emulator/device (or use whichever target this project's contributors normally use for a quick check — Android is the simplest to boot for this check).

- [ ] **Step 3: Verify the screen manually**

Navigate to the "CoffeeCart" bottom-nav tab. Confirm:
- A loading spinner briefly appears (the fake repository's simulated 500ms delay).
- Three rows render: "Downtown Espresso Cart" (green dot), "Riverside Brew" (red dot), "Central Park Coffee" (green dot), each with its address and a loaded image on the right.
- Tapping any row navigates to the Orders tab (existing placeholder behavior, unchanged).

- [ ] **Step 4: Commit if any manual fixups were needed**

If Step 3 uncovered a bug, fix it, re-run Steps 1–3, then:

```bash
git add -A
git commit -m "fix: address issues found in manual verification"
```

If no fixups were needed, there is nothing to commit for this task.

---

## Self-review notes

- **Spec coverage:** domain model (Task 1), repository interface + fake (Task 2), ViewModel with Loading/Success/Error (Task 3), Koin bootstrap + module (Task 4), Coil3 dependency (Task 5), row UI with dot indicator (Task 6), screen wiring + id-based navigation (Task 7), manual verification (Task 8) — all spec sections have a task.
- **Placeholder scan:** no TBD/TODO, every step has literal code and exact commands.
- **Type consistency:** `CoffeeCart`, `CoffeeCartRepository`, `CoffeeCartListUiState`, `CoffeeCartListViewModel` package paths and signatures match across every task that references them.
