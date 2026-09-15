package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strNavigateNow
import coffeecart.composeapp.generated.resources.strWeAreOnTheMap
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.buttons.OverlayBackButton
import com.coffeecart.app.ui.location.CoffeeCartMap
import com.coffeecart.shared.feature.bakerydetails.BakeryDetailsUiState
import com.coffeecart.shared.feature.bakerydetails.BakeryDetailsViewModel
import com.coffeecart.shared.model.Bakery
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Details screen displaying the details of the selected bakery.
 * For now: header image, map, and a CTA button only.
 */
@Composable
fun BakeryDetailsScreen(
    bakeryId: String,
    onBackClick: () -> Unit,
    onCtaClick: (String) -> Unit,
    viewModel: BakeryDetailsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bakeryId) {
        viewModel.loadBakery(bakeryId)
    }

    BakeryDetailsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onCtaClick = { onCtaClick(bakeryId) },
    )
}

@Composable
fun BakeryDetailsContent(
    uiState: BakeryDetailsUiState,
    onBackClick: () -> Unit,
    onCtaClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is BakeryDetailsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BakeryDetailsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(Spacing.XXLarge.dp)
                    )
                }
            }
            is BakeryDetailsUiState.Success -> {
                BakeryDetailsSuccessContent(
                    bakery = uiState.bakery,
                    onBackClick = onBackClick,
                    onCtaClick = onCtaClick,
                )
            }
        }
    }
}

@Composable
private fun BakeryDetailsSuccessContent(
    bakery: Bakery,
    onBackClick: () -> Unit,
    onCtaClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        BakeryHeaderImage(
            imageUrl = bakery.imageUrl,
            bakeryName = bakery.name,
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
                    text = bakery.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                if (bakery.address.isNotEmpty()) {
                    Text(
                        text = bakery.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val latitude = bakery.latitude
                val longitude = bakery.longitude
                if (latitude != null && longitude != null) {
                    Spacer(modifier = Modifier.height(Spacing.Large.dp))

                    Text(
                        text = stringResource(Res.string.strWeAreOnTheMap),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(Spacing.Small.dp))

                    BakeryMapBox(latitude = latitude, longitude = longitude)
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
        BakeryCtaButton(onClick = onCtaClick)
    }
}

@Composable
private fun BakeryHeaderImage(
    imageUrl: String,
    bakeryName: String,
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
                contentDescription = "$bakeryName banner",
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
private fun BakeryMapBox(latitude: Double, longitude: Double) {
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
private fun BakeryCtaButton(
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
            text = stringResource(Res.string.strNavigateNow),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun BakeryDetailsScreenSuccessPreview() {
    BakeryDetailsContent(
        uiState = BakeryDetailsUiState.Success(
            Bakery(
                id = "bakery-1",
                name = "Sunrise Bakery",
                address = "12 Baker St",
                imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff",
                latitude = 32.0853,
                longitude = 34.7818,
            )
        ),
        onBackClick = {},
        onCtaClick = {},
    )
}

@Preview
@Composable
private fun BakeryDetailsScreenLoadingPreview() {
    BakeryDetailsContent(
        uiState = BakeryDetailsUiState.Loading,
        onBackClick = {},
        onCtaClick = {},
    )
}

@Preview
@Composable
private fun BakeryDetailsScreenErrorPreview() {
    BakeryDetailsContent(
        uiState = BakeryDetailsUiState.Error("Failed to load bakery detail."),
        onBackClick = {},
        onCtaClick = {},
    )
}
