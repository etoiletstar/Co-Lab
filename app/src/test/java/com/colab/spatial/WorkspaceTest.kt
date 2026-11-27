package com.colab.spatial

import com.colab.spatial.data.*
import org.junit.Assert.*
import org.junit.Test

class WorkspaceTest {
    @Test fun snapshotRoundTripRetainsRelationshipsCommentsAndArtifacts() {
        var state = WorkspaceSeed.create()
        state = WorkspaceOperations.comment(state, "idea-root", "comment-1", "验收：3 个窗口同步", 100)
        state = WorkspaceOperations.saveArtifact(state, "artifact-1", "纪要", WorkspaceOperations.localDraft(state), "idea-root", 200)
        assertEquals(state, WorkspaceCodec.decode(WorkspaceCodec.encode(state)))
    }

    @Test fun decisionNeedsExplicitHumanAction() {
        val before = WorkspaceSeed.create().let { WorkspaceOperations.select(it, "idea-challenge") }
        val draft = WorkspaceOperations.localDraft(before)
        assertTrue(draft.contains("未调用在线模型"))
        assertTrue(before.selected!!.pending)
        assertEquals(NodeKind.IDEA, before.selected!!.kind)
        val after = WorkspaceOperations.confirmDecision(before, before.selectedId)
        assertEquals(NodeKind.DECISION, after.selected!!.kind)
        assertFalse(after.selected!!.pending)
    }

    @Test fun artifactKeepsDraftSourceWhenAnotherWindowChangesSelection() {
        val original = WorkspaceSeed.create()
        val draft = WorkspaceOperations.localDraft(original)
        val changedSelection = WorkspaceOperations.select(original, "task-prototype")
        val after = WorkspaceOperations.saveArtifact(changedSelection, "a1", "纪要", draft, original.selectedId, 100)
        assertEquals("idea-root", after.artifacts.single().sourceNodeId)
        assertEquals("task-prototype", after.selectedId)
    }

    @Test fun taskCompletionIsReversibleAndCountsAreDerived() {
        val before = WorkspaceSeed.create()
        val completed = WorkspaceOperations.toggleTask(before, "task-prototype")
        assertEquals(1, completed.completedTasks)
        assertEquals(1, completed.taskCount)
        assertEquals(0, WorkspaceOperations.toggleTask(completed, "task-prototype").completedTasks)
    }

    @Test fun blankAndDuplicateInputsNeverCreateBrokenRecords() {
        val state = WorkspaceSeed.create()
        assertEquals(state, WorkspaceOperations.addIdea(state, "n1", "   ", ""))
        assertEquals(state, WorkspaceOperations.addIdea(state, "idea-root", "重复", ""))
        assertEquals(state, WorkspaceOperations.edit(state, "idea-root", "", "正文"))
        assertEquals(state, WorkspaceOperations.comment(state, "idea-root", "c1", "  ", 0))
        assertEquals(state, WorkspaceOperations.saveArtifact(state, "a1", "纪要", "内容", "missing", 0))
    }

    @Test fun newIdeaIsLinkedToSelectionAndCoordinatesStayFinite() {
        val before = WorkspaceSeed.create().let { WorkspaceOperations.select(it, "disc-collab") }
        val after = WorkspaceOperations.addIdea(before, "new-idea", "  合并讨论  ", "  先验证  ")
        assertEquals("disc-collab", after.selected!!.parentId)
        assertEquals("合并讨论", after.selected!!.title)
        assertEquals(after, WorkspaceOperations.move(after, "new-idea", Float.NaN, 10f))
        val moved = WorkspaceOperations.move(after, "new-idea", -100f, 9999f).selected!!
        assertEquals(0f, moved.x, 0f)
        assertEquals(1400f, moved.y, 0f)
    }

    @Test fun restartLoadsSavedStateAndUndoPersists() {
        val repository = MemoryRepository()
        val first = WorkspaceStore(repository)
        first.comment("idea-root", "重启后这条讨论仍然存在")
        val restored = WorkspaceStore(repository)
        assertEquals(first.state, restored.state)
        first.makeTask("disc-needs")
        assertEquals(2, first.state.taskCount)
        first.undo()
        assertEquals(1, first.state.taskCount)
        assertEquals(first.state, WorkspaceStore(repository).state)
    }

    @Test fun unreadableSnapshotIsNeverSilentlyOverwritten() {
        val repository = MemoryRepository(failLoad = true)
        val store = WorkspaceStore(repository)
        store.addIdea("临时观点", "")
        assertEquals(0, repository.saves)
        assertTrue(store.persistenceMessage.contains("读取失败"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownSchemaIsRejected() {
        WorkspaceCodec.decode(WorkspaceCodec.encode(WorkspaceSeed.create()).replace("\"schema\":1", "\"schema\":2"))
    }

    private class MemoryRepository(private val failLoad: Boolean = false) : WorkspaceRepository {
        var raw: String? = null
        var saves = 0
        override fun load(): WorkspaceState? {
            if (failLoad) error("bad snapshot")
            return raw?.let(WorkspaceCodec::decode)
        }
        override fun save(state: WorkspaceState): Boolean {
            raw = WorkspaceCodec.encode(state); saves++; return true
        }
    }
}
