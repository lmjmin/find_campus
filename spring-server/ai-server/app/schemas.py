from __future__ import annotations

from datetime import date, datetime
from typing import Any, List, Optional

from pydantic import BaseModel, ConfigDict, Field, field_validator


def _to_int(value: Any) -> Optional[int]:
    if value is None or value == "":
        return None
    try:
        return int(value)
    except (TypeError, ValueError):
        return None


def _to_date(value: Any) -> Optional[date]:
    if value is None or value == "":
        return None
    if isinstance(value, date) and not isinstance(value, datetime):
        return value
    if isinstance(value, datetime):
        return value.date()
    if isinstance(value, (int, float)):
        seconds = float(value) / 1000.0 if float(value) > 100000000000 else float(value)
        try:
            return datetime.fromtimestamp(seconds).date()
        except (OverflowError, OSError, ValueError):
            return None
    if isinstance(value, str):
        text = value.strip()
        if not text:
            return None
        text = text.replace("Z", "+00:00")
        try:
            return datetime.fromisoformat(text).date()
        except ValueError:
            try:
                return date.fromisoformat(text[:10])
            except ValueError:
                return None
    return None


class ItemPayload(BaseModel):
    model_config = ConfigDict(extra="ignore", coerce_numbers_to_str=True)

    item_type: str = Field(default="lost")
    item_id: int
    title: str = ""
    item_name: str = ""
    category_id: Optional[int] = None
    category_name: Optional[str] = None
    color: Optional[str] = None
    brand: Optional[str] = None
    location_id: Optional[int] = None
    location_name: Optional[str] = None
    location_detail: Optional[str] = None
    item_date: Optional[date] = None
    item_time: Optional[str] = None
    description: Optional[str] = None
    image_url: Optional[str] = None

    @field_validator("item_type", mode="before")
    @classmethod
    def normalize_item_type(cls, value: Any) -> str:
        text = str(value or "").strip().lower()
        return "found" if text == "found" else "lost"

    @field_validator("item_id", mode="before")
    @classmethod
    def normalize_item_id(cls, value: Any) -> int:
        parsed = _to_int(value)
        return parsed if parsed is not None else 0

    @field_validator("category_id", "location_id", mode="before")
    @classmethod
    def normalize_optional_int(cls, value: Any) -> Optional[int]:
        return _to_int(value)

    @field_validator("item_date", mode="before")
    @classmethod
    def normalize_item_date(cls, value: Any) -> Optional[date]:
        return _to_date(value)


class EmbedRequest(BaseModel):
    item: ItemPayload


class EmbedResponse(BaseModel):
    model_name: str
    image_embedding: Optional[List[float]]
    text_embedding: List[float]


class RecommendRequest(BaseModel):
    target: ItemPayload
    candidates: List[ItemPayload]
    top_k: int = Field(default=5, ge=1, le=50)


class ScoreBreakdown(BaseModel):
    image_similarity: float
    text_similarity: float
    metadata_score: float


class Recommendation(BaseModel):
    item: ItemPayload
    score: float
    reason: str
    breakdown: ScoreBreakdown


class RecommendResponse(BaseModel):
    model_name: str
    recommendations: List[Recommendation]