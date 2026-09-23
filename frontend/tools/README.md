# i18n tools

Utility scripts for the CircaBC frontend translation catalogs in
`src/assets/i18n/*.json`.

All catalogs use a **flat, dot-notation, alphabetically-sorted** format, for
example:

```json
{
  "admin.category.create": "Create category",
  "calendar.dayNames": ["Sunday", "Monday", "Tuesday", "..."]
}
```

- Nested objects are flattened to dot keys (`admin` → `admin.category.create`).
- JSON **arrays are kept as ordered leaf values** (e.g. `calendar.dayNames`,
  `monthNames`) because they are consumed by index at runtime — they must not
  be split into `calendar.dayNames.0`, `.1`, ...
- `en.json` is the **pivot** (source of truth); every other locale is expected
  to have exactly the same set of keys.

## Requirements

- Python 3 (no third-party packages).
- Run the commands from the `frontend/` directory. Paths inside the scripts are
  resolved relative to the script location, so they also work from elsewhere.

## Scripts

### `flatten_json.py`

Reusable flatten helpers plus a small CLI. Flattens a single JSON file to
dot-notation, alphabetically-sorted keys. Also imported by the other two
scripts (`flatten`, `load`, `dump_sorted`).

```bash
# Write <input>-flatten.json next to the input
python3 tools/flatten_json.py src/assets/i18n/fr.json

# Write to an explicit output path
python3 tools/flatten_json.py src/assets/i18n/fr.json /tmp/fr-flat.json
```

### `normalize_i18n.py`

Rewrites **all** `src/assets/i18n/*.json` catalogs in place to the canonical
flat + sorted format. Idempotent — running it twice changes nothing.

```bash
# Normalize every catalog in place
python3 tools/normalize_i18n.py

# Verify only: non-zero exit if any catalog is not normalized (CI gate)
python3 tools/normalize_i18n.py --check
```

### `compare_i18n.py`

Compares every locale against the `en.json` pivot and reports per-locale
**missing** keys (present in `en`, absent in the locale) and **extra** keys
(present in the locale, absent in `en`). Exits non-zero if any locale diverges,
so it can be used as a CI parity gate.

```bash
# Full report with per-key details
python3 tools/compare_i18n.py

# Summary line per locale only
python3 tools/compare_i18n.py --quiet
```

Example output:

```
Pivot en.json: 1747 keys

fr.json     1747 keys  missing=0    extra=0    [OK]
de.json     1746 keys  missing=1    extra=0    [DIFF]
    - missing: label.footer.navigation
```

## Typical workflow

1. Edit or add translations in the relevant `src/assets/i18n/*.json` file(s).
2. Run `python3 tools/normalize_i18n.py` to re-flatten and re-sort.
3. Run `python3 tools/compare_i18n.py` to confirm all locales match the pivot.
4. Commit.

## CI suggestion

Add both checks as a gate so catalogs stay normalized and in sync:

```bash
python3 tools/normalize_i18n.py --check   # fails if not normalized
python3 tools/compare_i18n.py --quiet     # fails if any locale diverges from en
```
