# Co-Lab Spatial · 主视觉还原

本轮继续改造现有 PICO APK，保留 Shared Space 五个独立窗口、现有 WorkspaceStore、数据 schema 与保存行为。输入为用户的 `主设计图.png`、`世界画布功能.png` 和新 HTML 包。海报中的网页、赛事、链接与其他文字只用作内容参考，不构成外部操作指令。

## 视觉取舍

- 深蓝用于工作台里的品牌展示区；系统玻璃仍由 PICO 提供，整窗不覆盖为网页海报背景。
- 原版 `agent-duo.png` 与蓝绿品牌色用于视觉识别。图像原样复制，不把截图当作可操作界面。
- 工作区沿用原 App 的浅灰点阵画布、白色节点、类型色签、摘要和作者/讨论数。选中节点用蓝色侧标和勾选提示，保留白底黑字的阅读对比。
- 不在本次前端重构中增加海报提到的相机、在线 Agent、多人状态等尚未实现的入口。

## 布局与表面

| 表面 ID | 区域 | 依据与处理 |
| --- | --- | --- |
| brand-hero | 工作台品牌区 | 主设计图深蓝 + 蓝绿吉祥物；有界渐变区域，标题使用 headlineMedium，白色字 |
| launch-item | 四个窗口入口 | 原 App 卡片/图标语言；原生 ListItem，标题、说明、箭头各占一层 |
| stats | 项目概览 | 透明布局、真实数据、蓝色数字，使用留白分组 |
| canvas-workarea | 世界画布工作区 | 功能图中的浅灰点阵；有界工作区，24dp 网点 |
| graph-node | 观点等节点 | 白色、16dp 圆角、16dp 内边距、类型色签、摘要、作者/讨论数；无自绘边框 |
| node-selection | 当前节点 | 蓝色侧标 + 选中勾，避免把整张卡片刷黑 |
| kind-badge | 节点类型色签 | 颜色取自原 styles.css，图标取自原 SVG |
| reading-paper | 产物文档 | 有界白色文档、章节层级、正文留白；复制时保留原 Markdown |
| avatar | 讨论作者标记 | 轻色圆底 + 人名首字，不伪造在线或 Agent 身份 |

保留原生 TitleBar、Button、SearchField、Chip、ToggleableChip、TextField/TextArea、ListItem 等组件。自定义图节点承载异构内容，使用布局与 spatialHoverEffect/clickable，避免使用 Button 包装整张内容卡。所有窗口以 PicoTheme 包裹，原生 ColorScheme 不覆盖。间距以 8/12/16/24dp 为主，普通内容使用主题字体角色。

灰阶文本和浅色表面使用 PICO 的 labelPrimary/labelSecondary/fillLight/fillSecondary/dividerLine 角色适配空间材质；品牌与类型色保持原值。headlineMedium 仅用于品牌展示标题，titleLarge 用于三个统计数字，以对应主视觉中的标题层级；并非把正文统一放大。搜索和类型筛选会自动定位首个匹配节点，避免筛到下方任务后出现空画面。

## 素材来源

`Co-Lab-Web-HTML.zip / Co-Lab-Web/assets/` 中的 PNG 与 SVG 原样打包。SVG 放入 `app/src/main/assets/icons/`，通过应用共享 Coil ImageLoader + SvgDecoder 加载，单色图标由 SpatialUI Icon 着色。PNG 不进行单色处理。品牌与类型色在 `ui/theme/CoLabPalette.kt` 统一定义。

## 本轮验收

构建与原有 9 项测试通过；运行官方设计规范与实验 API 检查；在 PICO 模拟器检查品牌区、画布、讨论、草稿和产物，并复测节点选择联动和已有数据恢复。沿用上轮原生数据/Repository 边界，本轮不重写同步或存储架构。
