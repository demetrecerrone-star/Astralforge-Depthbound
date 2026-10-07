#!/usr/bin/env python3
"""
Astral Forge skeletal asset prep helper.

This development-time tool extracts disconnected transparent components from
generated rigging sheets. It deliberately preserves every raw component so
semantic mappings can be corrected without regenerating art.

Requires: Pillow, numpy, opencv-python
"""

from pathlib import Path
from PIL import Image
import cv2
import json
import numpy as np

ALPHA_THRESHOLD = 24
MIN_AREA_RATIO = 0.00045

def connected_parts(image: Image.Image):
    rgba = np.array(image.convert("RGBA"))
    alpha = rgba[:, :, 3]
    mask = (alpha > ALPHA_THRESHOLD).astype(np.uint8)
    count, labels, stats, centers = cv2.connectedComponentsWithStats(mask, 8)
    width, height = image.size
    min_area = max(300, int(width * height * MIN_AREA_RATIO))
    parts = []
    for label in range(1, count):
        x, y, w, h, area = stats[label]
        if area < min_area:
            continue
        parts.append({
            "label": int(label),
            "x": int(x), "y": int(y),
            "w": int(w), "h": int(h),
            "area": int(area),
            "cx": float(centers[label][0]),
            "cy": float(centers[label][1])
        })
    parts.sort(key=lambda item: -item["area"])
    for index, part in enumerate(parts):
        part["index"] = index
    return rgba, labels, parts

def save_part(rgba, labels, part, output: Path, padding=8):
    height, width = labels.shape
    x1 = max(0, part["x"] - padding)
    y1 = max(0, part["y"] - padding)
    x2 = min(width, part["x"] + part["w"] + padding)
    y2 = min(height, part["y"] + part["h"] + padding)
    sub = rgba[y1:y2, x1:x2].copy()
    keep = labels[y1:y2, x1:x2] == part["label"]
    sub[:, :, 3] = np.where(keep, sub[:, :, 3], 0).astype(np.uint8)
    output.parent.mkdir(parents=True, exist_ok=True)
    Image.fromarray(sub).save(output)
    return [x1, y1, x2 - x1, y2 - y1]

def split_sheet(source: Path, output_dir: Path):
    image = Image.open(source).convert("RGBA")
    rgba, labels, parts = connected_parts(image)
    output_dir.mkdir(parents=True, exist_ok=True)
    image.save(output_dir / "source_sheet.png")
    index = {}
    for part in parts:
        filename = f"piece_{part['index']:03d}.png"
        bbox = save_part(
            rgba, labels, part,
            output_dir / "parts" / "raw" / filename
        )
        index[str(part["index"])] = {
            "file": f"parts/raw/{filename}",
            "bbox": bbox,
            "area": part["area"]
        }
    (output_dir / "parts").mkdir(exist_ok=True)
    with open(output_dir / "parts" / "index.json", "w") as handle:
        json.dump({"components": index}, handle, indent=2)
    return parts

if __name__ == "__main__":
    print("Use this module from the roster-specific build script.")
