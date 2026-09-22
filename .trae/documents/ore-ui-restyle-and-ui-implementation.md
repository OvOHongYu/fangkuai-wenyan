# Ore UI 风格改造与 UI 层实现计划

## Summary

将"字源·文言实词"App 视觉风格从当前"水墨朱砂"改为 Minecraft Ore UI（基岩版现代 UI 设计系统）：全局强制深色玻璃拟态、Ore 绿主按钮、粗圆角卡片、细亮边框、分段进度条；接入像素字体（英文 Monocraft / 中文方舟像素字体，均为 OFL 开源许可）；建立共享 Ore 组件库后，再并行派发 4 个 UI 屏幕实现批次（对应 spec Task 7-22，屏幕仍为占位页，正是引入设计系统的时机）。

## 用户已确认的决策

1. **主题模式**：强制深色（移除设置页深色模式开关，App 始终为 Ore UI 深色主题）
2. **英文字体**：Minecraft Ten 为 Mojang 专有资产不可再分发，改用 **Monocraft**（SIL OFL，仿 Minecraft 风格等宽像素字体）
3. **中文字体**：**方舟像素字体 ark-pixel-font**（TakWolf/ark-pixel-font，SIL OFL 1.1，含简体中文，选 12px proportional TTF）
4. **字体分层（修订反馈）**：像素字体仅用于按钮、标题等醒目元素；正文/长文本使用 Noto Sans CJK 思源黑体（Android 系统默认，无需打包）

## Current State（基于代码探索）

- 主题层：[Color.kt](e:\项目\高中文言工具\app\src\main\java\com\ziyuan\wenyan\ui\theme\Color.kt)（水墨朱砂配色+双色高亮）、[Theme.kt](e:\项目\高中文言工具\app\src\main\java\com\ziyuan\wenyan\ui\theme\Theme.kt)（Light/Dark 双 scheme）、[Type.kt](e:\项目\高中文言工具\app\src\main\java\com\ziyuan\wenyan\ui\theme\Type.kt)（Serif 标题）
- [MainActivity.kt](e:\项目\高中文言工具\app\src\main\java\com\ziyuan\wenyan\MainActivity.kt)：读取 `SettingsStore.darkMode` 决定深浅主题
- 11 个屏幕（home/library/quiz/profile/card/compare/texts×2/wrongbook/review/settings）全部为居中占位 Text，无共享组件包
- [MainScreen.kt](e:\项目\高中文言工具\app\src\main\java\com\ziyuan\wenyan\ui\MainScreen.kt) 路由完整，屏幕签名固定
- 数据层、Repository、Room、assets JSON（20 实词 + 8 虚词 + 3 对比卡 + 61 篇目）全部就绪
- Gradle/Android SDK 尚未安装完成（上次后台脚本失败，实现开始时先重装）
- `local.properties` 已指向 `C:/Users/DELL/AppData/Local/Android/Sdk`

## Proposed Changes

### 第 0 步：环境准备（构建前置）
- 检查 Gradle 8.7（`%USERPROFILE%\gradle-8.7\bin\gradle.bat`）与 cmdline-tools 是否就绪，未就绪则重跑安装脚本（上次失败原因查 `%TEMP%\trae-agent-toolhost\jobs\...\output.log`）
- sdkmanager 安装 `platform-tools`、`platforms;android-34`、`build-tools;34.0.0` 并接受许可（JAVA_HOME 指向系统 JDK 21）

### 第 1 步：主题层 Ore 化（重写 3 文件 + 2 处小改）

