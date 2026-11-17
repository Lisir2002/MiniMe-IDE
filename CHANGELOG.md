# Changelog

本项目所有重要变更均记录于此文件。
格式遵循 [Keep a Changelog 1.1.0](https://keepachangelog.com/zh-CN/1.1.0/)，版本遵循 [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)。
提交信息规范见 [docs/CONVENTIONS.md](docs/CONVENTIONS.md)。

## [Unreleased]

### Added
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
- 修正 KSP 版本（2.2.21-2.0.2 → 2.2.21-2.0.5）、统一库模块 namespace / Kotlin 插件配置
- 补齐 gradle.properties（useAndroidX 等），Robolectric 单测固定模拟 SDK 34（Java 17 兼容）

## [0.1.0-rc1] - 2026-11-18

### Added
- 首个测试版脚手架（空壳工程，无业务功能）
- 发版排班门禁：正式版需 owner 排班批准，测试版自由发布
- 应用标识：MiniMe-IDE / `com.mini.ide` / minSdk 31（Android 12）

[Unreleased]: https://github.com/Lisir2002/MiniMe-IDE/compare/v0.1.0-rc1...HEAD
[0.1.0-rc1]: https://github.com/Lisir2002/MiniMe-IDE/releases/tag/v0.1.0-rc1
