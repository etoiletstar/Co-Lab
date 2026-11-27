package com.colab.spatial.data

enum class NodeKind(val label: String) {
    IDEA("观点"), DISCUSSION("讨论"), DECISION("决策"), TASK("任务"), ARTIFACT("产物")
}

data class NodeComment(val id: String, val author: String, val text: String, val createdAt: Long)

data class WorkspaceNode(
    val id: String,
    val kind: NodeKind,
    val title: String,
    val body: String,
    val author: String,
    val x: Float,
    val y: Float,
    val parentId: String? = null,
    val comments: List<NodeComment> = emptyList(),
    val pending: Boolean = false,
    val completed: Boolean = false
)

data class WorkspaceArtifact(
    val id: String,
    val title: String,
    val markdown: String,
    val sourceNodeId: String,
    val createdAt: Long
)

data class WorkspaceState(
    val revision: Int = 1,
    val selectedId: String = "idea-root",
    val nodes: List<WorkspaceNode> = emptyList(),
    val artifacts: List<WorkspaceArtifact> = emptyList(),
    val activity: List<String> = emptyList()
) {
    val selected: WorkspaceNode? get() = nodes.firstOrNull { it.id == selectedId }
    val taskCount: Int get() = nodes.count { it.kind == NodeKind.TASK }
    val completedTasks: Int get() = nodes.count { it.kind == NodeKind.TASK && it.completed }
}

/** Pure domain operations. No network calls or implicit model-generated decisions. */
object WorkspaceOperations {
    fun select(state: WorkspaceState, id: String): WorkspaceState =
        if (state.nodes.any { it.id == id }) state.copy(selectedId = id) else state

    fun addIdea(state: WorkspaceState, id: String, title: String, body: String): WorkspaceState {
        if (title.isBlank() || state.nodes.any { it.id == id }) return state
        val freePosition = (0..6).flatMap { row -> (0..4).map { column -> 40f + column * 350f to 30f + row * 225f } }
            .firstOrNull { (x, y) -> state.nodes.none { kotlin.math.abs(it.x - x) < 320f && kotlin.math.abs(it.y - y) < 180f } }
            ?: (40f to 1380f)
        val node = WorkspaceNode(
            id = id, kind = NodeKind.IDEA, title = title.trim().take(120),
            body = body.trim().take(12000), author = "我", x = freePosition.first,
            y = freePosition.second, parentId = state.selectedId
        )
        return changed(state.copy(nodes = state.nodes + node, selectedId = id), "新增观点：${node.title}")
    }

    fun edit(state: WorkspaceState, id: String, title: String, body: String): WorkspaceState {
        if (title.isBlank()) return state
        val original = state.nodes.firstOrNull { it.id == id } ?: return state
        val edited = original.copy(title = title.trim().take(120), body = body.trim().take(12000))
        if (edited == original) return state
        return changed(state.copy(nodes = state.nodes.map { if (it.id == id) edited else it }), "编辑节点：${edited.title}")
    }

    fun comment(state: WorkspaceState, nodeId: String, id: String, text: String, now: Long): WorkspaceState {
        if (text.isBlank()) return state
        val node = state.nodes.firstOrNull { it.id == nodeId } ?: return state
        val comment = NodeComment(id, "我", text.trim().take(4000), now)
        return changed(
            state.copy(nodes = state.nodes.map { if (it.id == nodeId) it.copy(comments = it.comments + comment) else it }),
            "补充讨论：${node.title}"
        )
    }

    fun confirmDecision(state: WorkspaceState, nodeId: String): WorkspaceState {
        val node = state.nodes.firstOrNull { it.id == nodeId } ?: return state
        if (node.kind == NodeKind.DECISION && !node.pending) return state
        return changed(state.copy(nodes = state.nodes.map {
            if (it.id == nodeId) it.copy(kind = NodeKind.DECISION, pending = false) else it
        }), "人工确认决策：${node.title}")
    }

    fun makeTask(state: WorkspaceState, nodeId: String): WorkspaceState {
        val node = state.nodes.firstOrNull { it.id == nodeId } ?: return state
        if (node.kind == NodeKind.TASK) return state
        return changed(state.copy(nodes = state.nodes.map {
            if (it.id == nodeId) it.copy(kind = NodeKind.TASK, pending = false, completed = false) else it
        }), "转为任务：${node.title}")
    }

    fun toggleTask(state: WorkspaceState, nodeId: String): WorkspaceState {
        val node = state.nodes.firstOrNull { it.id == nodeId && it.kind == NodeKind.TASK } ?: return state
        return changed(state.copy(nodes = state.nodes.map {
            if (it.id == nodeId) it.copy(completed = !it.completed) else it
        }), if (node.completed) "重新打开任务：${node.title}" else "完成任务：${node.title}")
    }

