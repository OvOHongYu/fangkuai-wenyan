# 第三方许可与署名

本项目的**源代码、卡片数据、篇目数据与文档**以 MIT 许可发布，详见 [LICENSE](LICENSE)。

下列第三方资源遵循各自许可，**不属于 MIT 许可范围**。

---

## 一、字体（SIL Open Font License 1.1）

### 1. Monocraft —— 英文 / 数字字形

| 项 | 内容 |
|---|---|
| 用途 | 按钮、标题、统计数字等醒目元素的英文与数字字形 |
| 文件 | `app/src/main/res/font/monocraft.ttf` |
| 版权 | Copyright (c) 2022, Idrees Hassan（https://github.com/IdreesInc/Monocraft） |
| 许可 | SIL Open Font License, Version 1.1（OFL-1.1） |
| 完整许可 | [app/src/main/assets/OFL_Monocraft.txt](app/src/main/assets/OFL_Monocraft.txt) |

### 2. 方舟像素字体 Ark Pixel Font —— 中文字形

| 项 | 内容 |
|---|---|
| 用途 | 按钮、标题等醒目元素的中文字形（本项目采用 12px 比例字体·简体中文版） |
| 文件 | `app/src/main/res/font/ark_pixel.ttf` |
| 版权 | Copyright (c) 2021, TakWolf（https://github.com/TakWolf/ark-pixel-font） |
| 许可 | SIL Open Font License, Version 1.1（OFL-1.1） |
| 完整许可 | [app/src/main/assets/OFL_ark_pixel.txt](app/src/main/assets/OFL_ark_pixel.txt) |

### OFL-1.1 合规说明

- 上述两款字体均以**未作任何修改的原始文件**随本项目分发，未使用任何保留字体名称（Reserved Font Name）。
- 完整 OFL-1.1 许可文本随仓库分发，同时也打包进 APK 的 `assets/` 目录，满足"许可随字体一同分发"的要求。
- 依 OFL-1.1：字体本身不得单独出售；其衍生作品必须继续沿用 OFL-1.1 许可。
- 若需在其他项目中使用，请遵循各自的上游仓库与 OFL-1.1 条款。

---

## 二、正文字体说明

App 的正文（例句、译文、注释等长文本）使用 **Android 系统默认中文字体**（Noto Sans CJK / 思源黑体），本项目**不随包分发**该字体文件。

---

## 三、主要开源依赖

以下依赖均通过 Gradle 引入，遵循其各自许可（主要为 Apache License 2.0）：

| 依赖 | 许可 |
|---|---|
| Kotlin / kotlinx.coroutines / kotlinx.serialization | Apache-2.0 |
| AndroidX（Core、Activity、Lifecycle、Navigation、DataStore） | Apache-2.0 |
| Jetpack Compose（UI、Material 3） | Apache-2.0 |
| Dagger Hilt | Apache-2.0 |
| Room | Apache-2.0 |
| Coil | Apache-2.0 |

各依赖的完整许可文本可在其官方仓库或 Maven 制品中查阅。

---

## 四、内容声明

- 篇目正文引自统编版高中语文教材（必修上/下册、选择性必修上/中/下册）及公版古籍，译文与字词注释为本项目教研整理，仅用于学习交流。
- 卡片中的高考真题例句出处标注（年份/试卷）仅作题源提示，具体表述以官方试题为准。
