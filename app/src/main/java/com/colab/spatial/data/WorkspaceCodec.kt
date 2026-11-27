package com.colab.spatial.data

import org.json.JSONArray
import org.json.JSONObject

/** Versioned local snapshot; networking can implement the same WorkspaceRepository contract later. */
object WorkspaceCodec {
    fun encode(state: WorkspaceState): String = JSONObject().apply {
        put("schema", 1)
        put("revision", state.revision)
        put("selectedId", state.selectedId)
        put("nodes", JSONArray().apply { state.nodes.forEach { node -> put(JSONObject().apply {
            put("id", node.id); put("kind", node.kind.name); put("title", node.title)
            put("body", node.body); put("author", node.author); put("x", node.x); put("y", node.y)
            put("parentId", node.parentId ?: JSONObject.NULL)
            put("pending", node.pending); put("completed", node.completed)
            put("comments", JSONArray().apply { node.comments.forEach { comment -> put(JSONObject().apply {
                put("id", comment.id); put("author", comment.author); put("text", comment.text); put("createdAt", comment.createdAt)
            }) } })
        }) } })
        put("artifacts", JSONArray().apply { state.artifacts.forEach { artifact -> put(JSONObject().apply {
            put("id", artifact.id); put("title", artifact.title); put("markdown", artifact.markdown)
            put("sourceNodeId", artifact.sourceNodeId); put("createdAt", artifact.createdAt)
        }) } })
        put("activity", JSONArray(state.activity))
    }.toString()

    fun decode(raw: String): WorkspaceState {
        val root = JSONObject(raw)
        require(root.getInt("schema") == 1) { "不支持的本地数据版本" }
        val nodes = root.getJSONArray("nodes").mapObjects { item -> WorkspaceNode(
            id = item.getString("id"), kind = NodeKind.valueOf(item.getString("kind")),
            title = item.getString("title"), body = item.getString("body"), author = item.getString("author"),
            x = item.getDouble("x").toFloat(), y = item.getDouble("y").toFloat(),
            parentId = if (item.isNull("parentId")) null else item.getString("parentId"),
            comments = item.getJSONArray("comments").mapObjects { comment -> NodeComment(
                comment.getString("id"), comment.getString("author"), comment.getString("text"), comment.getLong("createdAt")
            ) }, pending = item.optBoolean("pending"), completed = item.optBoolean("completed")
        ) }
        require(nodes.isNotEmpty() && nodes.map { it.id }.distinct().size == nodes.size)
        require(nodes.all { it.id.isNotBlank() && it.title.isNotBlank() && it.x.isFinite() && it.y.isFinite() })
        val ids = nodes.map { it.id }.toSet()
        require(nodes.all { it.parentId == null || it.parentId in ids })
        val artifacts = root.getJSONArray("artifacts").mapObjects { item -> WorkspaceArtifact(
            item.getString("id"), item.getString("title"), item.getString("markdown"),
            item.getString("sourceNodeId"), item.getLong("createdAt")
        ) }
        require(artifacts.all { it.sourceNodeId in ids })
        val activities = root.getJSONArray("activity")
        return WorkspaceState(
            revision = root.getInt("revision"), selectedId = root.getString("selectedId").takeIf { it in ids } ?: nodes.first().id,
            nodes = nodes, artifacts = artifacts, activity = List(activities.length()) { activities.getString(it) }
        )
    }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { transform(getJSONObject(it)) }
}
