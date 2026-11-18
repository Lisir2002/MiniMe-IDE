# MiniMe-IDE

[![RC Build](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/rc.yml/badge.svg)](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/rc.yml)
[![Release](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/release.yml/badge.svg)](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/release.yml)
[![Version](https://img.shields.io/badge/version-1.0.0--rc2-blue)](version.properties)
[![License](https://img.shields.io/badge/license-Proprietary-gray)](LICENSE)

> **安卓上的前端编程教学 IDE —— 写代码 + 即时预览，完全离线。**
> 面向入门/教学场景，iOS 风格全自绘界面，Android 12+，无任何云端依赖。

| 项 | 值 |
| --- | --- |
| 应用名称 | MiniMe-IDE |
| 包名 / applicationId | `com.mini.ide` |
| 当前阶段 | **M6 自研语言**（S5 Phase3 MiniLang + 编译器 + S7 扩展点最小激活） |
| 最低系统 | Android 12（API 31） |

> **M4 已打通核心闭环**：项目列表 → 新建/打开 → HTML/CSS/JS 三文件编辑 → 300ms 防抖即时预览 → 自动/手动保存 → 控制台日志。编辑器为离线自实现内核（透明 textarea + 语法高亮镜像），完整实现 Bridge 协议，未来可无缝替换为 CodeMirror 6（Kotlin 引擎层零改动）。
>
> **M5 叠加教学能力**：工作区顶栏教程入口 → 2 节内置课程（分步任务 + 数据驱动校验 + 提示卡片 + 进度跟踪）；预览报错升级为结构化诊断（分类标签 + 中文教学建议 + 来源行号 + 分类过滤）。
>
> **M6 兑现自研语言**：新增面向小白的中文 DSL **MiniLang**（`页面/文字/按钮/卡片/当点击`）与独立编译器（`:language:dsl`：词法 → 语法 → AST → 代码生成 → HTML/CSS/JS），注册进语言注册表——**编辑器内核零改动**即获得 MiniLang 高亮与补全。编译产物复用 M4 预览链路，编译诊断经 console 通道复用 M5 诊断卡片。内置「MiniLang 入门」模板，一键创建会响应的中文网页。S7 扩展系统最小激活（ExtensionRegistry）。

---

## 快速开始

环境搭建见 [`docs/ENVIRONMENT.md`](docs/ENVIRONMENT.md)。**本地只做单元测试**：

```bash
# 本地标准验证：全模块单元测试 + 架构铁律
ANDROID_HOME=/opt/android-sdk ./gradlew testDebugUnitTest checkModuleRules --no-daemon
```

> 当前 173 个 JVM 单测覆盖数据层、引擎层、语言层（含 MiniLang 编译器）、四个 feature 的 ViewModel、桥接协议、教学诊断、扩展注册表、DSL 预览接入，以及新增的结构化日志（core:log）与权限系统（core:permission）。

> ⚠️ **全量测试与发行构建（assemble / release）不在本地执行，一律由 GitHub Actions 云端完成。**

---

## 技术栈基线

| 组件 | 版本 | 组件 | 版本 |
| --- | --- | --- | --- |
| Kotlin | 2.2.21 | AGP | 8.10.0 |
| Gradle (wrapper) | 8.11.1 | KSP | 2.2.21-2.0.5 |
| Compose BOM | 2026.04.01 | Hilt | 2.57.2 |
| Room | 2.8.4 | Coroutines | 1.10.2 |
| JDK | 17 (Temurin) | minSdk / targetSdk | 31 / 36 |

> 版本唯一来源为 `gradle/libs.versions.toml`。AGP / Gradle / Kotlin / KSP 必须成套升级。

---

## 模块地图

20 个 Gradle 模块，按 8 大系统分层。依赖方向单向向下，架构铁律由 `checkModuleRules` 编译期强制。

```
:app                              应用壳 / Hilt 装配层（唯一可依赖所有 impl）
:core:common    (S8)              基础设施：常量、DispatcherProvider
:core:model                       共享领域模型：Project / SourceFile / Lesson / ValidatorSpec / FileKind
:core:designsystem  (S2)          主题令牌：色板、EditorTheme、Compose 主题（默认亮色）
:core:uicommon                    自绘组件库：玻璃/按钮/卡片/顶栏/标签栏/SVG 图标/动效
:core:log           (M7)          结构化日志：CONVENTIONS 格式 / 按日分片+轮转 / 外部 MINIIDE 目录 / 全局崩溃捕获
:core:permission    (M7)          隐私合规：6 类权限定义 / 用前申请 / rationale / 运行时弹窗+设置页
:data:api / :data:impl  (S3)      数据系统：Repository / Room / ContentStore
:engine:api / :engine:impl  (S4)  核心引擎：Editor / Render / Bridge / Project / Lesson / Diagnostic
:language:api / :language:dsl / :language:impl (S5) 语言与语法：可插拔 LanguageProfile / MiniLang 编译器 / LanguageProvider / 注册表
:feature:project / editor / preview  MVP 功能模块（只依赖 api；editor/preview 含 WebView 适配与 assets）
:feature:lesson     (S6)          教学系统（M5 已激活：课程列表/分步校验/进度/提示）
:extension:api      (S7)         扩展点（M6 最小激活：ExtensionRegistry；Share/Collab 仍仅契约）
:testdouble                       Fake 实现（测试 / 预览替换）
```

**架构铁律**（`./gradlew checkModuleRules` 校验，违规即构建失败）：

1. 任何模块不得依赖 `:app`　2. `:core:*` 不得依赖业务模块
3. `*:api` 不得依赖 `*:impl`　4. `:feature:*` 不得依赖 `*:impl`　5. `*:impl` 不得依赖 `:feature:*`

---

## 版本与发版

- **当前版本**：`1.0.0-rc2`（在 rc1 基础上修复顶栏重叠/新建项目闪退，新增结构化日志与权限系统，默认浅色主题；release 包已开 R8 混淆）。正式版 `1.0.0` 仍须 owner 排班批准后才发。
- **版本真源**：`version.properties`（`x.y.z` + `RC_NUMBER`）；versionCode 存于 `release/versioncode.txt`，单调递增（仅云端发版时递增）。
- **测试版** `x.y.z-rcN`：手动触发 `rc.yml`（workflow_dispatch，输入版本号），云端全量测试 → R8 出包 → 打 tag `v*` → 创建 GitHub Release 发版页面并上传 APK，自由发布无门禁。
- **正式版** `x.y.z`：手动触发 `release.yml`，**必须先通过排班门禁**，云端 `assembleRelease`（同样经 R8 混淆）。
- **混淆**：所有 release 构建（`isMinifyEnabled` + `isShrinkResources`）均启用 R8，`proguard-rules.pro` 白名单保留 JS 桥接与反射入口。
- **M7 稳定性/隐私**：默认亮色主题（白底黑字）；顶栏已回避系统状态栏；新建项目闪退已修（`ProjectEngine.open()` 过滤初始 null）；结构化日志写入外部存储根目录 `MINIIDE/logs/`（无权限时降级应用专属目录），全局崩溃捕获写 FATAL 堆栈；6 类权限（存储/网络/定位/相机/打开外部应用/安装未知应用）按需申请、用前告知，拒绝后优雅降级。详见仓库外 `M7-稳定性与隐私合规实现说明.md`。

### 排班门禁（正式版不可绕过）

正式版发布前须在 `release/schedule.yaml` 中有 `status: approved` 记录（仅 owner 可改），CI 校验：
版本存在 / approved / 进入批准窗口 / 排班人为 owner / sourceRc 匹配，全部通过才构建。

```bash
# 本地预检（不会放行正式版，仅验证脚本逻辑）
python3 scripts/check_schedule.py --version 1.0.0 --schedule release/schedule.yaml
```

> 门禁在 CI 服务端执行 + 分支保护，本地无法放行正式版。

---

## 本地 / 云端分工

| 动作 | 本地 | GitHub Actions |
| --- | --- | --- |
| JVM 单元测试 / 架构铁律 | ✅ | ✅ |
| 全量测试 / 全模块 assemble | ❌ | ✅ |
| RC / 正式版构建、versionCode 递增、打 tag | ❌ | ✅ |

---

## 规范与文档

| 文档 | 说明 |
| --- | --- |
| [`docs/CONVENTIONS.md`](docs/CONVENTIONS.md) | **工程规范**：提交信息、发版说明、日志格式、文档规范（唯一事实来源） |
| [`docs/ENVIRONMENT.md`](docs/ENVIRONMENT.md) | 环境安装、持久化配置、本地/云端分工 |
| [`CHANGELOG.md`](CHANGELOG.md) | 变更与发版历史（Keep a Changelog） |
| 产品与技术方案 / 工程蓝图 / 视觉设计规范 | 仓库外同级目录（设计阶段文档） |

---

*MiniMe-IDE · 教学型离线前端 IDE · M7 稳定性与隐私合规 · v1.0.0-rc2（R8 混淆）*
