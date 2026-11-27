package com.colab.spatial.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.colab.spatial.R
import com.colab.spatial.ui.components.BrandIcon

/**
 * "My Flow Guided Questions" frame. Only a screenshot of this design was supplied, so every size below was
 * measured from it (converted to the 1920 x 1080 design space). Questions and cards are fixed local sample
 * content; no online model is called.
 */
private data class GuideQuestion(val title: String, val hint: String)

private val guideQuestions = listOf(
    GuideQuestion("这个案例问答 Agent 主要服务谁？", "明确使用者，才能决定回答的深度与用语"),
    GuideQuestion("你希望它优先回答哪类问题？", "问题范围决定需要沉淀哪些资料与依据"),
    GuideQuestion("你希望这个案例问答 Agent 呈现什么气质？", "风格决定检索结果如何被组织与表达，也影响团队的使用信任感"),
    GuideQuestion("回答是否必须附上来源？", "来源可追溯，团队才能放心采纳结论"),
    GuideQuestion("有哪些内容不应该由它回答？", "划清边界，避免输出超出团队共识的内容"),
)

private val cardColors = listOf(
    Color(0xFFE03A5F), // design-style: fixed-figma-color guided card red
    Color(0xFF7B45E8), // design-style: fixed-figma-color guided card purple
    Color(0xFF16B055), // design-style: fixed-figma-color guided card green
    Color(0xFF2F80ED), // design-style: fixed-figma-color guided card blue
)
private val cardOrigins = listOf(392 to 242, 884 to 242, 540 to 466, 1032 to 466)
private const val CARD_BODY = "先立一套色彩、字体、间距与组件规则，让所有界面与宣传物料共用同一套语言。"

@Composable
fun GuidedQuestionsScreen(onBack: () -> Unit, onFinish: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(2) } // 0-based, opens at 3/5 like the design
    var shift by rememberSaveable { mutableIntStateOf(0) }
    val question = guideQuestions[step]

    // top bar
    Box(Modifier.at(666, 86, 62, 62).glass(31, material = true).clickable(onClick = onBack).testTag("返回"), contentAlignment = Alignment.Center) {
        BrandIcon("arrow-right.svg", Modifier.size(22.dp).rotate(180f), tint = Color.White)
    }
    AdminPill(Modifier.at(742, 86, 149, 62))
    Row(Modifier.at(907, 86, 347, 62).glass(31, material = true), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        DText("My Flow ", 20, bold = true)
        DText("Guided Questions ${step + 1}/${guideQuestions.size}", 20)
    }

    // direction cards (colors rotate on regenerate)
    cardOrigins.forEachIndexed { i, (x, y) ->
        DirectionCard(cardColors[(i + shift) % cardColors.size], Modifier.at(x, y, 473, 207))
    }
    FileBadge("P", ".pptx", Color(0xFFFF5A2E), Color(0xFFFFA58C), Modifier.at(1386, 270, 136, 153)) // design-style: fixed-figma-color pptx badge
    FileBadge("W", ".docx", Color(0xFF2F80ED), Color(0xFFA5CBFF), Modifier.at(371, 495, 136, 153)) // design-style: fixed-figma-color docx badge

    Box(Modifier.at(903, 728, 112, 36).glass(18, material = true).clickable { shift = (shift + 1) % cardColors.size }.testTag("重新生成"),
        contentAlignment = Alignment.Center) {
        DText("↻  重新生成", 13, bold = true)
    }

    // question panel
    Box(Modifier.at(508, 782, 904, 156).glass(30, material = true)) {
        DText(question.title, 20, bold = true, modifier = Modifier.offset(36.dp, 24.dp))
        DText(question.hint, 15, alpha = 0.45f, modifier = Modifier.offset(36.dp, 58.dp))
        Row(Modifier.offset(35.dp, 101.dp).size(164.dp, 36.dp).clip(RoundedCornerShape(14.dp)).background(ChipFill),
            verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(12.dp))
            Image(painterResource(R.drawable.spatial_agent), null, Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            DText("DeepSeek V4.1 Flash", 13)
        }
        Box(Modifier.offset(205.dp, 101.dp).size(36.dp).clip(RoundedCornerShape(14.dp)).background(ChipFill),
            contentAlignment = Alignment.Center) {
            Box(Modifier.size(14.dp).border(1.5.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White))
            }
        }
        Row(Modifier.offset(778.dp, 108.dp).size(113.dp, 36.dp).clip(RoundedCornerShape(14.dp)).background(ChipFill)
            .clickable { if (step < guideQuestions.lastIndex) step++ else onFinish() }.testTag("下一步"),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            DText(if (step < guideQuestions.lastIndex) "${step + 1}/${guideQuestions.size} 下一步" else "完成", 12, bold = true)
            Spacer(Modifier.width(6.dp))
            BrandIcon("arrow-right.svg", Modifier.size(14.dp), tint = Color.White)
        }
    }
    Box(Modifier.at(508, 950, 904, 18), contentAlignment = Alignment.Center) {
        DText("Agent 的输出可能不准确，请检查关键结论 · 会话内容仅项目成员可见", 11, alpha = 0.5f)
    }
}

@Composable
private fun DirectionCard(color: Color, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(35.dp)).background(color)) {
        DText("Spark ideas", 26, bold = true, modifier = Modifier.offset(37.dp, 24.dp))
        DText("创作方向", 22, bold = true, modifier = Modifier.offset(350.dp, 27.dp))
        DText("统一规范手册", 24, bold = true, modifier = Modifier.offset(37.dp, 110.dp))
        DText(CARD_BODY, 13, alpha = 0.92f, lineHeight = 1.5f, maxLines = 2, modifier = Modifier.offset(37.dp, 145.dp).width(400.dp))
    }
}

@Composable
private fun FileBadge(letter: String, ext: String, color: Color, cap: Color, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(color)) {
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomEnd = 60.dp))
            .background(cap))
        DText(letter, 58, bold = true, lineHeight = 1f, modifier = Modifier.align(Alignment.Center).offset(0.dp, (-6).dp))
        DText(ext, 20, bold = true, lineHeight = 1f, modifier = Modifier.align(Alignment.BottomCenter).offset(0.dp, (-14).dp))
    }
}
