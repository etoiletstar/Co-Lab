package com.colab.spatial.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.colab.spatial.data.*
import com.colab.spatial.ui.components.BrandIcon
import com.colab.spatial.ui.theme.CoLabPalette as Brand
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import kotlinx.coroutines.launch

/**
 * 独立空间窗口：飞书协同（Windows.Feishu）
 * 100% 沿用 Co-Lab 原生 SpatialUI 组件体系，零侵入扩展：
 * - 同屏唤起飞书客户端 / 飞书会议 / 飞书云文档（共享空间下保留全部 Co-Lab 窗口）
 * - 一键将选中节点或整份项目共识推送为飞书群交互卡片（Webhook）
 * - 将飞书会议纪要/群聊讨论一键沉淀为世界画布节点
 */
@Composable
fun FeishuPage() {
    val context = LocalContext.current
    val store = WorkspaceStore.get(context)
    val state = store.state
    val selected = state.selected
    val navigator = LocalSpatialNavigator.current
    val scope = rememberCoroutineScope()

    val saved = remember { FeishuBridge.loadSettings(context) }
    var webhookUrl by rememberSaveable { mutableStateOf(saved.webhookUrl) }
    var docOrMeetingUrl by rememberSaveable { mutableStateOf(saved.docOrMeetingUrl) }
    var transcriptInput by rememberSaveable { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("共享空间模式已就绪：唤起飞书时，工作台与世界画布全部保留") }
    var isPushing by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TitleBar(title = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                BrandIcon("chat.svg", tint = Brand.brandBlue)
                Text("飞书协同", style = PicoTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        })

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "共享空间同屏协同 · 唤起飞书窗口、推送画布共识到飞书群、导入飞书会议纪要",
                style = PicoTheme.typography.bodySmall,
                color = PicoTheme.colorScheme.labelSecondary
            )

            // 状态反馈条
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brand.blueSoft)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandIcon("check.svg", Modifier.size(16.dp), tint = Brand.brandBlue)
                Text(statusMessage, style = PicoTheme.typography.bodySmall, color = Brand.navy)
            }

            // 模块 1：同屏唤起飞书客户端 / 飞书会议 / 飞书云文档
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PicoTheme.colorScheme.fillSecondary)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("1. 同屏唤起飞书窗口（多窗口共存）", style = PicoTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                TextField(
                    value = docOrMeetingUrl,
                    onValueChange = { docOrMeetingUrl = it },
                    modifier = Modifier.fillMaxWidth().testTag("飞书链接输入框"),
                    placeholder = { Text("输入飞书会议链接或云文档 URL（留空则打开飞书）") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            FeishuBridge.saveSettings(context, FeishuSettings(webhookUrl, docOrMeetingUrl))
                            statusMessage = FeishuBridge.launchFeishuWindow(context, docOrMeetingUrl)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand.brandBlue,
                            contentColor = PicoTheme.colorScheme.labelPrimaryLight
                        ),
                        leadingIcon = { BrandIcon("arrow-expand.svg") }
                    ) { Text("打开飞书会议 / 文档") }

                    Button(
                        onClick = {
                            statusMessage = FeishuBridge.launchFeishuWindow(context, null)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PicoTheme.colorScheme.fillLight,
                            contentColor = PicoTheme.colorScheme.labelPrimary
                        )
                    ) { Text("唤起飞书 App") }
                }
            }

            // 模块 2：推送 Co-Lab 画布共识到真实飞书群（Webhook）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PicoTheme.colorScheme.fillSecondary)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2. 推送共识卡片到飞书群", style = PicoTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Chip(label = { Text(selected?.let { "当前：${it.title.take(10)}" } ?: "整项目汇总") }, onClick = null)
                }
                TextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    modifier = Modifier.fillMaxWidth().testTag("飞书Webhook输入框"),
                    placeholder = { Text("粘贴飞书群机器人 Webhook（https://open.feishu.cn/open-apis/bot/v2/hook/...）") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        enabled = !isPushing && selected != null,
                        onClick = {
                            val node = selected ?: return@Button
                            FeishuBridge.saveSettings(context, FeishuSettings(webhookUrl, docOrMeetingUrl))
                            isPushing = true
                            statusMessage = "正在推送节点「${node.title}」到飞书群…"
                            scope.launch {
                                val md = buildString {
                                    appendLine("**【${node.kind.label}】${node.title}**")
                                    appendLine(node.body.ifBlank { "（暂无正文说明）" })
                                    appendLine("作者：${node.author} ｜ 状态：${if (node.completed) "已完成" else if (node.pending) "待人工确认" else "进行中"}")
                                    if (node.comments.isNotEmpty()) {
                                        appendLine("\n**最新讨论：**")
                                        node.comments.takeLast(3).forEach { c -> appendLine("- ${c.author}：${c.text}") }
                                    }
                                }
                                val res = FeishuBridge.pushCardToFeishuGroup(
                                    webhookUrl = webhookUrl,
                                    title = "节点共识同步",
                                    markdownContent = md,
                                    projectSummary = "${state.nodes.size} 个画布节点 · ${state.completedTasks}/${state.taskCount} 任务完成"
                                )
                                statusMessage = res.message
                                isPushing = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand.brandBlue,
                            contentColor = PicoTheme.colorScheme.labelPrimaryLight
                        ),
                        leadingIcon = { BrandIcon("arrow-right.svg") }
                    ) { Text(if (isPushing) "推送中…" else "推送选中节点") }

                    Button(
                        enabled = !isPushing,
                        onClick = {
                            FeishuBridge.saveSettings(context, FeishuSettings(webhookUrl, docOrMeetingUrl))
                            isPushing = true
                            statusMessage = "正在汇总整个项目共识并推送到飞书群…"
                            scope.launch {
                                val decisions = state.nodes.filter { it.kind == NodeKind.DECISION }
                                val tasks = state.nodes.filter { it.kind == NodeKind.TASK }
                                val md = buildString {
                                    appendLine("**Co-Lab 空间项目进度同步**")
                                    appendLine("共 ${state.nodes.size} 个节点，${decisions.size} 项已确认决策，${state.completedTasks}/${tasks.size} 项任务已完成。")
                                    if (decisions.isNotEmpty()) {
                                        appendLine("\n**核心共识：**")
                                        decisions.take(4).forEach { appendLine("- ✅ ${it.title}") }
                                    }
                                    if (tasks.isNotEmpty()) {
                                        appendLine("\n**行动任务：**")
                                        tasks.take(4).forEach { appendLine("- ${if (it.completed) "☑" else "☐"} ${it.title}") }
                                    }
                                }
                                val res = FeishuBridge.pushCardToFeishuGroup(
                                    webhookUrl = webhookUrl,
                                    title = "全项目共识与进度播报",
                                    markdownContent = md,
                                    projectSummary = "版本 v${state.revision}"
                                )
                                statusMessage = res.message
                                isPushing = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PicoTheme.colorScheme.fillLight,
                            contentColor = PicoTheme.colorScheme.labelPrimary
                        )
                    ) { Text("推送整项目进度") }
                }
            }

            // 模块 3：飞书会议纪要 / 群聊讨论 → 一键沉淀到世界画布
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PicoTheme.colorScheme.fillSecondary)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3. 飞书会议纪要 → 沉淀到世界画布", style = PicoTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { transcriptInput = FeishuBridge.sampleFeishuTranscript },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PicoTheme.colorScheme.fillLight,
                            contentColor = PicoTheme.colorScheme.labelPrimary
                        )
                    ) { Text("填入示例纪要") }
                }
                TextField(
                    value = transcriptInput,
                    onValueChange = { transcriptInput = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp).testTag("飞书纪要输入框"),
                    placeholder = { Text("粘贴飞书妙记或群聊讨论（每行一条，自动识别「决定/待办/建议」并生成画布节点）") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        enabled = transcriptInput.isNotBlank(),
                        onClick = {
                            val entries = FeishuBridge.parseFeishuTranscriptToNodes(transcriptInput)
                            if (entries.isEmpty()) {
                                statusMessage = "未识别到有效讨论内容，请每行输入至少 4 个字"
                            } else {
                                entries.forEach { entry ->
                                    store.addIdea(entry.title, entry.body)
                                    val newId = store.state.selectedId
                                    when (entry.kind) {
                                        NodeKind.DECISION -> store.confirm(newId)
                                        NodeKind.TASK -> store.makeTask(newId)
                                        else -> {}
                                    }
                                }
                                transcriptInput = ""
                                statusMessage = "已将飞书讨论提炼为 ${entries.size} 个新节点写入世界画布！"
                                navigator.openWindowContainer(Windows.Canvas, tag = Windows.Canvas)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand.brandBlue,
                            contentColor = PicoTheme.colorScheme.labelPrimaryLight
                        ),
                        leadingIcon = { BrandIcon("ai_stars.svg") }
                    ) { Text("一键写入世界画布") }
                }
            }
        }
    }
}
