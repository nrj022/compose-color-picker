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
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.mhssn.colorpicker.data.ColorRange
import io.mhssn.colorpicker.data.Colors.gradientColors
import io.mhssn.colorpicker.ext.*
import io.mhssn.colorpicker.helper.BoundedPointStrategy
import io.mhssn.colorpicker.helper.ColorPickerHelper
import io.mhssn.colorpicker.helper.MathHelper.getBoundedPointWithInRadius
import io.mhssn.colorpicker.helper.MathHelper.getLength
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@ExperimentalComposeUiApi
@Composable
internal fun CircleColorPicker(
    modifier: Modifier = Modifier,
    showAlphaBar: Boolean,
    showBrightnessBar: Boolean,
    lightCenter: Boolean,
    initialColor: Color = if (lightCenter) Color.White else Color.Black,
    onPickedColor: (Color) -> Unit
) {
    var radius by remember {
        mutableFloatStateOf(0f)
    }
    val initialHsv = remember {
        FloatArray(3).also { colorToHSV(initialColor.toArgb(), it) }
    }
    // Circle: angle = hue, distance from center = how far from the center color (white or black)
    // Brightness bar then mixes in the opposite color (black or white)
    // lightCenter: distance = S, brightness = 1 - V
    // dark center: brightness = V(1 - S), distance = VS / (1 - V(1 - S))
    val initialDistance: Float
    val initialBrightness: Float
    if (lightCenter) {
        initialDistance = initialHsv[1]
        initialBrightness = 1 - initialHsv[2]
    } else {
        initialBrightness = initialHsv[2] * (1 - initialHsv[1])
        initialDistance = if (initialBrightness < 1f) {
            initialHsv[2] * initialHsv[1] / (1 - initialBrightness)
        } else {
            0f
        }
    }
    // Offset from the center as a fraction of the radius, converted with the draw size
    // so it's correct from the first frame
    var pickerFraction by remember {
        val angle = Math.toRadians(initialHsv[0].toDouble())
        mutableStateOf(
            Offset(
                initialDistance * cos(angle).toFloat(),
                initialDistance * sin(angle).toFloat()
            )
        )
    }
    var pickerColor by remember {
        mutableStateOf(
            if (lightCenter) {
                Color.hsv(initialHsv[0], initialDistance, 1f)
            } else {
                Color.hsv(initialHsv[0], 1f, initialDistance)
            }
        )
    }
    var brightness by remember {
        mutableFloatStateOf(initialBrightness)
    }
    var alpha by remember {
        mutableFloatStateOf(initialColor.alpha)
    }
    LaunchedEffect(brightness, pickerColor, alpha) {
        onPickedColor(
            Color(
                pickerColor.red().moveColorTo(!lightCenter, brightness),
                pickerColor.green().moveColorTo(!lightCenter, brightness),
                pickerColor.blue().moveColorTo(!lightCenter, brightness),
                (255 * alpha).roundToInt()
            )
        )
    }
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        Canvas(modifier = modifier
            .size(200.dp)
            .onSizeChanged {
                radius = it.width / 2f
            }
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val angle =
                            (Math.toDegrees(
                                atan2(
                                    it.y - radius,
                                    it.x - radius
                                ).toDouble()
                            ) + 360) % 360
                        val length = getLength(it.x, it.y, radius)
                        val radiusProgress = 1 - (length / radius).coerceIn(0f, 1f)
                        val angleProgress = angle / 360f
                        val (rangeProgress, range) = ColorPickerHelper.calculateRangeProgress(
                            angleProgress
                        )
                        pickerColor = when (range) {
                            ColorRange.RedToYellow -> {
                                Color(
                                    red = 255.moveColorTo(lightCenter, radiusProgress),
                                    green = (255f * rangeProgress)
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    blue = 0.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.YellowToGreen -> {
                                Color(
                                    red = (255 * (1 - rangeProgress))
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    green = 255.moveColorTo(lightCenter, radiusProgress),
                                    blue = 0.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.GreenToCyan -> {
                                Color(
                                    red = 0.moveColorTo(lightCenter, radiusProgress),
                                    green = 255.moveColorTo(lightCenter, radiusProgress),
                                    blue = (255 * rangeProgress)
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                )
                            }
                            ColorRange.CyanToBlue -> {
                                Color(
                                    red = 0.moveColorTo(lightCenter, radiusProgress),
                                    green = (255 * (1 - rangeProgress))
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    blue = 255.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.BlueToPurple -> {
                                Color(
                                    red = (255 * rangeProgress)
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                    green = 0.moveColorTo(lightCenter, radiusProgress),
                                    blue = 255.moveColorTo(lightCenter, radiusProgress),
                                )
                            }
                            ColorRange.PurpleToRed -> {
                                Color(
                                    red = 255.moveColorTo(lightCenter, radiusProgress),
                                    green = 0.moveColorTo(lightCenter, radiusProgress),
                                    blue = (255 * (1 - rangeProgress))
                                        .moveColorTo(lightCenter, radiusProgress)
                                        .roundToInt(),
                                )
                            }
                        }
                        val location = getBoundedPointWithInRadius(
                            it.x,
                            it.y,
                            length,
                            radius,
                            BoundedPointStrategy.Inside
                        )
                        pickerFraction = Offset(
                            (location.x - radius) / radius,
                            (location.y - radius) / radius
                        )
                    }
                }
                return@pointerInteropFilter true
            }) {
            drawCircle(
                Brush.sweepGradient(gradientColors)
            )
            drawCircle(
                ShaderBrush(
                    RadialGradientShader(
                        Offset(size.width / 2f, size.height / 2f),
                        colors = listOf(
                            if (lightCenter) {
                                Color.White
                            } else {
                                Color.Black
                            }, Color.Transparent
                        ),
                        radius = size.width / 2f
                    )
                )
            )
            val center = size.width / 2f
            drawColorSelector(
                pickerColor,
                Offset(center + pickerFraction.x * center, center + pickerFraction.y * center)
            )
        }
        if (showBrightnessBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(
                colors = listOf(
                    if (lightCenter) {
                        Color.Black
                    } else {
                        Color.White
                    }, pickerColor
                ),
                initialProgress = 1 - initialBrightness
            ) {
                brightness = 1 - it
            }
        }
        if (showAlphaBar) {
            Spacer(modifier = Modifier.height(16.dp))
            ColorSlideBar(
                colors = listOf(Color.Transparent, pickerColor),
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
private fun CircleColorPickerPreview() {
    CircleColorPicker(
        modifier = Modifier,
        initialColor = Color(0xFF6FA8DC),
        showAlphaBar = true,
        showBrightnessBar = true,
        lightCenter = true,
        onPickedColor = {}
    )
}

private fun Int.moveColorTo(toWhite: Boolean, progress: Float): Int {
    return if (toWhite) {
        lighten(progress)
    } else {
        darken(progress)
    }
}

private fun Double.moveColorTo(toWhite: Boolean, progress: Float): Double {
    return if (toWhite) {
        lighten(progress)
    } else {
        darken(progress)
    }
}