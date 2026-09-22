# 方块文言

> 把文言实词虚词的**推导卡片**装进口袋 —— 不背义项清单，只画推导链条。

面向高中生的文言实词虚词推演记忆 Android App。每个字配「一句话画面」，义项从本义逐步推导；并把每个义项与**教材篇目原文**双向打通：点开义项能查到它在哪些篇目中出现，点进篇目能直接定位到该字并区分「同义 / 异义」用法。

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![minSdk](https://img.shields.io/badge/minSdk-24%20(Android%207.0)-3DDC84.svg)](#-快速开始)

---

## ✨ 核心特性

### 一、卡片式推演记忆（实词 120 · 虚词 18）

实词卡片 8 个区块：**一句话画面 → 本义 → 推导链 → 例句表 → 易错辨析 → 成语验证 → 记忆口诀 → 相关字**

虚词卡片另有：**核心功能 → 判断流程**（条件 → 关系/词性 → 译法 + 例句）**→ 特殊用法**（如宾语前置「何以 = 以何」）**→ 对比辨析**

- **推导链动画**：核心画面缩小上移 → 连线延伸 → 逐条长出义项 → 弹出例句，约 8 秒，可跳过、可重播
- **易混对比卡**：如「而 vs 则」，用「绳子 / 开关」一句话点破区别，附关系 × 字的 ✅/❌ 对照表
- **高考导向**：义项标注 🎯 高考真题来源，附易错警示与口诀

### 二、释义 ↔ 篇目双向定位（本项目的差异化功能）

- **篇目库**：覆盖统编版高中语文**必修上/下册、选择性必修上/中/下册共 61 篇**文言诗文，每篇含正文、全文翻译与**按义项标注的字词注释（2521 条）**
- **按义项查篇目**：卡片中每个义项旁的「出现篇目 ▸」列出该义在该篇中出现的全部篇目。例如「而 = 转折」可查得 28 篇、「以 = 因为」18 篇、「爱 = 爱护」6 篇
- **3/4 屏底部定位阅读窗口**：点击篇目名后从底部弹出，**自动滚动到该字首次出现处**，并把正文中这个字的每一次出现都高亮：
  - 🟩 **与当前义项相同** → 一色
  - 🟧 **同一个字的其他义项** → 另一色
  - 其余字词**不着色**（普通篇目浏览则是纯文本，无任何标记）
- **点击即释义**：点正文里任一高亮字，弹出该处释义、注释，并提示与当前义项是否一致

### 三、学习闭环

| 模块 | 说明 |
|---|---|
| 每日一练 | 5 种模式：每日 5 题、对比检测、单卡检测、复习检测、错题重练；即时对错反馈 + 解析 + 正确率 |
| 错题本 | 答错自动入库（题干 / 选项 / 我的答案 / 正确答案 / 解析 / 来源），可重练、按卡片溯源 |
| 遗忘曲线复习 | 按艾宾浩斯间隔（10 分钟 → 1 天 → 3 天 → 7 天 → 15 天 → 30 天）生成复习计划，首页展示待复习数 |
| 学习统计 | 打卡天数、已学卡片、平均正确率、累计复习、近 7 天学习柱状图 |
| 收藏 / 搜索 | 收藏持久化；输入汉字直接跳转卡片，篇目支持标题与作者搜索 |
| 随机一字 | 首页一键抽卡，碎片时间随手复习 |

### 四、双主题皮肤

| Ore UI（默认） | Fluent UI |
|---|---|
| Minecraft 基岩版现代 UI 风格，深色玻璃面板 + Ore 绿主按钮 | 微软 Fluent 风格，浅色背景 + Fluent 蓝主色 |

设置中一键切换、即时生效并持久化；面板、按钮、Chip、进度条、滑杆、开关、输入框、分段选择器、顶栏与底部导航全部随皮肤换色。另支持**全 App 字号缩放**（80%–140%）。

---

## 📦 下载安装

前往 [Releases](../../releases) 下载最新 `fangkuai-wenyan-v*.apk` 安装即可（Android 7.0+）。

> 首次启动会解析内置的卡片与篇目 JSON 并写入本地 Room 数据库，请稍候片刻。

---

## 🛠 技术栈

| 层级 | 选型 |
|---|---|
| 语言 | Kotlin 2.0.20 |
| UI | Jetpack Compose（BOM 2024.09）+ Material 3 |
| 架构 | MVVM + Repository |
| 依赖注入 | Dagger Hilt 2.52 |
| 本地存储 | Room 2.7.1 + DataStore（Preferences） |
| 序列化 | kotlinx.serialization（JSON） |
| 导航 | Navigation Compose |
| 图片 | Coil |
| 构建 | AGP 8.5.2 / Gradle 8.7 / JDK 17+ |
| SDK | compileSdk 35 · targetSdk 34 · **minSdk 24** |

**字体分层**：标题、按钮等醒目元素用像素字体（英文 Monocraft / 中文方舟像素字体，均 SIL OFL-1.1）；正文用系统默认中文字体（思源黑体），保证长文阅读舒适。

---

## 🚀 快速开始

```bash
# 1. 克隆
git clone https://github.com/OvOHongYu/fangkuai-wenyan.git
cd fangkuai-wenyan

# 2. 指定 Android SDK 路径（或直接用 Android Studio 打开自动生成）
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# 3. 构建 Debug 包
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

**环境要求**：JDK 17 及以上、Android SDK Platform 35、Build-Tools 34.0.0。

Windows 下若项目位于含中文的路径，`gradle.properties` 中的 `android.overridePathCheck=true` 已处理该情况。

### 签名 Release 包

Release 构建从项目根的 `keystore.properties`（**不入库**）读取签名信息，缺失时产出未签名 APK：

```properties
storeFile=keystore/release.jks
storePassword=******
keyAlias=wenyan
keyPassword=******
```

```bash
./gradlew :app:assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk
```

---

## 📁 项目结构

```
app/src/main/
├─ java/com/ziyuan/wenyan/
│  ├─ MainActivity.kt / WenyanApp.kt      # 入口（Hilt + 主题 + 字号缩放）
│  ├─ data/
│  │  ├─ model/                           # 卡片 / 篇目数据模型（含 occurrences 推导）
│  │  ├─ local/                           # Room：Entity / DAO / AppDatabase
│  │  ├─ parser/DataImporter.kt           # 首次启动导入 assets JSON
│  │  └─ repository/                      # Card / Text / Learning 仓储
│  ├─ ui/
│  │  ├─ theme/                           # 双皮肤（AppColors + OreDark / FluentLight）、字体
│  │  ├─ components/OreUi.kt              # 共享组件库（面板/按钮/Chip/进度条/顶栏…）
│  │  ├─ home/ library/ card/ compare/    # 首页 / 卡片库 / 卡片详情 / 对比卡
│  │  ├─ texts/                           # 篇目库、篇目详情、3/4 屏定位阅读窗口
│  │  ├─ quiz/ review/ wrongbook/ profile/ settings/
│  │  └─ MainScreen.kt                    # 路由与底部导航
│  └─ util/                               # 遗忘曲线、出题器、设置存储
├─ assets/
│  ├─ cards/                              # shici_a~g.json(120) / xuci*.json(18) / compare.json(3)
│  ├─ texts/                              # texts_bx1/bx2/xb1/xb2/xb3.json（61 篇）
│  └─ OFL_*.txt                           # 字体许可原文
└─ res/font/                              # monocraft.ttf / ark_pixel.ttf
```

---

## 🗂 数据格式

数据与代码解耦：新增内容只需往 `assets/` 放 JSON，App 首次启动自动导入（文件名前缀决定类型：`shici*` 实词、`xuci*` 虚词、`compare*` 对比卡、`texts/` 篇目）。

**实词卡片**

```json
{
  "id": "shi_ai", "type": "实词", "char": "爱", "pinyin": "ài",
  "picture": "一个人紧紧抱住一样东西，舍不得松手",
  "benyi": "吝惜、舍不得",
  "chain":   [{ "from": "紧紧抱住舍不得松手", "to": "吝惜", "condition": "舍不得给别人" }],
  "examples":[{ "yixiang": "吝惜", "sentence": "齐国虽褊小，吾何爱一牛",
                "source": "《齐桓晋文之事》", "translation": "……", "isGaokao": true }],
  "idioms":  [{ "idiom": "爱不释手", "yixiang": "喜爱" }],
  "traps":   [{ "warning": "先秦「爱」多指「吝惜」", "gaokao": "2024 新课标Ⅱ卷" }],
  "mnemonic": "爱字本义舍不得，吝惜喜爱爱护连。",
  "relatedChars": ["惜", "怜", "吝"]
}
```

**篇目**（`annotations.positions` 为该词在正文中的字符下标，是双色高亮与「按义项查篇目」的基础；同一字多义项时按义项分别给出下标）

```json
{
  "id": "text_qihuan", "title": "齐桓晋文之事", "author": "孟子", "dynasty": "战国",
  "book": "必修下册", "genre": "文言文",
  "content": "……", "translation": "……",
  "annotations": [{ "word": "爱", "meaning": "吝惜", "note": "舍不得", "positions": [432, 1187] }]
}
```

> 虚词义项名统一使用规范表，例如 `而`：并列 / 承接 / 递进 / 转折 / 修饰 / 因果 / 假设；`以`：用 / 凭借 / 因为 / 把 / 来 / 按照 / 认为 / 在。卡片与篇目注释保持同一套命名，检索才能精确命中。

---

## 📄 许可

- **本项目代码、卡片数据、篇目数据与文档**：MIT，见 [LICENSE](LICENSE)
- **字体**：Monocraft（© 2022 Idrees Hassan）与方舟像素字体 Ark Pixel Font（© 2021 TakWolf）均为 **SIL Open Font License 1.1**，按原样分发、未声明保留字体名，完整许可文本随仓库与 APK 分发 —— 详见 [THIRD-PARTY-LICENSES.md](THIRD-PARTY-LICENSES.md)
- 正文使用 Android 系统默认中文字体（Noto Sans CJK / 思源黑体），不随包分发

## ⚠️ 声明

篇目正文引自统编版高中语文教材及公版古籍，译文与字词注释为教研整理，仅供学习交流；卡片中标注的高考真题出处仅作题源提示，具体表述以官方试题为准。