**Color.kt 重写为 Ore palette：**
- `OreBg = 0xFF161616`（页面底）、背景渐变 `0xFF242424 → 0xFF131313`
- `OrePanel = 0xE6222222`（90% 不透明深灰卡片）、`OrePanelBorder = 0x2EFFFFFF`
- `OreGreen = 0xFF4EA75A`（主色，按下态 `0xFF63C271`）、`OreGreenDim = 0xFF3D8447`
- `OreGray = 0xFF5C5C5C`（次按钮）、`OreRed = 0xFFB33A2C`、`OreYellow = 0xFFD9A93D`（收藏星/强调）
- 文本：主 `0xFFF5F5F5`、次 `0xFFB8B8B8`
- 阅读窗口双色高亮（深色专用）：`SameHighlight = 0x804EA75A`（同义·绿）、`DiffHighlight = 0x80C77F2E`（异义·橙）
- 删除原 Vermilion/Daiqing 等变量与 Light 高亮变体

**Theme.kt 重写：** 单一 `darkColorScheme(primary=OreGreen, background=OreBg, surface=OrePanel基色…)`；`WenyanTheme(content)` 去掉 darkTheme 参数，移除 LightColors

**Type.kt 重写（分层字体策略，按用户反馈修订）：**
- 醒目元素（标题 display/headline/title、按钮、Chip、Tab、底部导航标签）→ `PixelFontFamily`（方舟像素字体，中文标题）
- 正文/长文本（bodyLarge/bodyMedium、例句、译文、注释）→ `FontFamily.Default`（Android 系统默认中文即 Noto Sans CJK 思源黑体，**无需打包正文字体**）
- 像素标题保持 Normal 字重（避免合成粗体变形），行高适当放大

**MainActivity.kt 小改：** 移除 `settings.darkMode` 分支，固定深色主题；保留 `fontScale` 逻辑

**themes.xml（values 与 values-night）：** `windowBackground` 统一改 `#161616`

### 第 2 步：像素字体接入（仅醒目元素，新增 4 个文件）

- 下载（实现时执行，GitHub release 失败则用 jsdelivr/ghproxy 镜像）：
  - Monocraft TTF → `app/src/main/res/font/monocraft.ttf`
  - ark-pixel-font 12px proportional TTF → `app/src/main/res/font/ark_pixel.ttf`（res 资源名须小写）
- 许可文件：`app/src/main/assets/OFL_Monocraft.txt`、`app/src/main/assets/OFL_ark_pixel.txt`
- 新建 `ui/theme/Font.kt`（分层策略，按用户反馈）：
  - `PixelFontFamily = FontFamily(Font(R.font.ark_pixel))` —— **仅用于醒目元素**：标题、按钮文字、Chip、Tab、底部导航标签
  - `McFontFamily = FontFamily(Font(R.font.monocraft))` —— 仅用于纯英文/数字装饰元素（统计数字、进度百分比）
  - 正文/长文本一律 `FontFamily.Default`（系统 Noto Sans CJK 思源黑体，不打包，保证阅读舒适与 APK 体积）
  - 缺失字形（如拼音声调 à/é）由 Android 系统字体自动回退，不影响可读性
- `OreButton` 等组件内部文字固定使用 `PixelFontFamily`

### 第 3 步：共享 Ore 组件库（新建 ui/components/OreUi.kt）

| 组件 | 规格要点 |
|---|---|
| `OreBackground` | 全屏容器：垂直渐变 OreBg，内容 padding |
| `OrePanel` | 90% 深灰填充 + 1dp 亮边框 + 10dp 圆角 |
| `OreButton(text, onClick, variant, enabled)` | 44dp 高、8dp 圆角；PRIMARY 绿 / SECONDARY 灰 / DANGER 红；按下变亮 |
| `OreChip` | 6dp 圆角小标签，选中态绿色填充 |
| `OreProgressBar(progress)` | 10dp 高分段式绿色进度条（Canvas 每 12dp 画 2dp 间隔） |
| `OreSlider` / `OreSwitch` | 绿色填充控件（设置页用） |
| `OreTopBar(title, onBack)` | 56dp + 返回箭头 + 底部 1dp 边框 |
| `OreTextField` | 深底 `#1E1E1E`、边框聚焦变绿 |
| `OreTabRow` | 分段选择器（选中项绿色填充） |
| `OreSectionTitle(index, title)` | "① 一句话画面"样式：绿色序号 + 白色标题 |

