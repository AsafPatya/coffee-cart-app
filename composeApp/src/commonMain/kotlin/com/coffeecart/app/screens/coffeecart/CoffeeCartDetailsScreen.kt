package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strKosher
import coffeecart.composeapp.generated.resources.strOpenNow
import coffeecart.composeapp.generated.resources.strOpeningHours
import coffeecart.composeapp.generated.resources.strStartYourOrderNow
import coffeecart.composeapp.generated.resources.strOurImagesGallery
import coffeecart.composeapp.generated.resources.strWeAreOnTheMap
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.BorderWidth
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
    )
}

@Composable
fun CoffeeCartDetailsContent(
    uiState: CoffeeCartDetailsUiState,
    onBackClick: () -> Unit,
    onCtaClick: () -> Unit,
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
) {
    val scrollState = rememberScrollState()

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
                )

                cart.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Spacer(modifier = Modifier.height(Spacing.XXSmall.dp))
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Small.dp))

                CoffeeCartBadgesRow()

                Spacer(modifier = Modifier.height(Spacing.Medium.dp))

                if (cart.address.isNotEmpty()) {
                    CoffeeCartLocationRow(address = cart.address)

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Spacing.Medium.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }

                val openingHours = cart.openingHours
                val openingHoursText = if (openingHours.isNotEmpty()) {
                    openingHours.joinToString("\n")
                } else {
                    stringResource(Res.string.strOpeningHours)
                }

                Text(
                    text = openingHoursText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

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
private fun CoffeeCartBadgesRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(Spacing.Large.dp),
            border = BorderStroke(BorderWidth.XXXSmall.dp, Color(0xFF4CAF50)),
            color = Color(0xFFE8F5E9),
        ) {
            Text(
                text = stringResource(Res.string.strOpenNow),
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = Spacing.Medium.dp, vertical = Spacing.XXSmall.dp),
            )
        }

        Surface(
            shape = RoundedCornerShape(Spacing.Large.dp),
            border = BorderStroke(BorderWidth.XXXSmall.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Spacing.Medium.dp, vertical = Spacing.XXSmall.dp),
            ) {
                Text(
                    text = "4.8 ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(Spacing.Large.dp),
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(Spacing.Large.dp),
            border = BorderStroke(BorderWidth.XXXSmall.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            color = Color.Transparent,
        ) {
            Text(
                text = stringResource(Res.string.strKosher),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = Spacing.Medium.dp, vertical = Spacing.XXSmall.dp),
            )
        }
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
                cartImages = listOf(
                    "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"
                ),
            )
        ),
        onBackClick = {},
        onCtaClick = {}
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
                    cartImages = listOf(
                        "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"
                    ),
                )
            ),
            onBackClick = {},
            onCtaClick = {}
        )
    }
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenLoadingPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Loading,
        onBackClick = {},
        onCtaClick = {}
    )
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenErrorPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Error("Failed to load coffee cart detail."),
        onBackClick = {},
        onCtaClick = {}
    )
}
