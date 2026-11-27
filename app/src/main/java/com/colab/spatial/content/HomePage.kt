package com.colab.spatial.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colab.spatial.R
import com.colab.spatial.data.WorkspaceStore
import com.colab.spatial.ui.components.BrandIcon
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator

/** Static sample content from the home design (single-user prototype, no backend). */
private data class HomeProject(val status: String, val title: String, val summary: String, val presence: String)

private val homeProjects = List(3) {
    HomeProject("推进中", "AI 校园导览助手",
        "把校园地图、课程信息与真实经验整理合成一个可以对话的导览助手，先服务本校新生，再考虑开放给其他高校。",
        "陈野等 在线")
}

private const val SCREEN_WELCOME = 0
private const val SCREEN_HOME = 1
private const val SCREEN_GUIDED = 2

/** Root of the main window: welcome/login -> home -> My Flow guided questions. */
@Composable
fun HomePage() {
    var screen by rememberSaveable { mutableIntStateOf(SCREEN_WELCOME) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var pendingIdea by rememberSaveable { mutableStateOf("") }
    val navigator = LocalSpatialNavigator.current
    val store = WorkspaceStore.get(LocalContext.current)
    val openProject = {
        navigator.openWindowContainer(Windows.Canvas, tag = Windows.Canvas)
        navigator.openWindowContainer(Windows.Details, tag = Windows.Details)
    }
    DesignStage {
        when (screen) {
            SCREEN_WELCOME -> WelcomeScreen(onEnter = { screen = SCREEN_HOME })
            SCREEN_GUIDED -> GuidedQuestionsScreen(onBack = { screen = SCREEN_HOME }, onFinish = {
                if (pendingIdea.isNotBlank()) store.addIdea(pendingIdea, "")
                pendingIdea = ""; screen = SCREEN_HOME; openProject()
            })
            else -> HomeScreen(tab, { tab = it }, openProject,
                onExplore = { text -> pendingIdea = text; screen = SCREEN_GUIDED },
                onAgent = { navigator.openWindowContainer(Windows.Assistant, tag = Windows.Assistant) },
                onSendIdea = { text -> store.addIdea(text, ""); openProject() },
                onFeishu = { navigator.openWindowContainer(Windows.Feishu, tag = Windows.Feishu) },
                onCanvas = { navigator.openWindowContainer(Windows.Canvas, tag = Windows.Canvas) })
        }
    }
}

@Composable
private fun HomeScreen(tab: Int, onTab: (Int) -> Unit, openProject: () -> Unit, onExplore: (String) -> Unit, onAgent: () -> Unit,
                       onSendIdea: (String) -> Unit, onFeishu: () -> Unit, onCanvas: () -> Unit) {
    AdminPill(Modifier.at(243, 86, 150, 62))
    HomeTabs(tab, onTab, Modifier.at(740, 86, 439, 62))
    TeamPill(Modifier.at(1463, 86, 214, 62), openProject)
    if (tab == 1) {
        ChatWorkspaceScreen(onSendIdea, onFeishu, onCanvas)
        return
    }
    GlassPanel(243, 162, 1434, 757, 57) {
    Image(painterResource(R.drawable.home_create_with), "Create With 说出你的想法，让它逐渐成形",
        Modifier.at(713, 197, 494, 142), contentScale = ContentScale.Fit)
    IdeaBar(Modifier.at(328, 373, 981, 85), onExplore)
    InspiredPill(Modifier.at(1325, 373, 267, 85)) { onExplore("") }
    DText("正在跟进的项目", 24, bold = true, modifier = Modifier.offset(303.dp, 493.dp))
    DText("当前计划上限 4/12", 16, alpha = 0.62f, modifier = Modifier.offset(303.dp, 526.dp))
    NewProjectCard(Modifier.at(302, 563, 112, 171), openProject)
    homeProjects.forEachIndexed { i, p -> ProjectCard(p, Modifier.at(429 + i * 403, 563, 382, 171), openProject) }
    }
    AgentPill(Modifier.at(243, 943, 298, 86), onAgent)
}

@Composable
fun AdminPill(modifier: Modifier) {
    Row(modifier.glass(31, material = true), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(40.dp).clip(CircleShape).background(AvatarBlue), contentAlignment = Alignment.Center) {
            DText("管", 13, bold = true)
        }
        Spacer(Modifier.width(10.dp))
        DText("· 管理员", 18, bold = true)
    }
}

@Composable
private fun TeamPill(modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.glass(31, material = true).clickable(onClick = onClick).testTag("进入团队协作空间"), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(21.dp))
        AvatarStack(28)
        Spacer(Modifier.width(12.dp))
        Column {
            DText("进入团队协作空间", 14, bold = true)
            DText("在线 2", 11, alpha = 0.62f)
        }
    }
}

