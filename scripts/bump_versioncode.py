#!/usr/bin/env python3
"""versionCode 递增：每次成功发布后 +1，保证单调递增、可审计。"""
from pathlib import Path
import sys

path = Path("release/versioncode.txt")
try:
    current = int(path.read_text(encoding="utf-8").strip())
except Exception as e:
    print(f"[失败] 读取 versioncode.txt 出错: {e}")
    sys.exit(1)

path.write_text(str(current + 1), encoding="utf-8")
print(f"versionCode: {current} -> {current + 1}")
