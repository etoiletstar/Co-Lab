package com.colab.spatial.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

interface WorkspaceRepository {
    fun load(): WorkspaceState?
    fun save(state: WorkspaceState): Boolean
}

class LocalWorkspaceRepository(context: Context) : WorkspaceRepository {
    private val preferences = context.applicationContext.getSharedPreferences("colab_workspace_v1", Context.MODE_PRIVATE)
    override fun load(): WorkspaceState? = preferences.getString("snapshot", null)?.let(WorkspaceCodec::decode)
    override fun save(state: WorkspaceState): Boolean = preferences.edit().putString("snapshot", WorkspaceCodec.encode(state)).commit()
}

/** One application-owned state object, shared by all independent Android window Activities. */
class WorkspaceStore(private val repository: WorkspaceRepository) {
    var persistenceMessage by mutableStateOf("已保存在本机 · 单人模式")
        private set
    var state by mutableStateOf(runCatching { repository.load() ?: WorkspaceSeed.create() }.getOrElse {
        persistenceMessage = "本地数据读取失败；原记录保留，当前使用示例。请先导出检查。"
        WorkspaceSeed.create()
    })
        private set
    private var readFailed = persistenceMessage.startsWith("本地数据读取失败")
    private val history = ArrayDeque<WorkspaceState>()
    val canUndo: Boolean get() = history.isNotEmpty()
    private fun id() = UUID.randomUUID().toString()

    fun update(recordUndo: Boolean = true, persist: Boolean = true, transform: (WorkspaceState) -> WorkspaceState) {
        val next = transform(state)
        if (next == state) return
        if (recordUndo) {
            history.addLast(state)
            while (history.size > 20) history.removeFirst()
        }
        state = next
        if (persist) save()
    }
    fun save() {
        if (readFailed) return // Do not overwrite the unreadable snapshot with demo data.
        persistenceMessage = if (repository.save(state)) "已保存在本机 · 单人模式" else "本机保存失败；请复制导出内容后重试"
    }
    fun select(id: String) = update(recordUndo = false) { WorkspaceOperations.select(it, id) }
    fun addIdea(title: String, body: String) = update { WorkspaceOperations.addIdea(it, id(), title, body) }
    fun edit(id: String, title: String, body: String) = update { WorkspaceOperations.edit(it, id, title, body) }
    fun comment(nodeId: String, text: String) = update { WorkspaceOperations.comment(it, nodeId, id(), text, System.currentTimeMillis()) }
    fun confirm(id: String) = update { WorkspaceOperations.confirmDecision(it, id) }
    fun makeTask(id: String) = update { WorkspaceOperations.makeTask(it, id) }
    fun toggleTask(id: String) = update { WorkspaceOperations.toggleTask(it, id) }
    fun move(id: String, x: Float, y: Float) = update(recordUndo = false, persist = false) { WorkspaceOperations.move(it, id, x, y) }
    fun saveArtifact(title: String, markdown: String, sourceId: String) = update {
        WorkspaceOperations.saveArtifact(it, id(), title, markdown, sourceId, System.currentTimeMillis())
    }
    fun undo() {
        if (history.isEmpty()) return
        state = history.removeLast().copy(revision = state.revision + 1)
        save()
    }
    fun exportMarkdown(): String = buildString {
        appendLine("# Co-Lab · 本机项目导出")
        appendLine("单人原型；示例项目；版本 ${state.revision}")
        state.nodes.forEach { node ->
            appendLine("\n## ${node.kind.label} · ${node.title}")
            appendLine(node.body)
            appendLine("状态：${if (node.completed) "已完成" else if (node.pending) "待人工确认" else "进行中"}；来源：${node.id}")
            node.comments.forEach { appendLine("- ${it.author}：${it.text}") }
        }
        state.artifacts.forEach { appendLine("\n---\n${it.markdown}") }
    }

    companion object {
        @Volatile private var instance: WorkspaceStore? = null
        fun get(context: Context): WorkspaceStore = instance ?: synchronized(this) {
            instance ?: WorkspaceStore(LocalWorkspaceRepository(context)).also { instance = it }
        }
    }
}