@Composable
private fun HomeTabs(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier) {
    Box(modifier.glass(31, material = true)) {
        listOf("Creative Hub", "WorkSpace").forEachIndexed { index, label ->
            val on = index == selected
            Box(Modifier.offset((6 + index * 214).dp, 5.dp).size(214.dp, 53.dp).clip(RoundedCornerShape(27.dp))
                .background(if (on) DarkButton else Color.Transparent)
                .clickable { onSelect(index) }.testTag(label), contentAlignment = Alignment.Center) {
                DText(label, 19, bold = on)
            }
        }
    }
}

@Composable
private fun IdeaBar(modifier: Modifier, onExplore: (String) -> Unit) {
    var idea by rememberSaveable { mutableStateOf("") }
    Box(modifier.glass(43)) {
        Box(Modifier.offset(49.dp, 20.dp).size(780.dp, 30.dp)) {
            if (idea.isEmpty()) DText("在此处键入你的想法，让创意发生", 20)
            BasicTextField(idea, { idea = it.take(120) }, Modifier.fillMaxSize().testTag("首页想法输入"),
                singleLine = true, cursorBrush = SolidColor(Color.White),
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp))
        }
        DText("DeepSeek V4.1 Flash 1017", 14, alpha = 0.5f, modifier = Modifier.offset(49.dp, 49.dp))
        Row(Modifier.offset(836.dp, 22.dp).size(123.dp, 42.dp).clip(RoundedCornerShape(21.dp)).background(DarkButton)
            .clickable { onExplore(idea); idea = "" }.testTag("开始探索"),
            verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(18.dp))
            BrandIcon("arrow-right.svg", Modifier.size(20.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
            DText("开始探索", 14, bold = true)
        }
    }
}

/** Glass "Inspired by My Flow" pill; rebuilt natively because the supplied artwork is a flat blue pill. */
@Composable
private fun InspiredPill(modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.glass(43).clickable(onClick = onClick).testTag("My Flow")) {
        DText("Inspired by", 20, modifier = Modifier.offset(26.dp, 15.dp))
        DText("My Flow", 24, bold = true, modifier = Modifier.offset(26.dp, 40.dp))
        Image(painterResource(R.drawable.agent_duo), null, Modifier.offset(160.dp, 12.dp).size(88.dp, 62.dp),
            contentScale = ContentScale.Fit)
    }
}

@Composable
private fun NewProjectCard(modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.glass(22, CardFill).clickable(onClick = onClick).testTag("新建项目")) {
        Image(painterResource(R.drawable.agent_duo), null, Modifier.offset(16.dp, 22.dp).size(80.dp, 64.dp),
            contentScale = ContentScale.Fit)
        DText("Create", 16, bold = true, lineHeight = 1f, modifier = Modifier.offset(17.dp, 120.dp))
        DText("New Project", 16, lineHeight = 1f, modifier = Modifier.offset(17.dp, 136.dp))
    }
}

@Composable
private fun ProjectCard(project: HomeProject, modifier: Modifier, onEnter: () -> Unit) {
    Box(modifier.glass(22, CardFill)) {
        DText("· ${project.status}", 16, bold = true, modifier = Modifier.offset(25.dp, 17.dp))
        DText(project.title, 24, bold = true, modifier = Modifier.offset(25.dp, 42.dp))
        DText(project.summary, 12, alpha = 0.62f, maxLines = 2, lineHeight = 1.5f,
            modifier = Modifier.offset(25.dp, 80.dp).width(334.dp))
        Box(Modifier.offset(25.dp, 124.dp)) { AvatarStack(28) }
        DText(project.presence, 12, bold = true, modifier = Modifier.offset(97.dp, 130.dp))
        Box(Modifier.offset(276.dp, 120.dp).size(96.dp, 39.dp).clip(RoundedCornerShape(10.dp)).background(DarkButton)
            .clickable(onClick = onEnter).testTag("进入项目"), contentAlignment = Alignment.Center) {
            DText("进入项目", 14, bold = true, color = LinkBlue)
        }
    }
}

@Composable
private fun AgentPill(modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.glass(43, material = true).clickable(onClick = onClick).testTag("海盐助手"), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(36.dp))
        Image(painterResource(R.drawable.spatial_agent), "海盐助手", Modifier.size(47.dp, 58.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(14.dp))
        Column {
            DText("试着问问海盐...", 20, bold = true)
            DText("YOUR AGENT", 12, alpha = 0.62f)
        }
    }
}
