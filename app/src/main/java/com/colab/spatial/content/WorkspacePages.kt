package com.colab.spatial.content

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.colab.spatial.R
import com.colab.spatial.data.*
import com.colab.spatial.ui.components.*
import com.colab.spatial.ui.theme.CoLabPalette as Brand
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

object Windows {
    const val Workbench = "colab.workbench"
    const val Canvas = "colab.canvas"
    const val Details = "colab.details"
    const val Assistant = "colab.assistant"
    const val Artifacts = "colab.artifacts"
    const val Feishu = "colab.feishu"
}

@Composable private fun store(): WorkspaceStore = WorkspaceStore.get(LocalContext.current)

@Composable
private fun Page(title: String, subtitle: String, icon: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                BrandIcon(icon, tint = Brand.brandBlue)
                Text(title, style = PicoTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        })
        Column(Modifier.weight(1f).padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(subtitle, style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
            content()
        }
    }
}

@Composable
private fun Action(label: String, enabled: Boolean = true, secondary: Boolean = false, icon: String? = null, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled,
        colors = if (secondary) ButtonDefaults.buttonColors(containerColor = PicoTheme.colorScheme.fillSecondary,
            contentColor = PicoTheme.colorScheme.labelPrimary) else ButtonDefaults.buttonColors(
            containerColor = Brand.brandBlue, contentColor = PicoTheme.colorScheme.labelPrimaryLight),
        leadingIcon = icon?.let { { BrandIcon(it) } }, modifier = Modifier.testTag(label)
    ) { Text(label) }
}

@Composable
private fun SaveStatus(store: WorkspaceStore) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        BrandIcon("check.svg", Modifier.size(16.dp), tint = if (store.persistenceMessage.contains("失败")) PicoTheme.colorScheme.error else Brand.decisionInk)
        Text(store.persistenceMessage, style = PicoTheme.typography.bodySmall,
            color = if (store.persistenceMessage.contains("失败")) PicoTheme.colorScheme.error else PicoTheme.colorScheme.labelSecondary)
    }
}

