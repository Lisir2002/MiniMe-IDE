# MiniMe-IDE 工程规范（Conventions）

> 本文是仓库内所有**提交、发版说明、日志、文档**格式的唯一事实来源（Single Source of Truth）。
> 任何违反本文的提交，CI 的 `lint-docs` / `lint-commits` 阶段将拒绝合入与发版。
> 版本 v1.0 · 适用于 M0 起全部阶段。

---

## 1. 提交信息规范（Conventional Commits 1.0.0）

### 1.1 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

- **type**（必填）：见 1.2。
- **scope**（可选，推荐）：改动所属模块，用模块名不带冒号，如 `data`、`engine`、`core-model`、`ci`、`docs`。跨模块用 `*` 或省略。
- **subject**（必填）：祈使句、首字母小写、结尾无句号、≤ 50 字符。
- **body**（可选）：说明「为什么」而非「是什么」，每行 ≤ 72 字符。
- **footer**（可选）：`BREAKING CHANGE: ...`、`Refs: #123`、`Co-authored-by: ...`。

### 1.2 type 取值表

| type | 含义 | 是否进入 CHANGELOG | 语义版本影响 |
| --- | --- | --- | --- |
| `feat` | 新功能 | ✅ Added | minor |
| `fix` | 缺陷修复 | ✅ Fixed | patch |
| `perf` | 性能优化 | ✅ Changed | patch |
| `refactor` | 不增功能不改 bug 的重构 | 否（除非破坏性） | — |
| `docs` | 文档 | 否 | — |
| `test` | 测试增改 | 否 | — |
| `build` | 构建/依赖/版本目录 | 否 | — |
| `ci` | CI 配置与流程 | 否 | — |
| `chore` | 杂务（版本递增等） | 否 | — |
| `style` | 纯格式（不改逻辑） | 否 | — |
| `revert` | 回滚 | ✅ Fixed | patch |

> **破坏性变更**：任意 type 后加 `!`（如 `feat(api)!: 重构 Repository 接口`）或在 footer 写 `BREAKING CHANGE:`，触发 **major** 版本。

### 1.3 合法示例

```
feat(data): 实现 Room ProjectRepository 与自动保存
fix(engine): 修复双 debounce 下重复触发预览
feat(api)!: LanguageProfile 增加 compiler 字段

BREAKING CHANGE: LanguageProvider 接口新增 compile() 方法，旧实现需补齐。
```

### 1.4 禁止

- ❌ `更新代码` / `fix bug` / `WIP` / `test`
- ❌ subject 首字母大写或带句号结尾
- ❌ 一个提交混合多个不相关改动（应拆分成多个原子提交）

---

## 2. 发版说明规范（CHANGELOG）

### 2.1 文件与结构

