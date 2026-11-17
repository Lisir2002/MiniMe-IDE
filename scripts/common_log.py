#!/usr/bin/env python3
"""统一结构化日志工具（见 docs/CONVENTIONS.md §3）。

所有 scripts/*.py 必须经此模块输出日志，保证时间戳/级别/模块标签/kv 字段一致。
格式: [ISO8601+08:00] [LEVEL  ] [module  ] message key=value ...
"""
from datetime import datetime, timedelta, timezone

CST = timezone(timedelta(hours=8))
_LEVEL_WIDTH = 7
_MODULE_WIDTH = 8


def _now() -> str:
    return datetime.now(CST).isoformat(timespec="seconds")


def log(level: str, module: str, message: str, **kv) -> None:
    """输出一条结构化日志。level ∈ {VERBOSE,DEBUG,INFO,WARN,ERROR,ASSERT}。"""
    fields = " ".join(f"{k}={v}" for k, v in kv.items())
    line = f"[{_now()}] [{level:<{_LEVEL_WIDTH}}] [{module:<{_MODULE_WIDTH}}] {message}"
    if fields:
        line += " " + fields
    print(line.rstrip())


def info(module: str, message: str, **kv) -> None:
    log("INFO", module, message, **kv)


def warn(module: str, message: str, **kv) -> None:
    log("WARN", module, message, **kv)


def error(module: str, message: str, **kv) -> None:
    log("ERROR", module, message, **kv)


def debug(module: str, message: str, **kv) -> None:
    log("DEBUG", module, message, **kv)
