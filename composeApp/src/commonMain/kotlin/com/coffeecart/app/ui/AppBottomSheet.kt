package com.coffeecart.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp

/**
 * A highly functional and generic BottomSheet holding a scrollable content column
 * and a statically docked close button at the bottom of the layout structure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.XXLarge.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
        ) {
            // First section: Scrollable content container Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                content = content
            )

            // Second section: Static Spacers and standard close button footer
            Spacer(modifier = Modifier.height(Spacing.Medium.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }

            Spacer(modifier = Modifier.height(Spacing.Large.dp))
        }
    }
}

@Preview
@Composable
private fun AppBottomSheetPreview() {
    AppBottomSheet(
        onDismiss = {},
        content = {
            Text(
                text = "Preview BottomSheet Content",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "This is a generic bottom sheet preview containing arbitrary details inside a scrollable view context.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