@Composable
fun WorkbenchPage() {
    val store = store()
    val state = store.state
    val navigator = LocalSpatialNavigator.current
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        navigator.openWindowContainer(Windows.Canvas, tag = Windows.Canvas)
        navigator.openWindowContainer(Windows.Details, tag = Windows.Details)
    }
    Page("Co-Lab Spatial", "工作空间 / AI 创新协作平台 / 示例项目", "Branch.svg") {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            BrandHero(Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Metric("${state.nodes.size}", "画布节点")
                Metric("${state.completedTasks} / ${state.taskCount}", "任务已完成")
                Metric("${state.artifacts.size}", "项目产物")
                Chip(label = { Text("本机 · 单人") }, onClick = null)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("继续你的创作", style = PicoTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Action("展开完整工作区", icon = "arrow-expand.svg") {
                    listOf(Windows.Canvas, Windows.Details, Windows.Assistant, Windows.Artifacts).forEach {
                        navigator.openWindowContainer(it, tag = it)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WindowLaunchItem("项目画布", "把想法连接成结构", "Branch.svg",
                        { navigator.openWindowContainer(Windows.Canvas, tag = Windows.Canvas) }, Modifier.weight(1f))
                    WindowLaunchItem("节点讨论", "让依据沉淀为共识", "chat.svg",
                        { navigator.openWindowContainer(Windows.Details, tag = Windows.Details) }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WindowLaunchItem("整理草稿", "核对后再人工采纳", "ai_stars.svg",
                        { navigator.openWindowContainer(Windows.Assistant, tag = Windows.Assistant) }, Modifier.weight(1f))
                    WindowLaunchItem("项目产物", "保留成果与来源", "File.svg",
                        { navigator.openWindowContainer(Windows.Artifacts, tag = Windows.Artifacts) }, Modifier.weight(1f))
                }
                WindowLaunchItem("飞书协同", "同屏唤起飞书窗口 · 推送共识卡片 · 导入会议纪要", "chat.svg",
                    { navigator.openWindowContainer(Windows.Feishu, tag = Windows.Feishu) }, Modifier.fillMaxWidth())
            }
            HorizontalDivider()
            Text("最近动态", style = PicoTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            state.activity.take(3).forEach { event ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    BrandIcon("check.svg", Modifier.size(16.dp), tint = Brand.decisionInk)
                    Text(event, style = PicoTheme.typography.bodyMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action("撤销上次修改", enabled = store.canUndo, secondary = true) { store.undo() }
                Action(if (copied) "已复制 Markdown" else "复制项目 Markdown", secondary = true, icon = "File.svg") {
                    copy(context, store.exportMarkdown()); copied = true
                }
            }
        }
        SaveStatus(store)
    }
}

@Composable
fun CanvasPage() {
    val store = store()
    val state = store.state
    val navigator = LocalSpatialNavigator.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("全部") }
    var newTitle by rememberSaveable { mutableStateOf("") }
    var adding by rememberSaveable { mutableStateOf(false) }
    val horizontal = rememberScrollState()
    val vertical = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val graphGrid = Brand.grid
    val visibleNodes = state.nodes.filter { (filter == "全部" || it.kind.name == filter) &&
        (query.isBlank() || "${it.title} ${it.body}".contains(query, ignoreCase = true)) }
    LaunchedEffect(query, filter) {
        if (query.isNotBlank() || filter != "全部") visibleNodes.firstOrNull()?.let {
            horizontal.scrollTo(((it.x - 24).coerceAtLeast(0f) * density).roundToInt())
            vertical.scrollTo(((it.y - 24).coerceAtLeast(0f) * density).roundToInt())
        }
    }
    val points = remember(density) { buildList {
        for (x in 12..2140 step 24) for (y in 12..1700 step 24) add(Offset(x * density, y * density))
    } }
    Page("世界画布", "AI 创新协作平台 / 从一个想法，到可以行动的下一步", "Branch.svg") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SearchField(query, { query = it.take(120) }, onSearch = {}, modifier = Modifier.weight(1f).testTag("搜索节点"),
                placeholder = { Text("搜索观点、讨论或任务") })
            Action("新增观点", icon = "add.svg") { adding = !adding }
            Action("回到起点", secondary = true, icon = "arrow-expand.svg") {
                coroutineScope.launch { horizontal.scrollTo(0); vertical.scrollTo(0) }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ToggleableChip(label = { Text("全部 ${state.nodes.size}") }, isToggleOn = filter == "全部", onClick = { filter = "全部" })
            NodeKind.entries.filter { it != NodeKind.ARTIFACT }.forEach { kind ->
                ToggleableChip(label = { Text("${kind.label} ${state.nodes.count { it.kind == kind }}") },
                    isToggleOn = filter == kind.name, onClick = { filter = kind.name },
                    leadingIcon = { BrandIcon(Brand.icon(kind), Modifier.size(16.dp)) })
            }
        }
        if (adding) Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(newTitle, { newTitle = it.take(120) }, modifier = Modifier.weight(1f).testTag("新观点标题"),
                placeholder = { Text("输入观点标题，将关联到当前节点") })
            Action("创建观点", enabled = newTitle.isNotBlank()) {
                store.addIdea(newTitle, ""); newTitle = ""; adding = false; filter = "全部"; query = ""
                navigator.openWindowContainer(Windows.Details, tag = Windows.Details)
            }
        }
        if (visibleNodes.isEmpty()) Text("没有匹配的节点，试试其他关键词。", style = PicoTheme.typography.bodyMedium,
            color = PicoTheme.colorScheme.labelSecondary)
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(20.dp))
            // design-style: design-surface canvas-workarea
            .background(Brand.canvas).verticalScroll(vertical).horizontalScroll(horizontal)) {
            Box(Modifier.size(2140.dp, 1700.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    drawPoints(points, PointMode.Points, graphGrid, strokeWidth = 2 * density, cap = StrokeCap.Round)
                    visibleNodes.forEach { node ->
                        val parent = visibleNodes.firstOrNull { it.id == node.parentId }
                        if (parent != null) {
                            val start = Offset((parent.x + 150) * density, (parent.y + 208) * density)
                            val end = Offset((node.x + 150) * density, node.y * density)
                            val middleY = (start.y + end.y) / 2
                            val path = Path().apply { moveTo(start.x, start.y); cubicTo(start.x, middleY, end.x, middleY, end.x, end.y) }
                            drawPath(path, if (node.id == state.selectedId) Brand.brandBlue else graphGrid,
                                style = Stroke(width = 2 * density))
                        }
                    }
                }
                visibleNodes.forEach { node -> key(node.id) {
                    GraphNode(node, state.selectedId == node.id,
                        onSelect = { store.select(node.id); navigator.openWindowContainer(Windows.Details, tag = Windows.Details) },
                        onDragStart = { store.select(node.id) }, onDragEnd = { store.save() },
                        onDrag = { delta -> store.state.nodes.firstOrNull { it.id == node.id }?.let {
                            store.move(it.id, it.x + delta.x / density, it.y + delta.y / density)
                        } },
                        modifier = Modifier.offset { IntOffset((node.x * density).roundToInt(), (node.y * density).roundToInt()) }
                            .size(300.dp, 208.dp).testTag("node.${node.id}"))
                } }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("长按拖动 · 点选后在独立窗口讨论", style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
            Text("${visibleNodes.size} / ${state.nodes.size} 个节点 · 本机保存", style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
        }
    }
}

private data class EditDraft(val title: String, val body: String)
private val editDrafts = mutableStateMapOf<String, EditDraft>()
private val commentDrafts = mutableStateMapOf<String, String>()

@Composable
fun DetailsPage() {
    val store = store()
    val node = store.state.selected ?: return
    val navigator = LocalSpatialNavigator.current
    val draft = editDrafts[node.id] ?: EditDraft(node.title, node.body)
    val dirty = draft.title != node.title || draft.body != node.body
    val comment = commentDrafts[node.id].orEmpty()
    Page("节点讨论", "世界画布 / 当前选中节点", "chat.svg") {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                KindBadge(node.kind)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AuthorAvatar(node.author)
                    Text(node.author, style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
                    if (node.completed) Chip(label = { Text("已完成") }, onClick = null)
                    else if (node.pending) Chip(label = { Text("待人工确认") }, onClick = null)
                }
            }
            Text("把想法说清楚", style = PicoTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            TextField(draft.title, { editDrafts[node.id] = draft.copy(title = it.take(120)) },
                modifier = Modifier.fillMaxWidth().testTag("编辑节点标题").semantics { contentDescription = "节点标题" }, isError = draft.title.isBlank())
            TextArea(draft.body, { editDrafts[node.id] = draft.copy(body = it.take(12000)) },
                modifier = Modifier.fillMaxWidth().height(156.dp).testTag("编辑节点正文").semantics { contentDescription = "节点正文" }, maxLines = 6,
                placeholder = { Text("描述你的想法、问题或依据，让讨论可以继续。") })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action("保存修改", enabled = dirty && draft.title.isNotBlank(), icon = "check.svg") {
                    store.edit(node.id, draft.title, draft.body); editDrafts.remove(node.id)
                }
                if (dirty) Action("放弃编辑", secondary = true) { editDrafts.remove(node.id) }
            }
            if (dirty) Text("修改尚未保存；切换节点后可回来继续编辑。", style = PicoTheme.typography.bodySmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Action("确认为决策", enabled = node.kind != NodeKind.DECISION || node.pending, secondary = true, icon = "check.svg") { store.confirm(node.id) }
                Action(if (node.kind == NodeKind.TASK) { if (node.completed) "重新打开任务" else "完成任务" } else "转为任务", secondary = true, icon = "task.svg") {
                    if (node.kind == NodeKind.TASK) store.toggleTask(node.id) else store.makeTask(node.id)
                }
                Action("整理为草稿", secondary = true, icon = "ai_stars.svg") { navigator.openWindowContainer(Windows.Assistant, tag = Windows.Assistant) }
            }
            HorizontalDivider()
            Text("讨论 ${node.comments.size}", style = PicoTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (node.comments.isEmpty()) Text("先分享一条依据，或提出一个值得验证的问题。", style = PicoTheme.typography.bodyMedium, color = PicoTheme.colorScheme.labelSecondary)
            node.comments.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AuthorAvatar(item.author)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.author, style = PicoTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(item.text, style = PicoTheme.typography.bodyMedium)
                    }
                }
            }
            TextArea(comment, { commentDrafts[node.id] = it.take(4000) },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("讨论输入").semantics { contentDescription = "补充讨论" },
                placeholder = { Text("补充讨论…") }, maxLines = 3)
            Action("发布到本机讨论", enabled = comment.isNotBlank(), icon = "chat.svg") { store.comment(node.id, comment); commentDrafts.remove(node.id) }
        }
        SaveStatus(store)
    }
}

