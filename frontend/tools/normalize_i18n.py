#!/usr/bin/env python3
"""Normalize all i18n catalogs to flat, alphabetically-sorted dot-key JSON.

Rewrites every ``src/assets/i18n/*.json`` file in place so that:
  * nested objects become flat dot-notation keys (admin.category.create),
  * arrays are preserved as ordered leaf values (calendar.dayNames, ...),
  * keys are sorted alphabetically for clean, diff-friendly output.

Usage:
    python3 tools/normalize_i18n.py            # normalize all locales
    python3 tools/normalize_i18n.py --check    # verify only, non-zero exit if not normalized

Run from the frontend/ directory (or anywhere; paths are resolved relative
to this file's location).
"""

import json
import sys
from pathlib import Path

from flatten_json import flatten

I18N_DIR = Path(__file__).resolve().parent.parent / "src" / "assets" / "i18n"


def normalized_text(path):
    data = json.loads(path.read_text(encoding="utf-8"))
    flat = dict(sorted(flatten(data).items()))
    return json.dumps(flat, indent=2, ensure_ascii=False, sort_keys=True) + "\n"


def main(argv):
    check_only = "--check" in argv
    files = sorted(I18N_DIR.glob("*.json"))
    if not files:
        print(f"No JSON files found in {I18N_DIR}")
        return 1

    changed = []
    for path in files:
        want = normalized_text(path)
        have = path.read_text(encoding="utf-8")
        if want != have:
            changed.append(path.name)
            if not check_only:
                path.write_text(want, encoding="utf-8")

    if check_only:
        if changed:
            print("Not normalized: " + ", ".join(changed))
            return 1
        print(f"All {len(files)} catalogs already normalized.")
        return 0

    if changed:
        print(f"Normalized {len(changed)}/{len(files)} catalogs: "
              + ", ".join(changed))
    else:
        print(f"All {len(files)} catalogs already normalized.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
