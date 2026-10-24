package io.mhssn.colorpicker.pickers

import android.view.MotionEvent
import android.graphics.Color.colorToHSV
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.mhssn.colorpicker.data.ColorRange
import io.mhssn.colorpicker.data.Colors.gradientColors
import io.mhssn.colorpicker.ext.*
import io.mhssn.colorpicker.helper.ColorPickerHelper
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@ExperimentalComposeUiApi
@Composable
internal fun RingColorPicker(
    modifier: Modifier = Modifier,
    ringWidth: Dp,
    previewRadius: Dp,
    showLightColorBar: Boolean,
    showDarkColorBar: Boolean,
    showAlphaBar: Boolean,
    showColorPreview: Boolean,
    initialColor: Color = Color.Red,
    onPickedColor: (Color) -> Unit,
) {
    val density = LocalDensity.current
    val ringWidthPx = remember {
        with(density) { ringWidth.toPx() }
    }
    val previewRadiusPx = remember {
        with(density) { previewRadius.toPx() }
    }
    var radius by remember {
        mutableFloatStateOf(0f)
    }
    // Ring hue, lightness and darkness map to HSV: hue = H, lightness = 1 - S, darkness = 1 - V
    val initialHsv = remember {
        FloatArray(3).also { colorToHSV(initialColor.toArgb(), it) }
    }
    // The selector always sits on the ring, so only the angle is stored and the position is
    // computed with the draw size, so it's correct from the first frame
    var pickerAngle by remember {
        mutableDoubleStateOf(Math.toRadians(initialHsv[0].toDouble()))
    }
    var selectedColor by remember {
        mutableStateOf(Color.hsv(initialHsv[0], 1f, 1f))
    }
    var color by remember {
        mutableStateOf(initialColor)
    }
    var lightColor by remember {
        mutableStateOf(Color.hsv(initialHsv[0], initialHsv[1], 1f))
    }
    var darkColor by remember {
        mutableStateOf(initialColor.copy(alpha = 1f))
    }
    var lightness by remember {
        mutableFloatStateOf(1 - initialHsv[1])
    }
    var darkness by remember {
        mutableFloatStateOf(1 - initialHsv[2])
    }
    var alpha by remember {
        mutableFloatStateOf(initialColor.alpha)
    }
    LaunchedEffect(selectedColor, lightness, darkness, alpha) {
        var red = selectedColor.red().lighten(lightness)
        var green = selectedColor.green().lighten(lightness)
        var blue = selectedColor.blue().lighten(lightness)
        lightColor = Color(red, green, blue, 255)
        red = red.darken(darkness)
        green = green.darken(darkness)
        blue = blue.darken(darkness)
        darkColor = Color(red, green, blue, 255)
        color = Color(red, green, blue, (255 * alpha).roundToInt())
        onPickedColor(color)
    }
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        Canvas(modifier = modifier
            .size(200.dp)
            .onSizeChanged {
                radius = it.width.toFloat() / 2
            }
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val red: Int
                        val green: Int
                        val blue: Int
                        val angle =
                            (Math.toDegrees(
                                atan2(
                                    it.y - radius,
                                    it.x - radius
                                ).toDouble()
                            ) + 360) % 360
                        val progress = angle / 360f
                        val (rangeProgress, range) = ColorPickerHelper.calculateRangeProgress(
                            progress
                        )
                        when (range) {
                            ColorRange.RedToYellow -> {
                                red = 255
                                green = (255f * rangeProgress).roundToInt()
                                blue = 0
                            }
                            ColorRange.YellowToGreen -> {
                                red = (255 * (1 - rangeProgress)).roundToInt()
                                green = 255
                                blue = 0
                            }
                            ColorRange.GreenToCyan -> {
                                red = 0
                                green = 255
                                blue = (255 * rangeProgress).roundToInt()
                            }
                            ColorRange.CyanToBlue -> {
                                red = 0
                                green = (255 * (1 - rangeProgress)).roundToInt()
                                blue = 255
                            }
                            ColorRange.BlueToPurple -> {
                                red = (255 * rangeProgress).roundToInt()
                                green = 0
                                blue = 255
                            }
                            ColorRange.PurpleToRed -> {
                                red = 255
                                green = 0
                                blue = (255 * (1 - rangeProgress)).roundToInt()
                            }
                        }
                        pickerAngle = Math.toRadians(angle)
                        selectedColor = Color(red, green, blue)
                    }
                }
                return@pointerInteropFilter true
            }) {
            val center = size.width / 2f
            val ringRadius = center - ringWidthPx / 2f
            drawCircle(
                Brush.sweepGradient(gradientColors),
                radius = ringRadius,
                style = Stroke(ringWidthPx)
            )
            if (showColorPreview) {
                drawCircle(
                    color,
                    radius = previewRadiusPx
                )
            }
            drawColorSelector(
                selectedColor,
                Offset(
                    center + ringRadius * cos(pickerAngle).toFloat(),
                    center + ringRadius * sin(pickerAngle).toFloat()
                )
            )
        }
        if (showLightColorBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(
                colors = listOf(
                    Color.White,
                    selectedColor
                ),
                initialProgress = initialHsv[1]
            ) {
                lightness = 1 - it
            }
        }
        if (showDarkColorBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(
                colors = listOf(Color.Black, lightColor),
                initialProgress = initialHsv[2]
            ) {
                darkness = 1 - it
            }
        }
        if (showAlphaBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(
                colors = listOf(Color.Transparent, darkColor),
                initialProgress = initialColor.alpha
            ) {
                alpha = it
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun RingColorPickerPreview() {
    RingColorPicker(
        modifier = Modifier,
        initialColor = Color(0xFF6FA8DC),
        ringWidth = 20.dp,
        previewRadius = 70.dp,
        showLightColorBar = true,
        showDarkColorBar = true,
        showAlphaBar = true,
        showColorPreview = true,
        onPickedColor = {}
    )
}