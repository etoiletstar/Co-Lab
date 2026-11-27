# Co-Lab 空间工作台

来自用户提供的 `Co-Lab-source.zip` 的**单人多窗口可操作原型**，独立于原网页和服务器运行。使用 PICO Spatial SDK BOM **6.2.8**、Kotlin、原生 SpatialUI；无需 Unity、登录、后端或在线模型。

## 当前能做什么

| 空间窗口 | 可操作内容 |
| --- | --- |
| 工作台 | 打开其他窗口、查看动态与任务进度、撤销修改、复制全项目 Markdown |
| 项目画布 | 搜索、新增观点、选中节点、显示关系连线、长按拖动、滚动画布 |
| 节点讨论 | 编辑并保存标题/正文、补充讨论、人工确认决策、转为任务、完成/重开任务 |
| 整理草稿 | 从选中节点和讨论生成本地规则纪要、修改草稿、人工采纳保存 |
| 项目产物 | 查看和复制 Markdown、追溯到来源节点 |

启动时打开工作台、画布、详情三个独立 `WindowContainer`。其他窗口通过工作台打开。初始左右摆放均以主窗口为锚点，避免依赖另一个窗口异步创建时产生重叠。每个窗口使用稳定 ID/tag，重复打开复用同一实例。

所有窗口共享一个应用级 `WorkspaceStore`。保存后的内容、选择、节点位置、讨论、任务和产物写入本机 SharedPreferences JSON；重启应用恢复。未保存的编辑在同次进程里按节点保留；需点“保存修改”落盘。撤销历史保留最近 20 次内容修改，仅在当前进程有效。拖动在手势结束时保存位置。

示例项目源自原应用的“AI 创新协作平台”，成员名称为示例记录。整理草稿明确标注**本地规则整理，未调用在线模型**。未接入原 PostgreSQL/Express、用户账号、多设备实时同步、语音或在线 Agent。

## 构建与运行

需要 Java 21、Android SDK Platform 35 / Build Tools 35、已配置的官方 PICO CLI。使用 Android Studio 时打开本目录，使用本机的 SDK 路径生成 `local.properties`，不要提交该文件。建议队友把工程放在英文路径。

在当前工作空间的 PowerShell 7 中：

```powershell
& '.\Co-Lab\pico-spatial\tools\Build-Pico.ps1'
```

该脚本会复用本工作空间工具环境，构建 APK 并运行 9 项测试。Windows 中文路径会通过临时目录 junction 使用英文路径访问，原文件不移动、不删除。其他机器没有工作空间工具环境时，使用自己已配置的 Java/Android/PICO 工具。

当前专用模拟器为 `CoLab_Pico_6_2`，设备 ID `emulator-5554`。在工作空间根目录中：

```powershell
. '.\.tools\pico-env.ps1'
& $taskPicoCli emulator start --avd CoLab_Pico_6_2 --yes --format json
& '.\Co-Lab\pico-spatial\tools\Build-Pico.ps1' -InstallDevice emulator-5554
```

设备 ID 可能在重启或增加模拟器后变化。先 `pico-cli device list --format json` 确认，再传入实际 ID。

APK：`app/build/outputs/apk/debug/app-debug.apk`。这是调试签名包，供内部体验；赛事提交前另配团队发布签名。

## 建议的演示路径

1. 在工作台“展开完整工作区”，用系统把手把常用窗口摆到舒适位置。
2. 在画布选中“完成工作区可操作原型”，观察独立详情窗口同步切换。
3. 补充一条验收讨论，发布到本机；点“完成任务”，观察画布状态和工作台计数变化。
4. 点“整理为草稿” → “整理当前节点”，核对、编辑后“人工采纳并保存”。
5. 在产物窗口查看纪要并回到来源节点；关闭并重开窗口，内容仍在。
6. 停止并重启应用，确认讨论、任务状态和产物保留。

## 继续接多人同步的位置

- `data/WorkspaceModels.kt`：节点、讨论、产物及纯数据操作。
- `data/WorkspaceCodec.kt`：schema=1 的本机 JSON 编解码及校验。
- `data/WorkspaceStore.kt`：应用级状态、仓库接口、本地保存、撤销。后续在 `WorkspaceRepository` 边界接原后端，并增加状态订阅；现有同步接口是同步本地接口，需要改为协程/Flow 后再发网络请求。
- `content/WorkspacePages.kt`：五个窗口的原生交互。
- `Main.kt`：窗口注册与初始空间布局。
- 原应用 `../canvas-live.js` / `../api.js`：已有 SSE、画布快照/操作批次和鉴权实现，可用于下一阶段适配。当前 APK 没有调用它们。

下一阶段应先统一服务端节点 ID、会话鉴权、操作去重/版本冲突和离线重连，再做多人光标、权限与在线 Agent。当前原型不是多人同步实现。

## 验证与已知限制

已通过 APK 构建、9 项单元测试、SpatialUI 官方规范检查（0 错误 / 0 警告）、实验 API 检查。已在 PICO 6.2.1 模拟器安装、启动并验证原生窗口选中联动。其他运行验证见工作空间 `output/colab-*` 记录。

模拟器运行不等于真机验收。裸手长按拖动、系统键盘、窗口尺寸舒适度及 30 分钟稳定性仍应在最终赛事设备上确认。本机电脑操作工具初始化失败，因此使用官方 PICO CLI 公开语义和空间截图完成可自动化检查；不声称已完成真机手势验收。

原型没有删除或覆盖原网页工程，也没有向原线上服务写入测试数据。
