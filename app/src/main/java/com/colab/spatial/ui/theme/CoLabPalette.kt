package com.colab.spatial.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import com.pico.spatial.ui.design.PicoTheme
import com.colab.spatial.data.NodeKind

/** App-owned colors from the supplied brand posters and original tokens.css/styles.css. */
object CoLabPalette {
    val navy = Color(0xFF101B2E) // design-style: fixed-figma-color supplied brand poster, bounded hero
    val navySoft = Color(0xFF24364F) // design-style: fixed-figma-color supplied brand poster, hero gradient
    val brandBlue = Color(0xFF3B72FF) // design-style: fixed-figma-color original tokens.css primary
    val collabGreen = Color(0xFF43CF7C) // design-style: fixed-figma-color original tokens.css collab
    val blueSoft = Color(0xFFEEF3FF) // design-style: fixed-figma-color original tokens.css primary-light
    val canvas: Color @Composable get() = PicoTheme.colorScheme.fillLight
    val paper: Color @Composable get() = PicoTheme.colorScheme.fillSecondary
    val ink: Color @Composable get() = PicoTheme.colorScheme.labelPrimary
    val muted: Color @Composable get() = PicoTheme.colorScheme.labelSecondary
    val grid: Color @Composable get() = PicoTheme.colorScheme.dividerLine
    val ideaInk = Color(0xFFB26A05) // design-style: fixed-figma-color original styles.css node--idea
    val ideaSoft = Color(0xFFFDF4E7) // design-style: fixed-figma-color original styles.css node--idea
    val discussionInk = Color(0xFF2F6FE4) // design-style: fixed-figma-color original styles.css node--discussion
    val discussionSoft = Color(0xFFEEF4FF) // design-style: fixed-figma-color original styles.css node--discussion
    val decisionInk = Color(0xFF0F8A63) // design-style: fixed-figma-color original styles.css node--decision
    val decisionSoft = Color(0xFFE9F7F1) // design-style: fixed-figma-color original styles.css node--decision
    val taskInk = Color(0xFFCF3F5B) // design-style: fixed-figma-color original styles.css node--task
    val taskSoft = Color(0xFFFDEFF2) // design-style: fixed-figma-color original styles.css node--task
    val artifactInk = Color(0xFF6D4AFF) // design-style: fixed-figma-color original styles.css node--artifact
    val artifactSoft = Color(0xFFF3EFFF) // design-style: fixed-figma-color original styles.css node--artifact

    fun ink(kind: NodeKind): Color = when(kind) {
        NodeKind.IDEA -> ideaInk; NodeKind.DISCUSSION -> discussionInk
        NodeKind.DECISION -> decisionInk; NodeKind.TASK -> taskInk; NodeKind.ARTIFACT -> artifactInk
    }
    fun soft(kind: NodeKind): Color = when(kind) {
        NodeKind.IDEA -> ideaSoft; NodeKind.DISCUSSION -> discussionSoft
        NodeKind.DECISION -> decisionSoft; NodeKind.TASK -> taskSoft; NodeKind.ARTIFACT -> artifactSoft
    }
    fun icon(kind: NodeKind): String = when(kind) {
        NodeKind.IDEA -> "light-bulb-idea.svg"; NodeKind.DISCUSSION -> "chat.svg"
        NodeKind.DECISION -> "check.svg"; NodeKind.TASK -> "task.svg"; NodeKind.ARTIFACT -> "File.svg"
    }
}
