#!/usr/bin/env python3
"""正式版发版门禁：校验排班表。

用法:
  python3 scripts/check_schedule.py --version 1.0.0 --schedule release/schedule.yaml

校验项（全部满足才 exit 0）:
  1. 版本号为 x.y.z（无 -rc 后缀）
  2. 排班表中存在该 version 记录
  3. status == approved
  4. 今天 >= window 日期
  5. scheduledBy == 预期 owner
  6. sourceRc 为合法 x.y.z-rcN 且与 version 主版本一致
任一失败 -> exit 1，CI 据此终止发布。日志格式遵循 docs/CONVENTIONS.md §3。
"""
import argparse
import datetime as dt
import re
import sys

from common_log import error, info

OWNER = "Lisir2002"
SEMVER = re.compile(r"^\d+\.\d+\.\d+$")
RCVER = re.compile(r"^\d+\.\d+\.\d+-rc\d+$")
MODULE = "gate"


def parse_schedule(path):
    """解析极简 YAML：releases 下的 '- key: value' 列表。够本表用，无第三方依赖。"""
    records = []
    current = None
    with open(path, encoding="utf-8") as f:
        for raw in f:
            line = raw.rstrip("\n")
            stripped = line.strip()
            if not stripped or stripped.startswith("#"):
                continue
            if stripped in ("releases: []", "releases:"):
                continue
            if stripped.startswith("- "):
                if current:
                    records.append(current)
                current = {}
                stripped = stripped[2:]
            if current is not None and ":" in stripped:
                key, _, val = stripped.partition(":")
                val = val.strip().strip('"').strip("'")
                current[key.strip()] = val
    if current:
        records.append(current)
    return records


def fail(message: str, **kv) -> int:
    error(MODULE, "发版门禁拒绝：" + message, **kv)
    return 1


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", required=True)
    ap.add_argument("--schedule", default="release/schedule.yaml")
    ap.add_argument("--owner", default=OWNER)
    args = ap.parse_args()

    v = args.version
    if not SEMVER.match(v):
        return fail("版本号必须为 x.y.z（无 rc 后缀）", version=v)

    records = parse_schedule(args.schedule)
    match = next((r for r in records if r.get("version") == v), None)
    if not match:
        return fail("排班表中无该版本记录", version=v)
    if match.get("status") != "approved":
        return fail("状态非 approved", version=v, status=match.get("status"))
    if match.get("scheduledBy") != args.owner:
        return fail("排班人非 owner", version=v, scheduledBy=match.get("scheduledBy"), owner=args.owner)

    window = match.get("window")
    if window:
        try:
            win_date = dt.date.fromisoformat(window)
        except ValueError:
            return fail("window 日期格式错误", version=v, window=window)
        if dt.date.today() < win_date:
            return fail("未到发版窗口", version=v, window=window, today=str(dt.date.today()))

    src = match.get("sourceRc", "")
    if not RCVER.match(src) or not src.startswith(v + "-rc"):
        return fail("sourceRc 不合法或不匹配", version=v, sourceRc=src)

    info(MODULE, "发版门禁通过", version=v, sourceRc=src, scheduledBy=match["scheduledBy"])
    return 0


if __name__ == "__main__":
    sys.exit(main())
