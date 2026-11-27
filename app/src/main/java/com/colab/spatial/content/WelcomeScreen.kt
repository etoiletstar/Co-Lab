package com.colab.spatial.content

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.colab.spatial.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private val haloBlue = Color(0xFF3B72FF) // design-style: fixed-figma-color brand blue glow
private val haloGreen = Color(0xFF43CF7C) // design-style: fixed-figma-color brand green glow

/**
 * Login/welcome frame (Web 1920 16) with a launch animation on the mascot artwork:
 * spring pop-in with a shockwave ring, breathing glow, floating bob, three comets orbiting the pair,
 * then the title and the button rise in one after another. There is no account backend, so the button just enters the app.
 */
@Composable
fun WelcomeScreen(onEnter: () -> Unit) {
    val hero = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val button = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { hero.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 70f)) }
        delay(650)
        launch { title.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }
        delay(550)
        button.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }
    val loop = rememberInfiniteTransition(label = "welcome")
    val orbit by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "orbit")
    val bob by loop.animateFloat(-1f, 1f, infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val pulse by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")

    GlassPanel(243, 163, 1434, 755, 57) {

    // glow, shockwave and comets share one canvas centred on the mascot pair
    Canvas(Modifier.at(461, 196, 1000, 560)) {
        val u = 1.dp.toPx()
        val c = Offset(size.width / 2f, size.height / 2f + bob * 10f * u)
        val appear = hero.value.coerceIn(0f, 1f)

        // breathing halo: blue left, green right
        val r = (300f + 30f * pulse) * u
        drawCircle(Brush.radialGradient(listOf(haloBlue.copy(alpha = 0.42f * appear), Color.Transparent), c + Offset(-70f * u, -10f * u), r),
            r, c + Offset(-70f * u, -10f * u))
        val r2 = (200f + 24f * (1f - pulse)) * u
        drawCircle(Brush.radialGradient(listOf(haloGreen.copy(alpha = 0.38f * appear), Color.Transparent), c + Offset(120f * u, 50f * u), r2),
            r2, c + Offset(120f * u, 50f * u))

        // one-off shockwave rings as the mascot pops in
        for (k in 0..1) {
            val p = ((hero.value - k * 0.12f) / 0.75f).coerceIn(0f, 1f)
            if (p in 0.001f..0.999f) {
                drawCircle(Color.White.copy(alpha = (1f - p) * 0.55f), (60f + 420f * p) * u, c, style = Stroke(width = (4f - 3f * p) * u))
            }
        }

        // three comets with fading tails on a tilted ellipse
        val a = 330f * u
        val b = 120f * u
        val tilt = (-12f * PI / 180f).toFloat()
        val colors = listOf(Color.White, haloBlue, haloGreen)
        for (i in 0..2) {
            for (t in 0..9) {
                val ang = ((orbit + i / 3f - t * 0.006f) * 2f * PI).toFloat()
                val ex = a * cos(ang)
                val ey = b * sin(ang)
                val x = c.x + ex * cos(tilt) - ey * sin(tilt)
                val y = c.y + ex * sin(tilt) + ey * cos(tilt)
                val fade = (1f - t / 10f) * appear
                drawCircle(colors[i].copy(alpha = 0.55f * fade), (9f - t * 0.6f) * u, Offset(x, y))
                if (t == 0) drawCircle(colors[i].copy(alpha = 0.25f * fade), 18f * u, Offset(x, y))
            }
        }
    }

    Image(painterResource(R.drawable.welcome_hero), "Co-Lab Spatial 蓝绿吉祥物",
        Modifier.at(690, 296, 542, 360).graphicsLayer {
            val s = (0.45f + 0.55f * hero.value) * (1f + 0.018f * pulse)
            scaleX = s; scaleY = s
            rotationZ = (1f - hero.value) * -14f
            translationY = bob * 10f * density
            alpha = hero.value.coerceIn(0f, 1f)
        }, contentScale = ContentScale.Fit)

    Image(painterResource(R.drawable.welcome_title), "Welcome to Co-Lab Spatial，AI-native Spatial Collaboration",
        Modifier.at(720, 610, 480, 175).graphicsLayer {
            alpha = title.value.coerceIn(0f, 1f)
            translationY = (1f - min(title.value, 1f)) * 36f * density
        }, contentScale = ContentScale.Fit)

    }
    Box(Modifier.at(848, 940, 225, 62).graphicsLayer {
        alpha = button.value.coerceIn(0f, 1f)
        val s = 0.85f + 0.15f * min(button.value, 1f)
        scaleX = s; scaleY = s
    }.glass(31, material = true).clickable(onClick = onEnter).testTag("登录/注册"), contentAlignment = Alignment.Center) {
        DText("登录/注册", 18, bold = true)
    }
}
