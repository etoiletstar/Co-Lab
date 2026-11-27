package com.colab.spatial.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class FeishuSettings(
    val webhookUrl: String = "",
    val docOrMeetingUrl: String = "https://www.feishu.cn/messenger/"
)

data class FeishuPushResult(
    val success: Boolean,
    val message: String
)

data class ParsedFeishuEntry(
    val title: String,
    val body: String,
    val kind: NodeKind
)

/**
 * 飞书双向空间协同桥接服务：
 * 1) 共享空间同屏唤起飞书 App / 飞书会议 / 飞书云文档（所有 Co-Lab 窗口保持可见）；
 * 2) 通过飞书自定义机器人 Webhook 实时推送 Co-Lab 画布节点/共识卡片到真实飞书群；
 * 3) 将飞书会议纪要 / 群聊讨论结构化拆解并写回 Co-Lab 空间画布。
 */
object FeishuBridge {
    private const val PREF_NAME = "colab_feishu_v1"
    private const val KEY_WEBHOOK = "webhook_url"
    private const val KEY_DOC_URL = "doc_meeting_url"
    private val FEISHU_PACKAGES = listOf("com.ss.android.lark", "com.larksuite.suite")

    fun loadSettings(context: Context): FeishuSettings {
        val sp = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return FeishuSettings(
            webhookUrl = sp.getString(KEY_WEBHOOK, "").orEmpty(),
            docOrMeetingUrl = sp.getString(KEY_DOC_URL, "https://www.feishu.cn/messenger/")
                .orEmpty()
                .ifBlank { "https://www.feishu.cn/messenger/" }
        )
    }

    fun saveSettings(context: Context, settings: FeishuSettings) {
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WEBHOOK, settings.webhookUrl.trim())
            .putString(KEY_DOC_URL, settings.docOrMeetingUrl.trim().ifBlank { "https://www.feishu.cn/messenger/" })
            .apply()
    }

    /**
     * 在共享空间中唤起飞书客户端或系统浏览器窗口（使用 FLAG_ACTIVITY_NEW_TASK，保留全部 Co-Lab 窗口并排共存）
     */
    fun launchFeishuWindow(context: Context, targetUrl: String? = null): String {
        val cleanUrl = targetUrl?.trim().orEmpty()
        // 1. 若用户指定了具体的飞书云文档 / 飞书会议 URL，优先拉起系统浏览器或支持该 DeepLink 的飞书窗口
        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
            }
            if (runCatching { context.startActivity(webIntent) }.isSuccess) {
                return "已在空间旁打开飞书窗口（原有 Co-Lab 窗口全部保留）"
            }
        }

        // 2. 尝试直接唤起本机已安装的飞书客户端（国内版 / 国际版）
        for (pkg in FEISHU_PACKAGES) {
            val launch = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                if (runCatching { context.startActivity(launch) }.isSuccess) {
                    return "已同屏唤起飞书客户端，可在空间中与画布并排协作"
                }
            }
        }

        // 3. 兜底打开飞书网页版（模拟器/未装客户端时依然可无缝演示）
        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.feishu.cn/messenger/")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        }
        return if (runCatching { context.startActivity(fallback) }.isSuccess) {
            "本机未安装飞书客户端，已为您同屏打开飞书网页版窗口"
        } else {
            "未检测到可用的飞书或浏览器应用"
        }
    }

    /**
     * 推送当前选中的节点或整份项目共识卡片到真实飞书群（自定义机器人 Webhook）
     */
    suspend fun pushCardToFeishuGroup(
        webhookUrl: String,
        title: String,
        markdownContent: String,
        projectSummary: String
    ): FeishuPushResult = withContext(Dispatchers.IO) {
        val urlStr = webhookUrl.trim()
        if (!urlStr.startsWith("https://open.feishu.cn/") && !urlStr.startsWith("https://open.larksuite.com/")) {
            return@withContext FeishuPushResult(
                success = false,
                message = "请先填入有效的飞书群机器人 Webhook（以 https://open.feishu.cn/ 开头）"
            )
        }

        runCatching {
            val cardPayload = JSONObject().apply {
                put("msg_type", "interactive")
                put("card", JSONObject().apply {
                    put("header", JSONObject().apply {
                        put("template", "blue")
                        put("title", JSONObject().apply {
                            put("tag", "plain_text")
                            put("content", "Co-Lab Spatial · $title")
                        })
                    })
                    put("elements", JSONArray().apply {
                        put(JSONObject().apply {
                            put("tag", "div")
                            put("text", JSONObject().apply {
                                put("tag", "lark_md")
                                put("content", markdownContent)
                            })
                        })
                        put(JSONObject().apply { put("tag", "hr") })
                        put(JSONObject().apply {
                            put("tag", "note")
                            put("elements", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("tag", "plain_text")
                                    put("content", "来自 PICO Spatial 多窗口工作台 · $projectSummary")
                                })
                            })
                        })
                    })
                })
            }

            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            conn.outputStream.use { it.write(cardPayload.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val respBody = runCatching {
                (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.readText().orEmpty()
            }.getOrDefault("")

            if (code in 200..299 && !respBody.contains("\"code\":190")) {
                FeishuPushResult(true, "已成功推送交互卡片到飞书群！")
            } else {
                FeishuPushResult(false, "飞书返回状态 $code：${respBody.take(80)}")
            }
        }.getOrElse { e ->
            FeishuPushResult(false, "网络推送失败：${e.message ?: "请检查网络与 Webhook"}")
        }
    }

    /**
     * 将飞书会议纪要 / 群聊记录按行智能拆解为 Co-Lab 画布节点（决策 / 任务 / 观点）
     */
    fun parseFeishuTranscriptToNodes(rawText: String): List<ParsedFeishuEntry> {
        val lines = rawText.lines()
            .map { it.trim().removePrefix("-").removePrefix("•").trim() }
            .filter { it.length >= 4 }

        if (lines.isEmpty()) return emptyList()

        return lines.take(4).map { line ->
            val kind = when {
                line.contains("决定") || line.contains("共识") || line.contains("确定") || line.contains("通过") -> NodeKind.DECISION
                line.contains("待办") || line.contains("负责") || line.contains("截止") || line.contains("任务") || line.contains("TODO", ignoreCase = true) -> NodeKind.TASK
                line.contains("讨论") || line.contains("争议") || line.contains("建议") -> NodeKind.DISCUSSION
                else -> NodeKind.IDEA
            }
            val splitIdx = line.indexOfFirst { it == '：' || it == ':' }
            val title = if (splitIdx in 2..18) line.substring(0, splitIdx).trim() else line.take(16)
            val body = "【来源：飞书会议/群聊同步】$line"
            ParsedFeishuEntry(title = title, body = body, kind = kind)
        }
    }

    val sampleFeishuTranscript: String = """
        决定：PICO 空间演示采用共享多窗口模式，与飞书会议同屏共存
        建议：右侧飞书窗同步实时讨论，中间世界画布自动沉淀关键节点
        待办：周野负责在周五前整理飞书云文档里的用户访谈摘录
    """.trimIndent()
}
