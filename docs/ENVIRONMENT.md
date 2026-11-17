# MiniMe-IDE 环境搭建指南

> 本文记录本地构建环境的**安装、持久化配置与职责分工**。环境变更时同步更新本文。
> 当前基线日期：2026-11-18。

---

## 1. 环境基线

| 组件 | 版本 | 安装方式 | 持久化位置 |
| --- | --- | --- | --- |
| JDK | Temurin 17.0.21 | sdkman | `/root/.sdkman/candidates/java/17.0.21.0+9-tem` |
| Android SDK Platform | android-36 | sdkmanager | `/opt/android-sdk/platforms/android-36` |
| Android Build-Tools | 36.0.0 | sdkmanager | `/opt/android-sdk/build-tools/36.0.0` |
| Platform-Tools (adb) | 37.0.1 | sdkmanager | `/opt/android-sdk/platform-tools` |
| Gradle (wrapper) | 8.11.1 | 工程 wrapper 自动下载 | `~/.gradle/wrapper/dists` |
| Kotlin (Gradle 内置) | 2.2.21 | Gradle 插件 | — |

> 版本组合必须成套：AGP 8.10.0 / Gradle 8.11.1 / Kotlin 2.2.21 / KSP 2.2.21-2.0.5 / JDK 17。升级需成套验证。

---

## 2. 环境变量持久化（已生效）

配置写入 **`/etc/profile.d/minimide-env.sh`**（系统级，登录 shell 自动加载），并在 `/root/.zshrc`、`/root/.bashrc` 各加一行 source（覆盖非登录交互式 shell）。因此**新开终端无需手动 export**。

导出的变量：

```bash
export JAVA_HOME=/root/.sdkman/candidates/java/17.0.21.0+9-tem
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
# PATH 前置：JAVA_HOME/bin、ANDROID_HOME/cmdline-tools/latest/bin、ANDROID_HOME/platform-tools
```

验证持久化（新终端应直接生效）：

```bash
java -version                 # openjdk 17.0.21
echo $ANDROID_HOME            # /opt/android-sdk
sdkmanager --version          # 命令可找到
```

---

## 3. 本地 vs 云端职责分工（强制）

| 动作 | 本地 | GitHub Actions |
| --- | --- | --- |
| JVM 单元测试（`*:testDebugUnitTest`） | ✅ | ✅ 复跑守门 |
| 架构铁律 `checkModuleRules` | ✅ | ✅ |
| 全量测试 / 全模块 assemble | ❌ | ✅ 唯一执行方 |
| RC 构建 / 正式版构建 | ❌ | ✅ 唯一执行方 |
| versionCode 递增 / 打 tag | ❌ | ✅ |

> **本地只做单元测试，全量测试与任何发行构建一律在云端。** 详见 `docs/CONVENTIONS.md` §5。

### 本地常用命令

```bash
# 单元测 + 架构铁律（本地标准动作）
./gradlew :core:common:testDebugUnitTest \
          :core:model:testDebugUnitTest \
          :language:api:testDebugUnitTest \
          checkModuleRules --no-daemon

# 发版门禁脚本自检（本地预检，不会放行正式版）
python3 scripts/check_schedule.py --version 1.0.0 --schedule release/schedule.yaml
python3 scripts/bump_versioncode.py --dry-run
```

> 全量构建与 release 构建**不要在本地执行**，交给 CI。

---

## 4. 已解决的环境问题（备查）

| 问题 | 原因 | 处理 |
| --- | --- | --- |
| Gradle 配置报 KSP 插件找不到 | 版本目录填了不存在的 `2.2.21-2.0.2` | 改为 `2.2.21-2.0.5`（已验证存在） |
| 全局 init.gradle 语法错误 | 原文件 `mavelCentral()` 拼错、URL 缺引号，且与 `FAIL_ON_PROJECT_REPOS` 冲突 | 重命名禁用（`init.gradle.disabled`），仓库由工程 settings 统一管理 |
| 库模块缺 compileSdk / namespace / Kotlin 插件 | M0 脚手架 convention plugin 未接上 | 根 build.gradle.kts 统一配置 + 各模块补插件声明 |
| 源码包名含 `/` | 占位类批量生成时目录分隔符混入包名 | 全量修正 13 个文件的 package/import |

---

*文档版本 v1.0 · MiniMe-IDE 环境搭建指南 · 配置已持久化 · 本地单测/云端发行*