@Composable
fun AssistantPage() {
    val store = store()
    val navigator = LocalSpatialNavigator.current
    var sourceId by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var markdown by rememberSaveable { mutableStateOf("") }
    var saved by rememberSaveable { mutableStateOf(false) }
    Page("整理草稿", "本地规则整理 · 未调用在线模型 · 由你决定是否采纳", "ai_stars.svg") {
        if (markdown.isBlank()) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.agent_duo), "Co-Lab 蓝绿吉祥物", Modifier.size(240.dp, 190.dp))
                Text("把讨论，整理成下一步。", style = PicoTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("已有内容、讨论依据、待确认事项，\n整理为一份可以继续修改的纪要。", style = PicoTheme.typography.bodyLarge)
                Text("当前节点：${store.state.selected?.title.orEmpty()}", style = PicoTheme.typography.bodyMedium,
                    color = PicoTheme.colorScheme.labelSecondary)
            }
        } else {
            Text("草稿来源：${store.state.nodes.firstOrNull { it.id == sourceId }?.title.orEmpty()}",
                style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
            TextField(title, { title = it.take(120); saved = false }, Modifier.fillMaxWidth().testTag("产物标题").semantics { contentDescription = "产物标题" })
            TextArea(markdown, { markdown = it.take(24000); saved = false },
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("草稿正文").semantics { contentDescription = "草稿正文" }, showScrollIndicator = true)
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Action(if (markdown.isBlank()) "整理当前节点" else "重新整理当前节点", secondary = markdown.isNotBlank(), icon = "ai_stars.svg") {
                sourceId = store.state.selectedId
                title = "${store.state.selected?.title.orEmpty()} · 讨论纪要"
                markdown = WorkspaceOperations.localDraft(store.state); saved = false
            }
            if (markdown.isNotBlank()) Action(if (saved) "已保存到产物" else "人工采纳并保存",
                enabled = !saved && title.isNotBlank() && markdown.isNotBlank(), icon = "check.svg") {
                store.saveArtifact(title, markdown, sourceId); saved = true
                navigator.openWindowContainer(Windows.Artifacts, tag = Windows.Artifacts)
            }
            Action("查看产物", secondary = true, icon = "File.svg") { navigator.openWindowContainer(Windows.Artifacts, tag = Windows.Artifacts) }
        }
    }
}

