#!/usr/bin/env python3
"""Flatten / unflatten helpers for i18n JSON catalogs.

Provides reusable functions and a small CLI. Objects are flattened to
dot-notation keys; JSON arrays are treated as leaf values and kept intact
(important for index-based catalogs such as calendar.dayNames / monthNames).

CLI usage:
    python3 flatten_json.py <input.json> [output.json]

If no output path is given, "<input>-flatten.json" is written next to the input.
Keys are sorted alphabetically for stable, diff-friendly output.
"""

import json
import sys
from pathlib import Path


def flatten(obj, prefix=""):
    """Turn {"a": {"b": 1}} into {"a.b": 1}. Arrays are kept as-is."""
    out = {}
    for key, value in obj.items():
        full_key = f"{prefix}.{key}" if prefix else key
        if isinstance(value, dict):
            out.update(flatten(value, full_key))
        else:
            out[full_key] = value
    return out


def load(path):
    with Path(path).open(encoding="utf-8") as f:
        return json.load(f)


def dump_sorted(mapping, path):
    """Write a flat mapping as pretty, alphabetically-sorted JSON."""
    with Path(path).open("w", encoding="utf-8") as f:
        json.dump(dict(sorted(mapping.items())), f, indent=2,
                  ensure_ascii=False, sort_keys=True)
        f.write("\n")


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 1
    in_path = Path(argv[1])
    out_path = (
        Path(argv[2]) if len(argv) > 2
        else in_path.with_name(f"{in_path.stem}-flatten.json")
    )
    flat = flatten(load(in_path))
    dump_sorted(flat, out_path)
    print(f"Flattened {len(flat)} keys: {in_path} -> {out_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
