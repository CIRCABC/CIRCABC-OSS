#!/usr/bin/env python3
"""Compare every i18n catalog against the English pivot (en.json).

Reports, per locale, keys that are missing (present in en, absent in locale)
and extra (present in locale, absent in en). Exits non-zero if any locale
diverges, so it can be used as a CI parity gate.

Usage:
    python3 tools/compare_i18n.py            # summary + details
    python3 tools/compare_i18n.py --quiet    # summary only
"""

import sys
from pathlib import Path

from flatten_json import flatten, load

I18N_DIR = Path(__file__).resolve().parent.parent / "src" / "assets" / "i18n"
PIVOT = "en"


def main(argv):
    quiet = "--quiet" in argv
    pivot_path = I18N_DIR / f"{PIVOT}.json"
    pivot_keys = set(flatten(load(pivot_path)))
    print(f"Pivot {PIVOT}.json: {len(pivot_keys)} keys\n")

    diverged = False
    for path in sorted(I18N_DIR.glob("*.json")):
        if path.stem == PIVOT:
            continue
        keys = set(flatten(load(path)))
        missing = sorted(pivot_keys - keys)
        extra = sorted(keys - pivot_keys)
        status = "OK" if not missing and not extra else "DIFF"
        if status == "DIFF":
            diverged = True
        print(f"{path.name:<10} {len(keys):>5} keys  "
              f"missing={len(missing):<4} extra={len(extra):<4} [{status}]")
        if not quiet and (missing or extra):
            for k in missing:
                print(f"    - missing: {k}")
            for k in extra:
                print(f"    + extra:   {k}")

    return 1 if diverged else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
