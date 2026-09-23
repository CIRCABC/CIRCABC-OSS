#!/usr/bin/env bash
# Run all Playwright tests that come AFTER a given test file (sorted alphabetically).
# Usage: ./scripts/run-tests-after.sh <test-file-or-prefix>
# Example: ./scripts/run-tests-after.sh 56b
#          ./scripts/run-tests-after.sh tests/56b-library-file-details-page.spec.ts

set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: $0 <test-file-or-prefix> [extra playwright args...]"
  echo "Example: $0 56b"
  echo "         $0 tests/56b-library-file-details-page.spec.ts"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TESTS_DIR="$SCRIPT_DIR/tests"

AFTER="$1"
shift

# If a full path or filename was given, extract just the basename for comparison
if [[ "$AFTER" == *"/"* ]]; then
  AFTER="$(basename "$AFTER")"
fi

# If just a prefix number was given (e.g. "56b"), find the matching file
if [[ ! "$AFTER" == *.spec.ts ]]; then
  MATCH=$(ls "$TESTS_DIR" | grep -E "^${AFTER}-.*\.spec\.ts$" | head -1 || true)
  if [ -z "$MATCH" ]; then
    echo "Error: No test file found matching prefix '$AFTER'" >&2
    exit 1
  fi
  AFTER="$MATCH"
fi

# Get all test files sorted, then pick those that come strictly after the given one
FILES_AFTER=()
while IFS= read -r file; do
  if [[ "$file" > "$AFTER" ]]; then
    FILES_AFTER+=("tests/$file")
  fi
done < <(ls "$TESTS_DIR" | grep '\.spec\.ts$' | sort)

if [ ${#FILES_AFTER[@]} -eq 0 ]; then
  echo "No tests found after '$AFTER'"
  exit 0
fi

echo "Running ${#FILES_AFTER[@]} tests after '$AFTER':"
printf "  %s\n" "${FILES_AFTER[@]}" | head -5
if [ ${#FILES_AFTER[@]} -gt 5 ]; then
  echo "  ... and $((${#FILES_AFTER[@]} - 5)) more"
fi
echo ""

cd "$SCRIPT_DIR"
exec npx playwright test "${FILES_AFTER[@]}" --max-failures=1 "$@"