**MainScreen.kt 底部导航 Ore 化：** `NavigationBar` 容器色 `0xF0181818`、选中指示器/图标绿色；同时做 3 处签名协调改动：
1. `HomeScreen` 增加 `onOpenLibrary` 参数（快捷入口跳转卡片库）
2. `CardDetailScreen` 增加 `onStartQuiz` 参数（实战检测，`quiz?mode=card&sourceId=<cardId>`）
3. `WrongBookScreen` 增加 `onStartQuiz` 参数（重练，`Dest.Quiz.ofWrong()`）

### 第 4 步：并行派发 4 个 UI 实现子代理（签名不变，全部使用 Ore 组件）

| 批次 | 范围 | 关键文件 |
|---|---|---|
| A | 首页（搜索/今日复习/学习进度/快捷入口/随机一字）+ 卡片库（实词音序/虚词分类/对比/篇目库入口/收藏） | home、library 各 Screen+ViewModel |
| B | 实词/虚词卡片详情（8 大区块、收藏、上一字/下一字、打开即标记已学）+ 推导链动画（约 8 秒可跳过重播）+ "出现篇目"弹窗 + **TextReadingSheet（3/4 屏底部窗口：自动定位首次出现、同义/异义双色高亮、译文/注释切换）** | card、compare 包 + 新建 ui/texts/TextReadingSheet.kt |
| C | 篇目库（按册别分组 + 标题/作者搜索）+ 篇目详情（原文/译文/注释三标签） | texts 包 |
| D | 检测（daily/compare/card/wrong/review 五种模式 + 反馈解析 + 正确率）+ 复习页 + 错题本 + 我的（统计 + 近 7 天柱图）+ 设置（**移除深色开关，改为静态"主题：Ore UI（深色）"项**；字体大小滑块；导出 JSON 分享） | quiz/review/wrongbook/profile/settings 包 |

子代理提示词中附 Ore UI 组件清单与设计 token，保证全 App 风格统一。

### 第 5 步：spec/checklist 同步

- [spec.md](e:\项目\高中文言工具\.trae\specs\build-wenyan-android-app\spec.md)："设置与导出"需求改为"字体大小调节、卡片导出；App 始终为 Ore UI 深色主题"；What Changes 补充 Ore UI 风格
- [checklist.md](e:\项目\高中文言工具\.trae\specs\build-wenyan-android-app\checklist.md)："设置支持字体大小调节与深色模式切换" → "设置支持字体大小调节；App 始终为 Ore UI 深色主题"
- [tasks.md](e:\项目\高中文言工具\.trae\specs\build-wenyan-android-app\tasks.md)：勾选 Task 1-6，后续随实现进度勾选

## Assumptions & Decisions

- 强制深色（用户确认）；`SettingsStore.darkMode` 字段保留在 DataStore 中但不再被读取，避免迁移
- Minecraft Ten 版权风险规避，用 Monocraft 替代（用户确认）
- 像素字体两份 TTF 约 5-10MB，可接受；仅本地学习工具，无上架需求
- 双色高亮沿用"同义绿 / 异义橙"语义，按深色主题调整透明度
- UI 批次间互不共享新文件（TextReadingSheet 由批次 B 独家创建并按固定签名使用），避免并行冲突

## Verification

1. 环境检查：gradle -v、sdkmanager list 显示 android-34
2. `gradle.bat :app:assembleDebug` 编译通过（在 e:\项目\高中文言工具 下执行，JAVA_HOME 指向 JDK 21）
3. 按更新后 checklist 逐项静态验证（读代码核对：强制深色、各屏使用 Ore 组件、TextReadingSheet 3/4 屏 + 双色高亮 + 自动滚动、五种检测模式、遗忘曲线间隔 10min/1/3/7/15/30 天、字体资源与许可文件存在）
4. tasks.md / checklist.md 勾选与最终汇报
