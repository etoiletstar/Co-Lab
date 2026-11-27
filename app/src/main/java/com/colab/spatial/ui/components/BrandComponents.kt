package com.colab.spatial.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import com.colab.spatial.R
import com.colab.spatial.data.NodeKind
import com.colab.spatial.ui.theme.CoLabPalette as Brand
import com.pico.spatial.ui.design.*

private object BrandImageLoader {
    private var loader: ImageLoader? = null
    @Synchronized fun get(context: Context): ImageLoader = loader ?: ImageLoader.Builder(context.applicationContext)
        .components { add(SvgDecoder.Factory()) }.build().also { loader = it }
}

@Composable
fun BrandIcon(name: String, modifier: Modifier = Modifier.size(20.dp), tint: Color? = null, description: String? = null) {
    val context = LocalContext.current
    val loader = remember(context.applicationContext) { BrandImageLoader.get(context) }
    val painter = rememberAsyncImagePainter("file:///android_asset/icons/$name", imageLoader = loader)
    if (tint == null) Icon(painter = painter, contentDescription = description, modifier = modifier)
    else Icon(painter = painter, contentDescription = description, modifier = modifier, tint = tint)
}

@Composable
fun BrandHero(modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(24.dp))
        // design-style: design-surface brand-hero
        .background(Brush.linearGradient(listOf(Brand.navy, Brand.navySoft)))
        .padding(28.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Co-Lab Spatial", style = PicoTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PicoTheme.colorScheme.labelPrimaryLight)
            Text("让想法，在空间里相遇。", style = PicoTheme.typography.titleSmall, color = PicoTheme.colorScheme.labelPrimaryLight)
            Text("Your Skills. Your Agents. One Team.", style = PicoTheme.typography.bodySmall, color = Brand.collabGreen)
        }
        Image(painterResource(R.drawable.agent_duo), "Co-Lab 蓝绿吉祥物", Modifier.size(160.dp, 138.dp))
    }
}

@Composable
fun KindBadge(kind: NodeKind, modifier: Modifier = Modifier) {
    Chip(label = { Text(kind.label) }, onClick = null, modifier = modifier,
        leadingIcon = { BrandIcon(Brand.icon(kind), Modifier.size(16.dp)) },
        colors = ChipsDefaults.chipColors(backgroundColor = Brand.soft(kind), contentColor = Brand.ink(kind)))
}

@Composable
fun AuthorAvatar(author: String, modifier: Modifier = Modifier.size(26.dp)) {
    Box(modifier.clip(CircleShape)
        // design-style: design-surface avatar
        .background(Brand.blueSoft), contentAlignment = Alignment.Center) {
        Text(author.take(1), style = PicoTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Brand.brandBlue)
    }
}

@Composable
fun WindowLaunchItem(title: String, description: String, icon: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(modifier = modifier.clip(RoundedCornerShape(16.dp))
        // design-style: design-surface launch-item
        .background(Brand.paper).clickable(onClick = onClick), padding = PaddingValues(16.dp),
        leadingContent = { BrandIcon(icon, tint = Brand.brandBlue) },
        headlineContent = { Text(title, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(description) },
        trailingContent = { BrandIcon("arrow-right.svg") })
}

@Composable
fun Metric(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(value, style = PicoTheme.typography.titleLarge, color = Brand.brandBlue, fontWeight = FontWeight.Bold)
        Text(label, style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
    }
}

@Composable
fun DocumentBody(markdown: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        markdown.lines().forEach { line ->
            when {
                line.isBlank() -> Spacer(Modifier.height(4.dp))
                line.startsWith("# ") -> Text(line.removePrefix("# "), style = PicoTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Brand.ink)
                line.startsWith("## ") -> Text(line.removePrefix("## "), style = PicoTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Brand.ink)
                line.startsWith("> ") -> Text(line.removePrefix("> "), style = PicoTheme.typography.bodySmall, color = Brand.muted)
                line == "---" -> HorizontalDivider()
                else -> Text(if (line.startsWith("- ")) "• ${line.removePrefix("- ")}" else line,
                    style = PicoTheme.typography.bodyMedium, color = Brand.ink)
            }
        }
    }
}
