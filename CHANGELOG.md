# Changelog

本项目所有重要变更均记录于此文件。
格式遵循 [Keep a Changelog 1.1.0](https://keepachangelog.com/zh-CN/1.1.0/)，版本遵循 [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)。
提交信息规范见 [docs/CONVENTIONS.md](docs/CONVENTIONS.md)。

## [Unreleased]

### Added
- M0 工程脚手架：16 个 Gradle 模块、版本目录、编译期架构铁律校验
- 本地单元测试：数据模型 / 常量 / 语言契约共 10 个用例
- 工程规范体系：提交 / 发版说明 / 日志 / 文档规范

### Changed
- 修正 KSP 版本（2.2.21-2.0.2 → 2.2.21-2.0.5）、统一库模块 namespace / Kotlin 插件配置

## [0.1.0-rc1] - 2026-11-18

### Added
- 首个测试版脚手架（空壳工程，无业务功能）
- 发版排班门禁：正式版需 owner 排班批准，测试版自由发布
- 应用标识：MiniMe-IDE / `com.mini.ide` / minSdk 31（Android 12）

[Unreleased]: https://github.com/Lisir2002/MiniMe-IDE/compare/v0.1.0-rc1...HEAD
[0.1.0-rc1]: https://github.com/Lisir2002/MiniMe-IDE/releases/tag/v0.1.0-rc1
