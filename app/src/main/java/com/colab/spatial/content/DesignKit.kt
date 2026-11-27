package com.colab.spatial.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text

/**
 * The supplied designs are 1920 x 1080 frames. Pages are laid out in that coordinate space (1 unit = 1dp)
 * and the whole stage is scaled uniformly to the window, so proportions match the design at any 16:9 size.
 */
@Composable
fun DesignStage(content: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scale = minOf(maxWidth.value / 1920f, maxHeight.value / 1080f)
        Box(Modifier.align(Alignment.Center).requiredSize(1920.dp, 1080.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale), content = content)
    }
}

/** Absolute placement in design units. */
fun Modifier.at(x: Int, y: Int, w: Int, h: Int): Modifier = offset(x.dp, y.dp).size(w.dp, h.dp)

val DesignWhite = Color.White
val GlassFill = Color.Black.copy(alpha = 0.23f) // design-style: fixed-figma-color Web 1920 glass layer
val GlassStroke = Color.White.copy(alpha = 0.28f) // design-style: fixed-figma-color Web 1920 glass border
val ChipFill = Color.Black.copy(alpha = 0.18f) // design-style: fixed-figma-color Web 1920 inner chip
val CardFill = Color(0xFF383838).copy(alpha = 0.48f) // design-style: fixed-figma-color Web 1920 project card
val DarkButton = Color.Black.copy(alpha = 0.32f) // design-style: fixed-figma-color Web 1920 dark pill button
val AvatarBlue = Color(0xFF2A82E4) // design-style: fixed-figma-color Web 1920 admin avatar
val AvatarGreen = Color(0xFF43CF7C) // design-style: fixed-figma-color Web 1920 member avatar
val AvatarRed = Color(0xFFFF5733) // design-style: fixed-figma-color Web 1920 member avatar
val LinkBlue = Color(0xFF3B72FF) // design-style: fixed-figma-color Web 1920 project link

/**
 * Frosted glass surface used by every component. The design specifies a heavy backdrop blur; the SDK's
 * backgroundMaterial only offers Thin/Regular/Thick (no pixel radius), so the strongest, Thick, is used.
 */
fun Modifier.glass(radius: Int, fill: Color = GlassFill, material: Boolean = radius >= 50): Modifier {
    val shape = RoundedCornerShape(radius.dp)
    val base = this.clip(shape)
    // Nested materials hid sibling content in the emulator, so only top-level surfaces get the material;
    // small chips and cards sit on that already-blurred backdrop.
    val surface = if (material) base.backgroundMaterial(style = Material.Regular) else base
    return surface.background(fill).border(1.dp, GlassStroke, shape)
}

/** Text with a pixel size taken from the design. */
@Composable
fun DText(text: String, size: Int, bold: Boolean = false, color: Color = DesignWhite, alpha: Float = 1f,
          modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE, lineHeight: Float = 1.35f) {
    Text(text, modifier = modifier, maxLines = maxLines, color = color.copy(alpha = alpha),
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        style = PicoTheme.typography.bodyMedium.copy(fontSize = size.sp, lineHeight = (size * lineHeight).sp))
}

/** Member avatar: colored disc with two white eyes tilted -15 degrees. */
@Composable
fun MemberAvatar(color: Color, size: Int = 28) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy((size * 0.18f).dp)) {
            repeat(2) {
                Box(Modifier.size((size * 0.11f).dp, (size * 0.29f).dp).rotate(-15f)
                    .clip(RoundedCornerShape(2.dp)).background(Color.White))
            }
        }
    }
}

@Composable
fun AvatarStack(size: Int = 28) {
    Row(horizontalArrangement = Arrangement.spacedBy((-size * 0.36f).dp)) {
        listOf(AvatarGreen, AvatarBlue, AvatarRed).forEach { MemberAvatar(it, size) }
    }
}

/**
 * Frosted panel whose content lives inside it. A material surface draws over siblings that are not its children,
 * so everything on the panel is placed in an inner box that keeps the stage's absolute coordinates.
 */
@Composable
fun GlassPanel(x: Int, y: Int, w: Int, h: Int, radius: Int, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.at(x, y, w, h).glass(radius, material = true)) {
        Box(Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).size(1920.dp, 1080.dp).offset((-x).dp, (-y).dp), content = content)
    }
}
