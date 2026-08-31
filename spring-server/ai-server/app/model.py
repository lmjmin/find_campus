from __future__ import annotations

from functools import lru_cache
from io import BytesIO
import hashlib
from typing import Optional

import numpy as np
import requests
import torch
from PIL import Image
from transformers import CLIPModel, CLIPProcessor


DEFAULT_MODEL_NAME = "openai/clip-vit-base-patch32"


def _normalize(vector: np.ndarray) -> np.ndarray:
    norm = np.linalg.norm(vector)
    if norm == 0:
        return vector.astype(np.float32)
    return (vector / norm).astype(np.float32)


def _hash_embedding(value: str, dimensions: int = 512) -> list[float]:
    seed = (value or "unknown campus item").encode("utf-8", errors="ignore")
    chunks = []
    counter = 0
    while len(chunks) < dimensions:
        digest = hashlib.sha256(seed + str(counter).encode("ascii")).digest()
        chunks.extend((byte / 127.5) - 1.0 for byte in digest)
        counter += 1
    return _normalize(np.asarray(chunks[:dimensions], dtype=np.float32)).tolist()


class EmbeddingModel:
    def __init__(self, model_name: str = DEFAULT_MODEL_NAME, device: Optional[str] = None):
        self.model_name = model_name
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        self.model = None
        self.processor = None
        try:
            self.model = CLIPModel.from_pretrained(model_name).to(self.device)
            self.processor = CLIPProcessor.from_pretrained(model_name)
            self.model.eval()
        except Exception as exc:
            print(f"[findcampus-ai] CLIP load failed, using hash fallback: {exc}")

    def text_embedding(self, text: str) -> list[float]:
        safe_text = text.strip() or "unknown campus lost and found item"
        if self.model is None or self.processor is None:
            return _hash_embedding(safe_text)
        inputs = self.processor(text=[safe_text], return_tensors="pt", padding=True, truncation=True)
        inputs = {key: value.to(self.device) for key, value in inputs.items()}
        with torch.no_grad():
            features = self.model.get_text_features(**inputs)
        vector = features[0].detach().cpu().numpy()
        return _normalize(vector).tolist()

    def image_embedding(self, image_url: Optional[str]) -> Optional[list[float]]:
        if not image_url:
            return None
        if self.model is None or self.processor is None:
            return _hash_embedding(image_url)
        try:
            image = self._load_image(image_url)
        except Exception:
            return None

        inputs = self.processor(images=image, return_tensors="pt")
        inputs = {key: value.to(self.device) for key, value in inputs.items()}
        with torch.no_grad():
            features = self.model.get_image_features(**inputs)
        vector = features[0].detach().cpu().numpy()
        return _normalize(vector).tolist()

    def _load_image(self, image_url: str) -> Image.Image:
        if image_url.startswith("http://") or image_url.startswith("https://"):
            response = requests.get(image_url, timeout=8)
            response.raise_for_status()
            return Image.open(BytesIO(response.content)).convert("RGB")

        return Image.open(image_url).convert("RGB")


@lru_cache(maxsize=1)
def get_model(model_name: str = DEFAULT_MODEL_NAME) -> EmbeddingModel:
    return EmbeddingModel(model_name=model_name)