    fun move(state: WorkspaceState, id: String, x: Float, y: Float): WorkspaceState {
        if (!x.isFinite() || !y.isFinite()) return state
        return state.copy(nodes = state.nodes.map {
            if (it.id == id) it.copy(x = x.coerceIn(0f, 1800f), y = y.coerceIn(0f, 1400f)) else it
        })
    }

    fun saveArtifact(state: WorkspaceState, id: String, title: String, markdown: String, sourceNodeId: String, now: Long): WorkspaceState {
        val source = state.nodes.firstOrNull { it.id == sourceNodeId } ?: return state
        if (title.isBlank() || markdown.isBlank() || state.artifacts.any { it.id == id }) return state
        val artifact = WorkspaceArtifact(id, title.trim().take(120), markdown.trim().take(24000), source.id, now)
        return changed(state.copy(artifacts = listOf(artifact) + state.artifacts), "保存产物：${artifact.title}")
    }

    fun localDraft(state: WorkspaceState): String {
        val node = state.selected ?: return "请选择一个画布节点。"
        return buildString {
            appendLine("# ${node.title}")
            appendLine()
            appendLine("> 本地规则整理草稿，未调用在线模型。请核对后再采纳。")
            appendLine()
            appendLine("## 已有内容")
            appendLine(node.body.ifBlank { "暂无正文，请先补充信息。" })
            appendLine()
            appendLine("## 当前讨论")
            if (node.comments.isEmpty()) appendLine("暂无讨论。")
            node.comments.forEach { appendLine("- ${it.author}：${it.text}") }
            appendLine()
            appendLine("## 待人工确认")
            appendLine("- 负责人、交付物和验收标准是否明确？")
            appendLine("- 哪些内容是事实，哪些仍需要验证？")
            appendLine()
            appendLine("来源节点：${node.id}；本地版本：${state.revision}")
        }
    }

    private fun changed(state: WorkspaceState, event: String): WorkspaceState =
        state.copy(revision = state.revision + 1, activity = (listOf(event) + state.activity).take(40))
}

/** Curated, explicitly labelled sample records from the supplied Co-Lab data.js. */
object WorkspaceSeed {
    fun create(): WorkspaceState = WorkspaceState(
        nodes = listOf(
            WorkspaceNode("idea-root", NodeKind.IDEA, "如何让不同专业的大学生更好地协作？",
                "跨专业课程与创新项目里，信息、分工和成果常常断裂。先收集真实摩擦点，再决定提供什么样的协作支持。",
                "陈默", 410f, 30f, comments = listOf(
                    NodeComment("seed-c1", "陈默", "先别急着定方案，我更想确认痛点是否真实存在。", 0),
                    NodeComment("seed-c2", "林知夏", "这周先约跨专业小组做访谈。", 0)
                )),
            WorkspaceNode("disc-needs", NodeKind.DISCUSSION, "用户需求研究",
                "通过访谈与问卷，弄清跨专业协作中最常见的断点：信息同步、分工确认、成果归属。",
                "林知夏", 40f, 255f, "idea-root"),
            WorkspaceNode("disc-agent", NodeKind.DISCUSSION, "Agent 个性化适配",
                "是否允许每位成员按专业与工作习惯配置自己的 Agent？需要同时评估体验收益与实现成本。",
                "周野", 410f, 255f, "idea-root"),
            WorkspaceNode("disc-collab", NodeKind.DISCUSSION, "多人协作机制",
                "如何避免上下文冲突、重复劳动，以及草稿被误当成团队共识？采纳动作必须由人完成。",
                "陈默", 780f, 255f, "idea-root"),
            WorkspaceNode("idea-challenge", NodeKind.IDEA, "个性化会不会增加上手成本？",
                "先验证默认即可用的路径，再逐步开放角色、语气和上下文配置。",
                "许一", 780f, 480f, "disc-collab", pending = true),
            WorkspaceNode("decision-adopt", NodeKind.DECISION, "默认先跑通，个性化渐进开放",
                "在默认配置基础上提供渐进式个性化。Agent 整理的内容先保存为草稿，由人决定是否采纳。",
                "陈默", 410f, 480f, "disc-agent"),
            WorkspaceNode("task-prototype", NodeKind.TASK, "完成工作区可操作原型",
                "覆盖画布、节点讨论与产物查看，验证跨窗口选中、编辑和本地保存。",
                "周野", 410f, 705f, "decision-adopt")
        ),
        activity = listOf("已加载 Co-Lab 示例项目；本机单人模式")
    )
}
