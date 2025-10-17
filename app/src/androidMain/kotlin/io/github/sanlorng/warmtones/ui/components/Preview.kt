package io.github.sanlorng.warmtones.ui.components

import androidx.compose.ui.tooling.preview.Preview

object PreviewDevice {
    const val largeScaleDevice = "spec:width=1280dp,height=800dp,dpi=240"
}

@Preview(
    name = "LargePhone",
    widthDp = 360,
    heightDp = 640
)
annotation class CustomPreview()