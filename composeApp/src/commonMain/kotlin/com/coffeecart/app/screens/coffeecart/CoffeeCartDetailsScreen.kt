package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strBasedOnReviews
import coffeecart.composeapp.generated.resources.strOpenHours
import coffeecart.composeapp.generated.resources.strCallNow
import coffeecart.composeapp.generated.resources.strWebsite
import coffeecart.composeapp.generated.resources.strOurImagesGallery
import coffeecart.composeapp.generated.resources.strStartYourOrderNow
import coffeecart.composeapp.generated.resources.strWeAreOnTheMap
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.buttons.OverlayBackButton
import com.coffeecart.app.ui.location.CoffeeCartMap
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.CoffeeCart
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Details screen displaying the details of the selected coffee cart.
 * Plain content — the top bar (title + back button) is owned by AppContainer's single Scaffold.
 */
@Composable
fun CoffeeCartDetailsScreen(
    cartId: String,
    onCartNameLoaded: (String) -> Unit,
    onBackClick: () -> Unit,
    onCtaClick: (String) -> Unit,
    viewModel: CoffeeCartDetailsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cartId) {
        viewModel.loadCart(cartId)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CoffeeCartDetailsUiState.Success -> onCartNameLoaded(state.cart.name)
            is CoffeeCartDetailsUiState.Error -> onCartNameLoaded("")
            is CoffeeCartDetailsUiState.Loading -> onCartNameLoaded("")
        }
    }

    CoffeeCartDetailsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onCtaClick = { onCtaClick(cartId) },
        onFormatWebsiteUrl = { viewModel.getFormattedWebsite(it) },
        onFormatDialerNumber = { viewModel.getDialerNumber(it) },
    )
}

@Composable
fun CoffeeCartDetailsContent(
    uiState: CoffeeCartDetailsUiState,
    onBackClick: () -> Unit,
    onCtaClick: () -> Unit,
    onFormatWebsiteUrl: (String) -> String,
    onFormatDialerNumber: (String) -> String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (uiState) {
            is CoffeeCartDetailsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CoffeeCartDetailsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(Spacing.XXLarge.dp)
                    )
                }
            }
            is CoffeeCartDetailsUiState.Success -> {
                CoffeeCartDetailsSuccessContent(
                    cart = uiState.cart,
                    onBackClick = onBackClick,
                    onCtaClick = onCtaClick,
                    onFormatWebsiteUrl = onFormatWebsiteUrl,
                    onFormatDialerNumber = onFormatDialerNumber,
                )
            }
        }
    }
}

@Composable
private fun CoffeeCartDetailsSuccessContent(
    cart: CoffeeCart,
    onBackClick: () -> Unit,
    onCtaClick: () -> Unit,
    onFormatWebsiteUrl: (String) -> String,
    onFormatDialerNumber: (String) -> String,
) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    var showCallConfirmDialog by remember { mutableStateOf(false) }
    var phoneToCall by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        CoffeeCartHeaderImage(
            imageUrl = cart.imageUrl,
            cartName = cart.name,
            onBackClick = onBackClick,
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = -Spacing.XXLarge.dp),
            shape = RoundedCornerShape(topStart = Spacing.XXLarge.dp, topEnd = Spacing.XXLarge.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.Large.dp),
            ) {
                Text(
                    text = cart.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                if (cart.phone?.isNotBlank() == true || cart.website?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(Spacing.XXSmall.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cart.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                            CoffeeCartActionButton(
                                icon = Icons.Filled.Phone,
                                text = stringResource(Res.string.strCallNow),
                                onClick = {
                                    phoneToCall = phone
                                    showCallConfirmDialog = true
                                }
                            )
                        }

                        cart.website?.takeIf { it.isNotBlank() }?.let { website ->
                            CoffeeCartActionButton(
                                icon = Icons.Filled.Language,
                                text = stringResource(Res.string.strWebsite),
                                onClick = {
                                    try {
                                        val fullUrl = onFormatWebsiteUrl(website)
                                        uriHandler.openUri(fullUrl)
                                    } catch (_: Exception) {
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.Small.dp))

                CoffeeCartBadgesRow(rating = cart.rating, userRatingsTotal = cart.userRatingsTotal)

                Spacer(modifier = Modifier.height(Spacing.Medium.dp))

                if (cart.address.isNotEmpty()) {
                    CoffeeCartLocationRow(address = cart.address)

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Spacing.Medium.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }

                CoffeeCartOpeningHoursSection(openingHours = cart.openingHours)

                if (cart.cartImages.isNotEmpty()) {
                    CoffeeCartImagesSlide(images = cart.cartImages)
                }

                val latitude = cart.latitude
                val longitude = cart.longitude
                if (latitude != null && longitude != null) {
                    Spacer(modifier = Modifier.height(Spacing.Large.dp))

                    Text(
                        text = stringResource(Res.string.strWeAreOnTheMap),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(Spacing.Small.dp))

                    CoffeeCartMapBox(
                        latitude = latitude,
                        longitude = longitude,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.XXXXLarge.dp))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.Large.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        CoffeeCartStartOrderButton(onClick = onCtaClick)
    }

    if (showCallConfirmDialog && phoneToCall != null) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val dialogTitle = if (isRtl) "לחייג לעגלה?" else "Make a Call"
        val dialogText = if (isRtl) {
            "האם ברצונך להתקשר ל-${cart.name} במספר $phoneToCall?"
        } else {
            "Would you like to call ${cart.name} at $phoneToCall?"
        }
        val confirmText = if (isRtl) "התקשר" else "Call"
        val cancelText = if (isRtl) "ביטול" else "Cancel"

        AlertDialog(
            onDismissRequest = { showCallConfirmDialog = false },
            title = {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = dialogText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCallConfirmDialog = false
                        try {
                            val telUri = onFormatDialerNumber(phoneToCall!!)
                            uriHandler.openUri(telUri)
                        } catch (_: Exception) {
                        }
                    }
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCallConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(cancelText)
                }
            }
        )
    }
}

