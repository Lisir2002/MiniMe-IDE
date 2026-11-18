# Changelog

本项目所有重要变更均记录于此文件。
格式遵循 [Keep a Changelog 1.1.0](https://keepachangelog.com/zh-CN/1.1.0/)，版本遵循 [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)。
提交信息规范见 [docs/CONVENTIONS.md](docs/CONVENTIONS.md)。

## [Unreleased]

### Added
- M5 教学增强（Phase2）：S6 教学系统激活 + 错误提示结构化（`9152fa8` 之后）
  - `:core:model`：Lesson / LessonStep / ValidatorSpec / LessonTarget / CheckContext / StepResult / LessonProgress / LessonState 领域模型；Settings 新增 `lessonEnabled` 开关
  - `:data:impl`：Room 升级 v2，新增 lessons / lesson_progress 两张表 + LessonDao + LessonRepositoryImpl + 2 节内置课程（第一个网页 / 给文字上色），幂等 seed
  - `:engine:api/:impl`：LessonEngine（课程加载 / 数据驱动分步校验 / 进度推进 / 重置）+ StepValidator（Contains/Regex/AstRule 教学向宽松校验）
  - DiagnosticEngine + DiagnosticRules：把运行时/控制台报错升级为「分类 + 中文教学建议 + 行号」结构化诊断，规则表驱动、纯 JVM 可测
  - `:feature:lesson`：LessonViewModel + 自绘课程列表/详情分步 UI（指令/提示卡片/校验反馈/进度条/启用引导）
  - `:feature:preview` 错误提示增强：日志带分类标签/教学建议卡片/来源行号、错误/警告计数徽标、分类过滤、新错误自动展开
  - 工作区顶栏新增常驻教程入口（设计说明决策 6）
  - M5 本地单测：教学仓库 / StepValidator / DiagnosticEngine / LessonEngine / LessonViewModel / 诊断接入（共 37 用例）
- M4 MVP 闭环：项目列表 → 编辑 → 即时预览 → 保存端到端打通（`e7f347f` 之后）
  - S2 自绘组件库 `:core:uicommon`：磨砂玻璃/自绘按钮/卡片/顶栏/标签栏/语义化 SVG 图标/spring 动效/排版令牌
  - `:feature:project`：项目列表 ViewModel + 自绘卡片 UI，模板实例化 seed，打开信号导航
  - `:feature:editor`：编辑 ViewModel + 文件 Tab 工具栏 + WebView 适配端口；脏标记/自动保存/文件切换
  - `:feature:preview`：预览 ViewModel + 独立预览桥 + 控制台日志面板；300ms 防抖即时渲染
  - 离线编辑器内核（透明 textarea + 语法高亮镜像），完整实现 Bridge 协议，可无缝替换 CodeMirror 6
  - 预览桥接脚本注入器：劫持 console/error 经独立 AndroidBridge 回传，纯函数可 JVM 单测
  - `LanguageProvider` 只读端口（language:api），feature 按 FileKind 取语言配置，遵守"不依赖 impl"铁律
  - M4 本地单测：三个 feature 的 ViewModel + 桥接脚本注入（共 14 用例）
- M2 引擎层：S4 四大引擎（Bridge/Editor/Render/Project）+ S5 语言系统（原 M3 提前合并，语言是引擎依赖）
  - 桥接协议编解码（Base64 文本传输 + JSON 消息）、主题序列化、预览 HTML 拼装
  - 两个可测性端口：`WebViewPort`（解耦 Android WebView）、`EditorTheme`（跨层纯数据，解耦 Compose）
  - 项目引擎内存脏镜像：编辑即预览、自动保存写回、数据库刷新不冲掉未保存内容
  - HTML/CSS/JS 三套教学向 Profile + LanguageRegistry（Phase3 自研 DSL 可插拔，编辑器零改动）
  - M2 本地单测：编解码 / 主题 / HTML 拼装 / 四引擎行为 / 项目编排 / 语言注册表（共 40 用例，JVM 可测）
- M1 数据系统：Room 数据库（projects / source_files / templates）、DAO、四个 Repository 实现
- 内置模板 seed（空白三件套 / Hello World）、ContentStore 可替换抽象、SettingsRepository
- M1 本地单测：Repository CRUD / 模板实例化 / 级联删除 / JSON 往返（Robolectric + Room 内存库）
- M0 工程脚手架：16 个 Gradle 模块、版本目录、编译期架构铁律校验
- 本地单元测试：数据模型 / 常量 / 语言契约共 10 个用例
- 工程规范体系：提交 / 发版说明 / 日志 / 文档规范

### Changed
- Lesson.target 用纯数据 LessonTarget 替代蓝图里的 engine:api.PreviewDocument，避免 core:model 反向依赖（M5 设计决策 1）
- PreviewLog 升级为结构化诊断（新增 category/title/suggestion 字段，均带默认值向后兼容）
- EditorEngine / RenderEngine 接口继承 BridgeHandler，app 层统一装配 Fanout 桥接处理器
- 补全 Hilt 依赖图：Repository impl / DbContentStore / DefaultDispatcherProvider 改由 Module @Provides 构造
- 修正 KSP 版本（2.2.21-2.0.2 → 2.2.21-2.0.5）、统一库模块 namespace / Kotlin 插件配置
- 补齐 gradle.properties（useAndroidX 等），Robolectric 单测固定模拟 SDK 34（Java 17 兼容）

## [0.1.0-rc1] - 2026-11-18

### Added
- 首个测试版脚手架（空壳工程，无业务功能）
- 发版排班门禁：正式版需 owner 排班批准，测试版自由发布
- 应用标识：MiniMe-IDE / `com.mini.ide` / minSdk 31（Android 12）

[Unreleased]: https://github.com/Lisir2002/MiniMe-IDE/compare/v0.1.0-rc1...HEAD
[0.1.0-rc1]: https://github.com/Lisir2002/MiniMe-IDE/releases/tag/v0.1.0-rc1
