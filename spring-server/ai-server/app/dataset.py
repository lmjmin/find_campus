from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from PIL import Image
from torch.utils.data import Dataset


class PairDataset(Dataset):
    """JSONL dataset for positive/negative item pairs.

    Each row:
    {"left_image": "...", "left_text": "...", "right_image": "...", "right_text": "...", "label": 1}
    label=1 means same object, label=0 means different object.
    """

    def __init__(self, jsonl_path: str):
        self.rows: list[dict[str, Any]] = []
        with Path(jsonl_path).open("r", encoding="utf-8") as file:
            for line in file:
                line = line.strip()
                if line:
                    self.rows.append(json.loads(line))

    def __len__(self) -> int:
        return len(self.rows)

    def __getitem__(self, index: int) -> dict[str, Any]:
        row = self.rows[index]
        return {
            "left_image": _load_image(row.get("left_image")),
            "left_text": row.get("left_text", ""),
            "right_image": _load_image(row.get("right_image")),
            "right_text": row.get("right_text", ""),
            "label": float(row.get("label", 0)),
        }


def _load_image(path: str | None) -> Image.Image | None:
    if not path:
        return None
    return Image.open(path).convert("RGB")
