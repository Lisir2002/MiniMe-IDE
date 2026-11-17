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
任一失败 -> exit 1，CI 据此终止发布。
"""
import argparse
import datetime as dt
import re
import sys

OWNER = "Lisir2002"
SEMVER = re.compile(r"^\d+\.\d+\.\d+$")
RCVER = re.compile(r"^\d+\.\d+\.\d+-rc\d+$")


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
            if stripped == "releases: []":
                continue
            if stripped == "releases:":
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


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", required=True)
    ap.add_argument("--schedule", default="release/schedule.yaml")
    ap.add_argument("--owner", default=OWNER)
    args = ap.parse_args()

    v = args.version
    if not SEMVER.match(v):
        print(f"[门禁失败] 正式版版本号必须符合 x.y.z，收到: {v}")
        return 1

    records = parse_schedule(args.schedule)
    match = next((r for r in records if r.get("version") == v), None)
    if not match:
        print(f"[门禁失败] 排班表中无版本 {v} 的记录")
        return 1
    if match.get("status") != "approved":
        print(f"[门禁失败] 版本 {v} 状态为 {match.get('status')}，非 approved")
        return 1
    if match.get("scheduledBy") != args.owner:
        print(f"[门禁失败] 排班人 {match.get('scheduledBy')} 非 owner {args.owner}")
        return 1

    window = match.get("window")
    if window:
        try:
            win_date = dt.date.fromisoformat(window)
        except ValueError:
            print(f"[门禁失败] window 日期格式错误: {window}")
            return 1
        if dt.date.today() < win_date:
            print(f"[门禁失败] 未到发版窗口 {window}（今天 {dt.date.today()}）")
            return 1

    src = match.get("sourceRc", "")
    if not RCVER.match(src) or not src.startswith(v + "-rc"):
        print(f"[门禁失败] sourceRc {src} 不合法或不匹配版本 {v}")
        return 1

    print(f"[门禁通过] {v} 已排班批准（来源 {src}，排班人 {match['scheduledBy']}）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
