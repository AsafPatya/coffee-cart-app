# Coding rules

These apply to all Compose UI code in `composeApp`.

## 1. Every UI component ships with a `@Preview`

Every composable that renders UI (screens, reusable components) must have a corresponding
`@Preview` composable next to it, so it can be inspected without running the app. If the
composable takes parameters (callbacks, state), wrap it in a no-arg `@Preview` composable that
supplies stub values.

```kotlin
@Composable
fun HomeScreen(onNewOrderClick: () -> Unit) { ... }

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(onNewOrderClick = {})
}
```

## 2. No hardcoded `.dp` values — use `Spacing`

Never write a raw `8.dp`, `16.dp`, etc. in UI code. Use the `Spacing` scale
(`composeApp/src/commonMain/kotlin/com/coffeecart/app/theme/Spacing.kt`) instead:

```kotlin
Modifier.padding(Spacing.Medium.dp)
```

Scale (9 steps, XXXSmall to XXXLarge):

| Name | Value |
|---|---|
| XXXSmall | 2.dp |
| XXSmall | 4.dp |
| XSmall | 6.dp |
| Small | 8.dp |
| Medium | 12.dp |
| Large | 16.dp |
| XLarge | 20.dp |
| XXLarge | 24.dp |
| XXXLarge | 32.dp |
| XXXXLarge | 48.dp |
| XXXXXLarge | 96.dp |

If a design genuinely needs a value outside this scale, that's a signal to add a new named step
to `Spacing`, not to write a one-off `.dp` literal.
