"""Content validator. Placeholder until S1 adds the content schema."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
if not (ROOT / "app/src/main/assets/content/words.json").exists():
    print("No content yet (S0). Nothing to validate.")
    sys.exit(0)
sys.exit("words.json exists but the S1 validator is missing")
