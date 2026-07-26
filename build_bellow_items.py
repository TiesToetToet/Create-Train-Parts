import json
from pathlib import Path

# Generates the bellow item models from the authored 1x2 frame.
# The frame members stay one pixel thick, only their positions change,
# and the display scales shrink so larger frames still fit an item slot.

ROOT = Path(__file__).resolve().parent
BASE = ROOT / "src" / "main" / "resources" / "assets" / "create_train_parts" / \
       "models" / "item" / "bellow.json"
OUT_DIR = ROOT / "src" / "main" / "resources" / "assets" / "create_train_parts" / \
          "models" / "block" / "bellow"

SIZES = [(1, 2), (2, 2), (2, 3), (3, 2), (3, 3)]

FACES = json.loads(BASE.read_text())["elements"]
DISPLAY = json.loads(BASE.read_text())["display"]
TEXTURES = json.loads(BASE.read_text())["textures"]
PARENT = json.loads(BASE.read_text())["parent"]


def elements(width, height):
    left = 8 - 8 * width
    right = 8 + 8 * width
    bottom = 8 - 8 * height
    top = 8 + 8 * height
    boxes = [
        (left, bottom, right, bottom + 1),
        (right - 1, bottom + 1, right, top - 1),
        (left, bottom + 1, left + 1, top - 1),
        (left, top - 1, right, top),
    ]
    result = []
    for source, (x1, y1, x2, y2) in zip(FACES, boxes):
        result.append({
            "from": [x1, y1, 6],
            "to": [x2, y2, 10],
            "faces": source["faces"],
        })
    return result


def display(width, height):
    factor = 2 / max(width, height)
    scaled = {}
    for name, entry in DISPLAY.items():
        copy = dict(entry)
        if "scale" in copy:
            copy["scale"] = [round(value * factor, 5) for value in copy["scale"]]
        scaled[name] = copy
    return scaled


for width, height in SIZES:
    model = {
        "credit": "Made with Blockbench",
        "parent": PARENT,
        "textures": TEXTURES,
        "elements": elements(width, height),
        "display": display(width, height),
    }
    target = OUT_DIR / f"item_{width}x{height}.json"
    target.write_text(json.dumps(model, indent=4))
    print(f"wrote {target.relative_to(ROOT)}")