@Composable
private fun CoffeeCartHeaderImage(
    imageUrl: String,
    cartName: String,
    onBackClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Spacing.HeroHeight.dp)
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$cartName banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        OverlayBackButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

@Composable
private fun CoffeeCartBadgesRow(
    rating: Double?,
    userRatingsTotal: Int?,
) {
    if (rating == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.XXSmall.dp)
    ) {
        Text(
            text = rating.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.None.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val starDiff = rating - (i - 1)
                val icon = when {
                    starDiff >= 0.75 -> Icons.Filled.Star
                    starDiff >= 0.25 -> Icons.AutoMirrored.Filled.StarHalf
                    else -> Icons.Filled.StarBorder
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(Spacing.Large.dp)
                )
            }
        }

        if (userRatingsTotal != null) {
            Text(
                text = stringResource(Res.string.strBasedOnReviews).replace("%d", userRatingsTotal.toString()),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium
                ),
            )
        }
    }
}

@Composable
private fun CoffeeCartActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = Spacing.Medium.dp, vertical = Spacing.Small.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(Spacing.Large.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.Small.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CoffeeCartLocationRow(address: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = address,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CoffeeCartMapBox(latitude: Double, longitude: Double) {
    val shape = RoundedCornerShape(Spacing.Large.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Spacing.MapHeight.dp)
            .shadow(
                elevation = Spacing.Small.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
    ) {
        CoffeeCartMap(
            latitude = latitude,
            longitude = longitude,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun CoffeeCartOpeningHoursSection(
    openingHours: List<String>,
    modifier: Modifier = Modifier,
) {
    if (openingHours.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.strOpenHours),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(Spacing.Small.dp))

        Text(
            text = openingHours.joinToString("\n"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CoffeeCartImagesSlide(
    images: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(Spacing.Large.dp))

        Text(
            text = stringResource(Res.string.strOurImagesGallery),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(Spacing.Small.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(images) { imageUrl ->
                Box(
                    modifier = Modifier
                        .size(width = Spacing.HeroHeight.dp, height = Spacing.CategoryCardHeight.dp)
                        .clip(RoundedCornerShape(Spacing.Small.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Coffee Cart Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CoffeeCartStartOrderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.XXXXLarge.dp),
        shape = RoundedCornerShape(Spacing.XXXLarge.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(
            text = stringResource(Res.string.strStartYourOrderNow),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenSuccessPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Success(
            CoffeeCart(
                id = "123",
                name = "Downtown Espresso Cart",
                address = "123 Main St",
                imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
                latitude = 32.0853,
                longitude = 34.7818,
                phone = "+1 555-0199",
                openingHours = listOf("Sun-Thu: 08:00 – 18:00", "Fri: 08:00 – 15:00"),
                cartImages = listOf(
                    "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"
                ),
                rating = 4.6,
                userRatingsTotal = 211,
                website = "https://example.com/coffeecart",
            )
        ),
        onBackClick = {},
        onCtaClick = {},
        onFormatWebsiteUrl = { "https://$it" },
        onFormatDialerNumber = { "tel:$it" }
    )
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenHebrewPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        CoffeeCartDetailsContent(
            uiState = CoffeeCartDetailsUiState.Success(
                CoffeeCart(
                    id = "123",
                    name = "עגלה בחווה",
                    address = "כפר ביאליק",
                    imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
                    latitude = 32.0853,
                    longitude = 34.7818,
                    phone = "050-1234567",
                    openingHours = listOf("א׳-ה׳: 08:00 – 18:00", "יום שישי: 08:00 – 15:00"),
                    cartImages = listOf(
                        "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"
                    ),
                    rating = 4.7,
                    userRatingsTotal = 45,
                    website = "https://example.com/farmcart",
                )
            ),
            onBackClick = {},
            onCtaClick = {},
            onFormatWebsiteUrl = { "https://$it" },
            onFormatDialerNumber = { "tel:$it" }
        )
    }
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenLoadingPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Loading,
        onBackClick = {},
        onCtaClick = {},
        onFormatWebsiteUrl = { it },
        onFormatDialerNumber = { it }
    )
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenErrorPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Error("Failed to load coffee cart detail."),
        onBackClick = {},
        onCtaClick = {},
        onFormatWebsiteUrl = { it },
        onFormatDialerNumber = { it }
    )
}
