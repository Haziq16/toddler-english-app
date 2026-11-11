"""Writes build/art-preview.html: every word and scene drawable rendered as SVG, for a quick visual check.

Usage: python scripts/art_preview.py
"""
import pathlib
import re
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parent.parent
DRAW = ROOT / "app/src/main/res/drawable"
OUT = ROOT / "build/art-preview.html"
A = "{http://schemas.android.com/apk/res/android}"


def to_svg(path):
    root = ET.parse(path).getroot()
    w, h = root.get(A + "viewportWidth"), root.get(A + "viewportHeight")
    parts = []
    for p in root.iter("path"):
        fill = p.get(A + "fillColor") or "none"
        stroke = p.get(A + "strokeColor") or "none"
        parts.append(
            f'<path d="{p.get(A + "pathData")}" fill="{fill}" fill-opacity="{p.get(A + "fillAlpha") or 1}" stroke="{stroke}" '
            f'stroke-width="{p.get(A + "strokeWidth") or 0}" stroke-linecap="round" stroke-linejoin="round"/>'
        )
    return f'<svg viewBox="0 0 {w} {h}" xmlns="http://www.w3.org/2000/svg">{"".join(parts)}</svg>'


def main():
    cells = []
    for f in sorted(DRAW.glob("bg_*.xml")) + sorted(DRAW.glob("img_*.xml")):
        name = re.sub(r"^(img|bg)_", "", f.stem)
        cls = "bg" if f.stem.startswith("bg_") else "img"
        cells.append(f'<figure class="{cls}">{to_svg(f)}<figcaption>{name}</figcaption></figure>')
    OUT.parent.mkdir(exist_ok=True)
    OUT.write_text(
        "<!doctype html><meta charset=utf-8><title>Art preview</title><style>"
        "body{font:14px sans-serif;background:#FFF8EC;display:flex;flex-wrap:wrap;gap:8px;padding:8px}"
        "figure{margin:0;width:110px;text-align:center}figure.bg{width:300px}svg{width:100%;background:#fff;border-radius:8px}"
        "</style>" + "".join(cells),
        encoding="utf-8",
    )
    print(OUT)


if __name__ == "__main__":
    main()
