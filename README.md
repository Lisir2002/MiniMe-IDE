# MiniMe-IDE

[![RC Build](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/rc.yml/badge.svg)](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/rc.yml)
[![Release](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/release.yml/badge.svg)](https://github.com/Lisir2002/MiniMe-IDE/actions/workflows/release.yml)
[![Version](https://img.shields.io/badge/version-0.1.0--rc1-blue)](version.properties)
[![License](https://img.shields.io/badge/license-Proprietary-gray)](LICENSE)

> **安卓上的前端编程教学 IDE —— 写代码 + 即时预览，完全离线。**
> 面向入门/教学场景，iOS 风格全自绘界面，Android 12+，无任何云端依赖。

| 项 | 值 |
| --- | --- |
| 应用名称 | MiniMe-IDE |
| 包名 / applicationId | `com.mini.ide` |
| 当前阶段 | **M0 工程脚手架**（空壳，业务逻辑从 M1 起填充） |
| 最低系统 | Android 12（API 31） |

---

## 快速开始

环境搭建见 [`docs/ENVIRONMENT.md`](docs/ENVIRONMENT.md)。**本地只做单元测试**：

```bash
# 本地标准验证：单元测试 + 架构铁律
./gradlew :core:common:testDebugUnitTest \
          :core:model:testDebugUnitTest \
          :language:api:testDebugUnitTest \
          checkModuleRules --no-daemon
```

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

16 个 Gradle 模块，按 8 大系统分层。依赖方向单向向下，架构铁律由 `checkModuleRules` 编译期强制。

```
:app                              应用壳 / Hilt 装配层（唯一可依赖所有 impl）
:core:common    (S8)              基础设施：常量、DispatcherProvider
:core:model                       共享领域模型：Project / SourceFile / FileKind
:core:designsystem  (S2)          主题令牌：色板、EditorTokens、Compose 主题
:core:uicommon                    自绘通用组件（磨砂玻璃 Card / Sheet）
:data:api / :data:impl  (S3)      数据系统：Repository / Room / ContentStore
:engine:api / :engine:impl  (S4)  核心引擎：Editor / Render / Bridge / Project
:language:api / :language:impl (S5) 语言与语法：可插拔 LanguageProfile
:feature:project / editor / preview  功能模块（只依赖 api）
:feature:lesson     (S6)          教学系统（Phase2 激活）
:extension:api      (S7)          扩展点契约（仅接口，只留口不设计）
:testdouble                       Fake 实现（测试 / 预览替换）
```

**架构铁律**（`./gradlew checkModuleRules` 校验，违规即构建失败）：

1. 任何模块不得依赖 `:app`　2. `:core:*` 不得依赖业务模块
3. `*:api` 不得依赖 `*:impl`　4. `:feature:*` 不得依赖 `*:impl`　5. `*:impl` 不得依赖 `:feature:*`

---

## 版本与发版

- **版本真源**：`version.properties`（`x.y.z` + `RC_NUMBER`）；versionCode 存于 `release/versioncode.txt`，单调递增。
- **测试版** `x.y.z-rcN`：推送 tag `v*-rc*` 触发 `rc.yml`，自由发布。
- **正式版** `x.y.z`：手动触发 `release.yml`，**必须先通过排班门禁**。

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

*MiniMe-IDE · 教学型离线前端 IDE · 仅设计 + M0 脚手架阶段*
