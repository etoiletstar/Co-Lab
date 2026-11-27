package com.colab.spatial.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colab.spatial.R
import com.colab.spatial.ui.components.BrandIcon

/**
 * WorkSpace frame (Web 1920 17), laid out with the exact coordinates from the SVG.
 * The conversation is a fixed local sample; nothing here calls an online model.
 */
private val sidebarFill = Color.Black.copy(alpha = 0.23f) // design-style: fixed-figma-color Web 1920 17 sidebar layer
private val bubbleStroke = Color.White.copy(alpha = 0.26f) // design-style: fixed-figma-color Web 1920 17 bubble border

private val sampleTasks = listOf("Co-Lab Spatial Develop Debug", "Co-Lab Spatial Develop Debug", "Co-Lab Spatial Develop Debug")

private val agentBullets = listOf(
    "· 把杂乱信息整理成结论：把访谈、观察记录和讨论内容整理成可验证",
    "的结论，明确哪些有依据、哪些还只是猜测",
    "· 评估实现成本：拆解任务，评估工作量、依赖和排期风险",
    "· 看技术约束与风险：指出方案会受什么限制、哪些地方容易出问题",
)

@Composable
fun ChatWorkspaceScreen(onSendIdea: (String) -> Unit, onOpenFeishu: () -> Unit, onOpenCanvas: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var draft by rememberSaveable { mutableStateOf("") }

    // panel + darker sidebar
    GlassPanel(243, 163, 1434, 755, 57) {
    Box(Modifier.at(243, 163, 361, 755).clip(RoundedCornerShape(topStart = 57.dp, bottomStart = 57.dp)).background(sidebarFill))

    // sidebar
    DText("当前空间", 30, bold = true, modifier = Modifier.offset(288.dp, 210.dp))
    DText("Co-Lab Spatial WorkSpace", 18, alpha = 0.53f, modifier = Modifier.offset(288.dp, 245.dp))
    Row(Modifier.at(288, 282, 273, 43).glass(22, ChipFill).clickable { selected = 0 }.testTag("新建工作任务"),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        BrandIcon("File.svg", Modifier.size(16.dp), tint = Color.White)
        Spacer(Modifier.width(8.dp))
        DText("新建工作任务", 15, bold = true)
    }
    sampleTasks.forEachIndexed { i, title ->
        val top = 350 + i * 69
        Box(Modifier.at(266, top, 316, 69).let { if (i == selected) it.glass(22, ChipFill) else it }
            .clickable { selected = i }.testTag("任务$i")) {
            DText(title, 16, bold = true, modifier = Modifier.offset(21.dp, 12.dp))
            DText("Work Task: November 26, 2026", 11, alpha = 0.85f, modifier = Modifier.offset(21.dp, 36.dp))
        }
    }

    // user bubble
    Box(Modifier.at(671, 225, 279, 74).glass(30).border(1.dp, bubbleStroke, RoundedCornerShape(30.dp))) {
        Image(painterResource(R.drawable.spatial_agent), null, Modifier.offset(27.dp, 12.dp).size(25.dp))
        DText("任务指令", 12, bold = true, modifier = Modifier.offset(54.dp, 17.dp))
        DText("2026/11/26 17:27", 12, alpha = 0.8f, modifier = Modifier.offset(108.dp, 17.dp))
        DText("现在，请简单介绍一下你自己", 16, bold = true, modifier = Modifier.offset(27.dp, 40.dp))
    }

    // agent bubble
    Box(Modifier.at(671, 314, 503, 391).glass(30).border(1.dp, bubbleStroke, RoundedCornerShape(30.dp))) {
        Image(painterResource(R.drawable.spatial_agent), null, Modifier.offset(27.dp, 17.dp).size(25.dp))
        DText("海盐", 12, bold = true, modifier = Modifier.offset(54.dp, 22.dp))
        DText("2026/11/26 17:27", 12, alpha = 0.8f, modifier = Modifier.offset(82.dp, 22.dp))
        DText("你好，我是海盐", 12, bold = true, modifier = Modifier.offset(27.dp, 58.dp))
        DText("我主要帮 Co-Lab 做这几类事：", 12, bold = true, modifier = Modifier.offset(27.dp, 98.dp))
        agentBullets.forEachIndexed { i, line ->
            DText(line, 12, alpha = 0.85f, modifier = Modifier.offset(27.dp, (138 + i * 20).dp))
        }
        DText("你可以直接把材料发给我，例如访谈记录、需求草稿或方案思路；也可", 12, bold = true, modifier = Modifier.offset(27.dp, 238.dp))
        DText("以先说明目标，我们再一起确定下一步怎么做。", 12, bold = true, modifier = Modifier.offset(27.dp, 258.dp))
        DText("现在想从哪件事开始？", 12, bold = true, modifier = Modifier.offset(27.dp, 298.dp))
        Row(Modifier.offset(27.dp, 339.dp).size(147.dp, 31.dp).glass(16, ChipFill)
            .clickable { onSendIdea("海盐：把杂乱信息整理成结论") }.testTag("创建为观点节点"),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            BrandIcon("light-bulb-idea.svg", Modifier.size(13.dp), tint = Color.White)
            Spacer(Modifier.width(6.dp))
            DText("创建为 观点 节点", 12, bold = true)
        }
    }

    // composer
    Box(Modifier.at(671, 769, 905, 108).glass(30).border(1.dp, bubbleStroke, RoundedCornerShape(30.dp))) {
        Box(Modifier.offset(22.dp, 14.dp).size(700.dp, 24.dp)) {
            if (draft.isEmpty()) DText("描述任务目标,交付或者执行目标...", 16, alpha = 0.32f)
            BasicTextField(draft, { draft = it.take(200) }, Modifier.fillMaxSize().testTag("任务输入"), singleLine = true,
                cursorBrush = SolidColor(Color.White), textStyle = TextStyle(color = Color.White, fontSize = 16.sp))
        }
        DText("支持将附件拖拽至此处上传", 10, alpha = 0.32f, modifier = Modifier.offset(22.dp, 38.dp))
        Row(Modifier.offset(14.dp, 59.dp).size(166.dp, 37.dp).clip(RoundedCornerShape(14.dp)).background(ChipFill),
            verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(8.dp))
            Image(painterResource(R.drawable.spatial_agent), null, Modifier.size(25.dp))
            Spacer(Modifier.width(5.dp))
            DText("DeepSeek V4.1 Flash", 12)
        }
        Box(Modifier.offset(187.dp, 59.dp).size(37.dp).clip(RoundedCornerShape(14.dp)).background(ChipFill), contentAlignment = Alignment.Center) {
            Box(Modifier.size(14.dp).border(1.5.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White))
            }
        }
        Box(Modifier.offset(854.dp, 59.dp).size(37.dp).clip(RoundedCornerShape(14.dp)).background(ChipFill)
            .clickable { if (draft.isNotBlank()) { onSendIdea(draft); draft = "" } }.testTag("发送"),
            contentAlignment = Alignment.Center) {
            BrandIcon("arrow-right.svg", Modifier.size(16.dp), tint = Color.White)
        }
    }
    Box(Modifier.at(671, 888, 905, 18), contentAlignment = Alignment.Center) {
        DText("Agent 的输出可能不准确，请检查关键结论 · 会话内容仅项目成员可见", 11, alpha = 0.5f)
    }

    }
    // bottom tabs: Feishu Message | WorkSpace | Infinite canvas
    Box(Modifier.at(699, 947, 522, 50).glass(25, material = true)) {
        Box(Modifier.offset(176.dp, 3.dp).size(172.dp, 43.dp).clip(RoundedCornerShape(21.dp)).background(Color.Black.copy(alpha = 0.23f)))
        Box(Modifier.offset(0.dp, 0.dp).size(176.dp, 50.dp).clickable(onClick = onOpenFeishu).testTag("Feishu Message"),
            contentAlignment = Alignment.Center) { DText("Feishu Message", 15) }
        Box(Modifier.offset(176.dp, 0.dp).size(172.dp, 50.dp), contentAlignment = Alignment.Center) { DText("WorkSpace", 15, bold = true) }
        Box(Modifier.offset(348.dp, 0.dp).size(174.dp, 50.dp).clickable(onClick = onOpenCanvas).testTag("Infinite canvas"),
            contentAlignment = Alignment.Center) { DText("Infinite canvas", 15, bold = true) }
    }
}
