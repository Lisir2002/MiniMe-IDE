# MiniMe-IDE

> 安卓上的前端编程教学 IDE —— 写代码 + 即时预览，完全离线。
> 应用标识：`com.mini.ide` · 当前阶段：**M0 工程脚手架**（空壳，无业务功能）。

## 技术栈基线

| 组件 | 版本 |
| --- | --- |
| Kotlin | 2.2.21 |
| AGP | 8.10.0 |
| Gradle (wrapper) | 8.11.1 |
| Compose BOM | 2026.04.01 |
| Hilt | 2.57.2 |
| Room | 2.8.4 |
| KSP | 2.2.21-2.0.2 |
| JDK | 17 |
| minSdk | 31 (Android 12) |

> 版本组合严格落在 Kotlin 2.2.21 官方兼容区间内。升级时 AGP / Gradle / Kotlin / KSP 需成套调整。

## 模块结构（16 模块）

```
:app                  应用壳 / 装配层（唯一可依赖所有 impl）
:core:common          基础设施（S8）
:core:model           共享模型
:core:designsystem    主题令牌（S2）
:core:uicommon        自绘通用组件
:data:api / impl      数据系统（S3）
:engine:api / impl    核心引擎（S4）
:language:api / impl  语言与语法（S5）
:feature:project / editor / preview  功能模块
:feature:lesson       教学（S6，Phase2 激活）
:extension:api        扩展点契约（S7，仅接口）
:testdouble           Fake 实现
```

## 架构铁律（编译期强制）

`./gradlew checkModuleRules` 校验，且挂到 `check`：

1. 任何模块不得依赖 `:app`
2. `:core:*` 不得依赖业务模块（data/engine/language/feature）
3. `*:api` 不得依赖 `*:impl`
4. `:feature:*` 不得依赖 `*:impl`（只依赖 api，实现经 Hilt 注入）
5. `*:impl` 不得依赖 `:feature:*`

想违反架构 → 构建失败。

## 版本与发版

- 版本真源：`version.properties`（`x.y.z` + `RC_NUMBER`）
- versionCode：`release/versioncode.txt`，每次发布 +1，单调递增
- **测试版** `x.y.z-rcN`：打 tag `v*-rc*` 触发 `.github/workflows/rc.yml`，自由发布
- **正式版** `x.y.z`：手动触发 `.github/workflows/release.yml`，必须先通过排班门禁

### 排班门禁（正式版不可绕过）

正式版发布前，须在 `release/schedule.yaml` 中有 `status: approved` 的记录（仅 owner 可改），
并由 CI 校验：版本存在 / approved / 进入窗口 / 排班人为 owner / sourceRc 匹配。全部通过才构建。

```bash
# 本地预检门禁（不会放行正式版，仅验证脚本逻辑）
python3 scripts/check_schedule.py --version 1.0.0 --schedule release/schedule.yaml
```

## 本地构建

```bash
# 需 JDK 17 + Android SDK（compileSdk 36）
./gradlew build
./gradlew checkModuleRules
```

> 注意：本仓库不含 Android SDK。CI 已配置自动安装 SDK。

## 文档

设计与产品文档见仓库同级目录（或项目 Wiki）：
- 产品与技术方案
- 详细架构设计（工程蓝图）
- MiniMe-IDE 视觉设计规范
- M0 工程脚手架设计
