import re
import sys
import zipfile
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")


def extract_text(path: str) -> str:
    with zipfile.ZipFile(path) as zf:
        data = zf.read("word/document.xml").decode("utf-8", "ignore")
    return " ".join(re.findall(r"<w:t[^>]*>(.*?)</w:t>", data))


def main() -> int:
    for raw_path in sys.argv[1:]:
        path = Path(raw_path)
        print(f"=====FILE===== {path.name}")
        print(extract_text(str(path))[:50000])
        print()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