- 路径：仓库根 `CHANGELOG.md`。
- 格式：[Keep a Changelog 1.1.0](https://keepachangelog.com/zh-CN/1.1.0/) + [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)。
- 顶部固定保留：`## [Unreleased]`（未发布区）。
- 每个已发布版本标题：`## [x.y.z] - YYYY-MM-DD`，正式版带 Git 链接；测试版 `## [x.y.z-rcN] - YYYY-MM-DD`。

### 2.2 变更分类（固定顺序）

| 分类 | 对应 type |
| --- | --- |
| `### Added` | feat |
| `### Changed` | perf / 行为变更 |
| `### Deprecated` | 废弃预告 |
| `### Removed` | 移除 |
| `### Fixed` | fix / revert |
| `### Security` | 安全修复 |

每条变更以 `- ` 开头，祈使句，尽量引用提交短 hash：`- 实现数据层 Room 迁移 (`a1b2c3d`)`。

### 2.3 发版流程与 CHANGELOG 的绑定

1. 开发者每次 `feat/fix/perf` 提交时，同步在 `## [Unreleased]` 对应分类下追加条目（CI 校验：存在 feat/fix 提交但 Unreleased 无新增则 `lint-changelog` 警告）。
2. 打 rc tag（测试版）时：CI 自动生成/校验收紧 `## [x.y.z-rcN]` 段，日期为 tag 当日。
3. 正式版发版门禁通过后：把 `## [Unreleased]` 内容移动到 `## [x.y.z] - 今日`，清空 Unreleased，提交后才允许 `assembleRelease`。

---

## 3. 日志格式规范（Structured Logging）

> 目标：本地与 CI 的日志可机读、可检索、可对齐。所有脚本（`scripts/*.py`）与 CI step 均遵循；Kotlin 侧由 `:core:common` 的 `MiniLogger` 统一输出（M1 落地）。

### 3.1 文本格式（人读 + 机读兼容）

```
[2026-11-18T02:48:47+08:00] [INFO ] [gate     ] 排班门禁通过 version=1.0.0 sourceRc=1.0.0-rc3
```

字段按顺序、以空格分隔，**用方括号分区、key=value 携带结构化数据**：

| 字段 | 格式 | 说明 |
| --- | --- | --- |
| 时间戳 | ISO-8601 带时区 `YYYY-MM-DDTHH:mm:ss+08:00` | UTC+8 固定，便于排班窗口比对 |
| 级别 | `VERBOSE/DEBUG/INFO /WARN /ERROR/ASSERT`，补齐到 5/7 字符左对齐 | 见 3.2 |
| 模块标签 | 小写英文，补齐到 8 字符 | `gate`/`build`/`test`/`db`/`engine`/`bridge`/`ui`/`ci` |
| 消息 | 简洁描述 + 尾部 `key=value` 结构化字段 | 值含空格用引号包裹 |

### 3.2 级别使用准则

| 级别 | 用途 | 是否阻断 |
| --- | --- | --- |
| `INFO` | 正常流程节点（构建开始/门禁通过/测试通过） | 否 |
| `WARN` | 可恢复异常（降级、重试、 token 提醒） | 否 |
| `ERROR` | 失败（编译失败、门禁拒绝、测试失败） | 是（CI 该 step 退出非 0） |
| `DEBUG` | 排障细节，仅本地/RC | 否 |

**硬规则**：
- ❌ 任何日志不得输出 token / 密码 / 密钥 / 用户源码全文。
- ✅ 门禁拒绝、构建失败必须以 `ERROR` 级别输出，并给出可操作的原因。
- ✅ 数字/版本/计数一律用 `key=value`（如 `errors=3`、`version=1.0.0`），禁止揉进句子。

### 3.3 脚本实现约定

`scripts/` 下 Python 脚本统一用一个 `log()` 函数输出（M0 先把 `check_schedule.py` 改造为示范），签名：

```python
def log(level: str, module: str, message: str, **kv) -> None:
    ts = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec="seconds")
    fields = " ".join(f"{k}={v}" for k, v in kv.items())
    print(f"[{ts}] [{level:<7}] [{module:<8}] {message} {fields}".rstrip())
```

---

## 4. 文档规范（Docs）

| 文档 | 职责 | 位置 | 更新时机 |
| --- | --- | --- | --- |
| `README.md` | 项目门面：一句话定位、快速开始、模块地图、规范入口 | 仓库根 | 里程碑/构建方式变化时 |
| `docs/CONVENTIONS.md` | 本文：提交/发版/日志/文档格式 | `docs/` | 规范变更时（需 `docs:` 提交） |
| `CHANGELOG.md` | 变更与发版历史 | 仓库根 | 每次 feat/fix 与每个版本 |
| `docs/ARCHITECTURE.md` | 系统分层与契约索引 | `docs/` | 架构契约变更时 |
| `docs/ENVIRONMENT.md` | 本地/云端环境搭建与持久化配置 | `docs/` | 环境基线变化时 |

**README 专业性要求**：必须有 badge 位（CI 状态/版本）、一句话定位、技术栈表、快速开始（本地单测命令）、职责分工说明、规范与文档链接、许可证位。**禁止**在 README 出现 token、个人邮箱、临时路径。

---

## 5. 本地 / 云端职责分工（强制）

| 动作 | 本地（沙箱/开发者机） | GitHub Actions 云端 |
| --- | --- | --- |
| JVM 单元测试（`*:test`） | ✅ 必跑 | ✅ 复跑（守门） |
| 静态检查（lint / checkModuleRules） | ✅ 必跑 | ✅ 复跑 |
| 全量测试（含 connectedAndroidTest / 全模块 assemble） | ❌ 不做 | ✅ 唯一执行方 |
| RC 构建 `assembleDebug`/`assembleRc` | ❌ 不做 | ✅ 唯一执行方 |
| 正式版 `assembleRelease` | ❌ 禁止 | ✅ 门禁通过后执行 |
| versionCode 递增 / 打 tag | ❌ 不做 | ✅ CI 执行 |

> 记住：**本地只做单元测试，全量测试与任何发行构建一律在 GitHub 云端**。本地放宽是为了快反馈，云端收口是为了可控与可审计。

---

*文档版本 v1.0 · MiniMe-IDE 工程规范 · 提交 Conventional Commits · 发版 Keep a Changelog · 日志结构化 · 本地单测/云端发行*
