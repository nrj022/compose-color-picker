# Compose Color Picker

🎨 Color Picker for Jetpack compose

![colorPickers](/assets/colorPickers.png)

A color picker for Jetpack compose 🎨

> This is a fork of [mhssn95/compose-color-picker](https://github.com/mhssn95/compose-color-picker).
>
> Changes from the original:
> - Updated to Gradle 9 / AGP 9 / Kotlin 2.4 / Compose 1.11
> - Migrated the dialog from Material 2 to Material 3 (follows the app's theme and dark mode)
> - Added `initialColor` to all color picker types
> - Published via JitPack

# Download

Add the JitPack repository in `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency:

```groovy
implementation 'com.github.nrj022:compose-color-picker:2.0.1'
```

Requirements: `minSdk 21`, `compileSdk 33`, Kotlin 2.0, Compose BOM 2023.08.00 (Compose 1.5) or higher.
# Usage

```kotlin
ColorPicker {
    color = it
}
```

You can provide a type for the color picker as the following.

```kotlin
ColorPicker(
    type = ColorPickerType.Classic()
) {
    color = it
}
```
#### Color picker types
- Classic
  
  ```kotlin
  ColorPickerType.Classic(
      showAlphaBar = true,
      initialColor = Color.White
  )
  ```
  
- Circle

  ```kotlin
  ColorPickerType.Circle(
      showBrightnessBar = true,
      showAlphaBar = true,
      lightCenter = true,
      initialColor = Color.White // defaults to the center color
  )
  ```

	![circleColorPickers](/assets/circleColorPicker.png)


- Ring

  ```kotlin
  ColorPickerType.Ring(
      ringWidth = 10.dp,
      previewRadius = 80.dp,
      showLightnessBar = true,
      showDarknessBar = true,
      showAlphaBar = true,
      showColorPreview = true,
      initialColor = Color.Red
  )
  ```

  

- Simple Ring

  ```kotlin
  ColorPickerType.SimpleRing(
      colorWidth = 20.dp,
      tracksCount = 5,
      sectorsCount = 24,
      initialColor = Color.Red // the closest color in the ring is selected
  )
  ```

`initialColor` is only applied when the picker is first shown; later changes are ignored.

## Color Picker Dialog

```kotlin
ColorPickerDialog(
    show = showDialog,
    type = colorPickerType,
    properties = DialogProperties(),
    onDismissRequest = {
    	showDialog = false
    },
    onPickedColor = {
    	color = it
    },
)
```

## Useful Extensions

- ```kotlin
  val color = Color(0xffe1a1c1)
  val alpha = color.alpha() //return alpha value as integer => 255
  val red = color.red() //return red value as integer => 225
  val green = color.green() //return green value as integer => 161
  val blue = color.blue() //return blue value as integer => 193
  val hex = color.toHex() //return argb color as hex string
  val (alpha, red, green, blue) = color.argb() //return an array for all color channels value
  ```

- add `Modifier.transparentBackground` to draw behind your composable element a transparent effect background

  ```kotlin
  Box(
      modifier = Modifier
      .width(205.dp)
      .height(300.dp)
      .clip(RoundedCornerShape(32.dp))
      .transparentBackground(verticalBoxesCount = 16)
      .background(Color.Cyan.copy(alpha = 0.2f))
  )
  ```

  ![transparentBackground](/assets/transparentBackground.png)

# License

```
Copyright 2022 mhssn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

