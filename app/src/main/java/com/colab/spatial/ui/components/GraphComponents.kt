package com.colab.spatial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.colab.spatial.data.WorkspaceNode
import com.colab.spatial.ui.theme.CoLabPalette as Brand
import com.pico.spatial.ui.design.HorizontalDivider
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect

/** Rich graph record: intentionally a content layout, not a command Button. */
@Composable
fun GraphNode(
    node: WorkspaceNode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.clip(RoundedCornerShape(16.dp))
        // design-style: design-surface graph-node
        .background(Brand.paper).spatialHoverEffect()
        .pointerInput(node.id) {
            detectDragGesturesAfterLongPress(onDragStart = { onDragStart() }, onDragEnd = onDragEnd, onDragCancel = onDragEnd) { change, delta ->
                change.consume(); onDrag(delta)
            }
        }
        .clickable(role = Role.Button, onClickLabel = "打开节点讨论", onClick = onSelect)
        .semantics(mergeDescendants = true) {
            contentDescription = "${node.kind.label}：${node.title}${if (isSelected) "，已选中" else ""}"
            selected = isSelected
        }) {
        if (isSelected) Box(Modifier.width(4.dp).fillMaxHeight()
            // design-style: design-surface node-selection
            .background(Brand.brandBlue))
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                KindBadge(node.kind)
                if (node.completed) BrandIcon("check.svg", tint = Brand.decisionInk, description = "已完成")
                else if (node.pending) Text("待确认", style = PicoTheme.typography.bodySmall, color = Brand.ideaInk)
                else if (isSelected) BrandIcon("check.svg", tint = Brand.brandBlue, description = "已选中")
            }
            Text(node.title, style = PicoTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                color = Brand.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(node.body.ifBlank { "点击补充想法与依据" }, style = PicoTheme.typography.bodySmall,
                color = Brand.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthorAvatar(node.author, Modifier.size(22.dp))
                Text(node.author, style = PicoTheme.typography.bodySmall, color = Brand.muted)
                Spacer(Modifier.weight(1f))
                BrandIcon("chat.svg", Modifier.size(15.dp), tint = Brand.muted)
                Text("${node.comments.size}", style = PicoTheme.typography.bodySmall, color = Brand.muted)
            }
        }
    }
}