@Composable
fun ArtifactsPage() {
    val store = store()
    val context = LocalContext.current
    val navigator = LocalSpatialNavigator.current
    var selectedId by rememberSaveable { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }
    val artifacts = store.state.artifacts
    val selected = artifacts.firstOrNull { it.id == selectedId } ?: artifacts.firstOrNull()
    Page("项目产物", "${artifacts.size} 份本机产物 · 每份内容保留来源节点", "File.svg") {
        if (selected == null) {
            Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.agent_qingning), "Co-Lab 吉祥物", Modifier.size(160.dp))
                Text("让讨论留下成果", style = PicoTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("从画布选中一个节点，核对整理草稿，\n再把值得保留的内容采纳到这里。", style = PicoTheme.typography.bodyLarge)
            }
            Action("打开整理草稿", icon = "ai_stars.svg") { navigator.openWindowContainer(Windows.Assistant, tag = Windows.Assistant) }
        } else {
            if (artifacts.size > 1) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                artifacts.forEach { artifact ->
                    ToggleableChip(label = { Text(artifact.title.take(18)) }, isToggleOn = selected.id == artifact.id,
                        onClick = { selectedId = artifact.id; copied = false })
                }
            }
            SelectionContainer(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(20.dp))
                // design-style: design-surface reading-paper
                .background(Brand.paper).verticalScroll(rememberScrollState())) {
                DocumentBody(selected.markdown, Modifier.fillMaxWidth().padding(28.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(if (copied) "已复制" else "复制产物 Markdown", icon = "File.svg") { copy(context, selected.markdown); copied = true }
                Action("回到来源节点", secondary = true, icon = "Branch.svg") {
                    store.select(selected.sourceNodeId); navigator.openWindowContainer(Windows.Details, tag = Windows.Details)
                }
            }
        }
    }
}

private fun copy(context: Context, value: String) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Co-Lab Markdown", value))
}
