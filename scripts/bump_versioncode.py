#!/usr/bin/env python3
"""versionCode 递增：每次成功发布后 +1，保证单调递增、可审计。

用法:
  python3 scripts/bump_versioncode.py            # +1
  python3 scripts/bump_versioncode.py --dry-run  # 仅打印，不写入（本地校验用）
日志格式遵循 docs/CONVENTIONS.md §3。
"""
import argparse
from pathlib import Path
import sys

from common_log import error, info

MODULE = "build"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true", help="仅打印预期值，不修改文件")
    args = ap.parse_args()

    path = Path("release/versioncode.txt")
    try:
        current = int(path.read_text(encoding="utf-8").strip())
    except Exception as e:
        error(MODULE, "读取 versioncode.txt 失败", error=str(e))
        return 1

    nxt = current + 1
    if args.dry_run:
        info(MODULE, "versionCode 递增预演（未写入）", old=current, new=nxt)
        return 0

    path.write_text(str(nxt), encoding="utf-8")
    info(MODULE, "versionCode 已递增", old=current, new=nxt)
    return 0


if __name__ == "__main__":
    sys.exit(main())
