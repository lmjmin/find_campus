from __future__ import annotations

from datetime import date, datetime
from difflib import SequenceMatcher
from typing import Optional
import re

import numpy as np

from app.schemas import ItemPayload, Recommendation, ScoreBreakdown


MIN_VISIBLE_SCORE = 0.60

_MEANINGLESS = {
    "\u3147", "\u3141", "\u3134", "\u3137", "\u3131", "\u314b", "\u314e", "\u3160", "\u315c",
    "o", "x", "-", ".", ",", "test", "none", "null",
    "\ud14c\uc2a4\ud2b8", "\uc5c6\uc74c", "\ubaa8\ub984", "\ubbf8\uc785\ub825",
}

_STOPWORDS = {
    "\ubd84\uc2e4\ubb3c", "\uc2b5\ub4dd\ubb3c", "\ubd84\uc2e4", "\uc2b5\ub4dd", "\ucc3e\uc544\uc694",
    "\ucc3e\uc2b5\ub2c8\ub2e4", "\ubcf4\uc2e0", "\ubd84", "\uc5f0\ub77d", "\uc8fc\uc138\uc694",
    "\ubcf4\uad00\uc911", "\uc811\uc218\uc911", "\ubc18\ud658\uc644\ub8cc", "\uc120\ud0dd",
}

_ALIASES = {
    "\ubcf4\uc870\ubc30\ud130\ub9ac": "\ubcf4\uc870\ubc30\ud130\ub9ac",
    "\ubcf4\uc870 \ubc30\ud130\ub9ac": "\ubcf4\uc870\ubc30\ud130\ub9ac",
    "\ud734\ub300\uc6a9\ubc30\ud130\ub9ac": "\ubcf4\uc870\ubc30\ud130\ub9ac",
    "\ud734\ub300\uc6a9 \ubc30\ud130\ub9ac": "\ubcf4\uc870\ubc30\ud130\ub9ac",
    "\ubcf4\ubc30": "\ubcf4\uc870\ubc30\ud130\ub9ac",
    "powerbank": "\ubcf4\uc870\ubc30\ud130\ub9ac",
    "power bank": "\ubcf4\uc870\ubc30\ud130\ub9ac",
    "\uc5d0\uc5b4\ud31f": "\uc5d0\uc5b4\ud31f",
    "\uc5d0\uc5b4\ud31f\ud504\ub85c": "\uc5d0\uc5b4\ud31f",
    "airpod": "\uc5d0\uc5b4\ud31f",
    "airpods": "\uc5d0\uc5b4\ud31f",
    "\ub178\ud2b8\ubd81": "\ub178\ud2b8\ubd81",
    "laptop": "\ub178\ud2b8\ubd81",
    "\ub9c8\uc6b0\uc2a4": "\ub9c8\uc6b0\uc2a4",
    "mouse": "\ub9c8\uc6b0\uc2a4",
    "\uc548\uacbd": "\uc548\uacbd",
    "glasses": "\uc548\uacbd",
    "\uc9c0\uac11": "\uc9c0\uac11",
    "wallet": "\uc9c0\uac11",
    "\uc6b0\uc0b0": "\uc6b0\uc0b0",
    "umbrella": "\uc6b0\uc0b0",
}


def _clamp(value: float) -> float:
    return max(0.0, min(1.0, float(value)))


def _clean(value: Optional[str]) -> str:
    return (value or "").strip()


def _strip_location_number(value: str) -> str:
    return re.sub(r"^\s*\d+\.\s*", "", value or "").strip()


def _normalize(value: Optional[str]) -> str:
    text = _strip_location_number(_clean(value)).lower()
    text = re.sub(r"[\[\]\(\)\{\}/_,.:;|~!@#$%^&*+=?<>\"'`-]+", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    if text in _ALIASES:
        return _ALIASES[text]
    compact = text.replace(" ", "")
    return _ALIASES.get(compact, compact)


def _meaningful(value: Optional[str], min_len: int = 2) -> bool:
    text = _normalize(value)
    if len(text) < min_len:
        return False
    return text not in _MEANINGLESS


def _tokens(*values: Optional[str]) -> set[str]:
    tokens: set[str] = set()
    for value in values:
        if not _meaningful(value, 2):
            continue
        text = _normalize(value)
        if text in _ALIASES.values():
            tokens.add(text)
        for token in re.split(r"\s+", _strip_location_number(_clean(value)).lower()):
            normalized = _normalize(token)
            if len(normalized) >= 2 and normalized not in _STOPWORDS and normalized not in _MEANINGLESS:
                tokens.add(normalized)
    return tokens


def item_text(item: ItemPayload) -> str:
    parts = [
        item.item_name,
        item.title,
        item.category_name,
        item.color,
        item.brand,
        item.location_name,
        item.location_detail,
        item.description,
    ]
    meaningful_parts = [part.strip() for part in parts if _meaningful(part)]
    return " ".join(meaningful_parts)


def has_meaningful_text(item: ItemPayload) -> bool:
    return any(
        _meaningful(value, 2)
        for value in [item.title, item.item_name, item.color, item.brand, item.location_detail, item.description]
    )


def information_score(item: ItemPayload) -> float:
    score = 0.0
    if _meaningful(item.title, 2):
        score += 0.16
    if _meaningful(item.item_name, 2):
        score += 0.26
    if _meaningful(item.description, 8):
        score += 0.12
    if _meaningful(item.category_name, 2) or item.category_id is not None:
        score += 0.14
    if _meaningful(item.color, 2):
        score += 0.09
    if _meaningful(item.brand, 2):
        score += 0.06
    if _meaningful(item.location_name, 2) or item.location_id is not None:
        score += 0.09
    if _meaningful(item.location_detail, 2):
        score += 0.03
    if item.item_date is not None:
        score += 0.04
    if _meaningful(getattr(item, "item_time", None), 2):
        score += 0.01
    return _clamp(score)


def cosine_similarity(left: Optional[list[float]], right: Optional[list[float]]) -> float:
    if left is None or right is None:
        return 0.0
    left_array = np.asarray(left, dtype=np.float32)
    right_array = np.asarray(right, dtype=np.float32)
    denom = float(np.linalg.norm(left_array) * np.linalg.norm(right_array))
    if denom == 0:
        return 0.0
    return _clamp(float(np.dot(left_array, right_array) / denom))


def _coerce_date(value) -> Optional[date]:
    if value is None:
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
        text = value.strip().replace("Z", "+00:00")
        if not text:
            return None
        try:
            return datetime.fromisoformat(text).date()
        except ValueError:
            try:
                return date.fromisoformat(text[:10])
            except ValueError:
                return None
    return None


def _date_score(left: date, right: date) -> float:
    days = abs((left - right).days)
    if days == 0:
        return 1.0
    if days <= 1:
        return 0.86
    if days <= 3:
        return 0.62
    if days <= 7:
        return 0.32
    return 0.0


def _time_minutes(value: Optional[str]) -> Optional[int]:
    if not value:
        return None
    match = re.search(r"(\d{1,2})\s*:\s*(\d{2})", str(value))
    if not match:
        return None
    hour = int(match.group(1))
    minute = int(match.group(2))
    if hour < 0 or hour > 23 or minute < 0 or minute > 59:
        return None
    return (hour * 60) + minute


def _time_score(left: Optional[str], right: Optional[str]) -> float:
    left_min = _time_minutes(left)
    right_min = _time_minutes(right)
    if left_min is None or right_min is None:
        return 0.0
    diff = abs(left_min - right_min)
    if diff == 0:
        return 1.0
    if diff <= 30:
        return 0.85
    if diff <= 120:
        return 0.55
    if diff <= 360:
        return 0.20
    return 0.0


def _field_similarity(left: Optional[str], right: Optional[str]) -> float:
    if not (_meaningful(left, 2) and _meaningful(right, 2)):
        return 0.0
    l = _normalize(left)
    r = _normalize(right)
    if l == r:
        return 1.0
    if len(l) >= 2 and len(r) >= 2 and (l in r or r in l):
        shorter = min(len(l), len(r))
        longer = max(len(l), len(r))
        return max(0.82, shorter / longer)
    l_tokens = _tokens(left)
    r_tokens = _tokens(right)
    if l_tokens and r_tokens:
        overlap = len(l_tokens & r_tokens) / len(l_tokens | r_tokens)
        if overlap > 0:
            return max(overlap, SequenceMatcher(None, l, r).ratio() * 0.75)
    return SequenceMatcher(None, l, r).ratio()


def _category_similarity(target: ItemPayload, candidate: ItemPayload) -> float:
    if target.category_id is not None and candidate.category_id is not None:
        return 1.0 if target.category_id == candidate.category_id else 0.0
    return _field_similarity(target.category_name, candidate.category_name)


def _item_name_similarity(target: ItemPayload, candidate: ItemPayload) -> float:
    return max(
        _field_similarity(target.item_name, candidate.item_name),
        _field_similarity(target.item_name, candidate.title),
        _field_similarity(target.title, candidate.item_name),
    )


def _token_overlap_score(target: ItemPayload, candidate: ItemPayload) -> float:
    target_tokens = _tokens(target.title, target.item_name, target.description, target.color, target.brand)
    candidate_tokens = _tokens(candidate.title, candidate.item_name, candidate.description, candidate.color, candidate.brand)
    if not target_tokens or not candidate_tokens:
        return 0.0
    return len(target_tokens & candidate_tokens) / len(target_tokens | candidate_tokens)


def lexical_score(target: ItemPayload, candidate: ItemPayload) -> float:
    item_name_score = _item_name_similarity(target, candidate)
    title_score = _field_similarity(target.title, candidate.title)
    category_score = _category_similarity(target, candidate)
    color_score = _field_similarity(target.color, candidate.color)
    brand_score = _field_similarity(target.brand, candidate.brand)
    token_score = _token_overlap_score(target, candidate)

    score = (
        (0.50 * item_name_score)
        + (0.14 * title_score)
        + (0.16 * category_score)
        + (0.08 * color_score)
        + (0.05 * brand_score)
        + (0.07 * token_score)
    )
    return _clamp(score)


def identity_score(target: ItemPayload, candidate: ItemPayload) -> float:
    item_name_score = _item_name_similarity(target, candidate)
    category_score = _category_similarity(target, candidate)
    title_score = _field_similarity(target.title, candidate.title)
    token_score = _token_overlap_score(target, candidate)
    return _clamp(max(item_name_score, (0.70 * item_name_score) + (0.16 * category_score) + (0.08 * title_score) + (0.06 * token_score)))


def metadata_score(target: ItemPayload, candidate: ItemPayload) -> float:
    score = 0.0
    weight = 0.0

    category = _category_similarity(target, candidate)
    if category > 0:
        weight += 0.30
        score += 0.30 * category

    if _meaningful(target.color) and _meaningful(candidate.color):
        weight += 0.16
        score += 0.16 * _field_similarity(target.color, candidate.color)

    if _meaningful(target.brand) and _meaningful(candidate.brand):
        weight += 0.08
        score += 0.08 * _field_similarity(target.brand, candidate.brand)

    if target.location_id is not None and candidate.location_id is not None:
        weight += 0.22
        score += 0.22 if target.location_id == candidate.location_id else 0.0
    elif _meaningful(target.location_name) and _meaningful(candidate.location_name):
        weight += 0.22
        score += 0.22 * _field_similarity(target.location_name, candidate.location_name)

    target_date = _coerce_date(target.item_date)
    candidate_date = _coerce_date(candidate.item_date)
    if target_date and candidate_date:
        weight += 0.16
        score += 0.16 * _date_score(target_date, candidate_date)

    time_score = _time_score(getattr(target, "item_time", None), getattr(candidate, "item_time", None))
    if time_score > 0:
        weight += 0.08
        score += 0.08 * time_score

    if weight == 0:
        return 0.0
    return _clamp(score / weight)


def direct_match_boost(target: ItemPayload, candidate: ItemPayload, identity: float, metadata: float) -> float:
    name_score = _field_similarity(target.item_name, candidate.item_name)
    same_name = _meaningful(target.item_name, 2) and name_score >= 0.96
    same_category = _category_similarity(target, candidate) >= 0.90
    same_or_close_location = (
        (target.location_id is not None and candidate.location_id is not None and target.location_id == candidate.location_id)
        or _field_similarity(target.location_name, candidate.location_name) >= 0.88
    )

    if same_name and same_category and metadata >= 0.78:
        return 0.93
    if same_name and same_category:
        return 0.88
    if same_name and metadata >= 0.62:
        return 0.84
    if identity >= 0.86 and same_category and same_or_close_location:
        return 0.82
    return 0.0


def apply_information_penalty(raw_score: float, target: ItemPayload, candidate: ItemPayload, boosted: bool = False) -> float:
    target_info = information_score(target)
    candidate_info = information_score(candidate)
    pair_info = min(target_info, candidate_info)

    if pair_info < 0.20:
        return min(raw_score * 0.25, 0.20)
    if pair_info < 0.38:
        return min(raw_score * (0.35 + pair_info), 0.46)
    if pair_info < 0.55:
        cap = 0.80 if boosted else 0.66
        return min(raw_score * (0.60 + pair_info), cap)
    return raw_score * (0.88 + (0.12 * pair_info))


def hard_similarity_cap(target: ItemPayload, candidate: ItemPayload, identity: float, metadata: float, image_score: float) -> float:
    item_name_score = _item_name_similarity(target, candidate)
    category_score = _category_similarity(target, candidate)

    if _meaningful(target.item_name, 2) and _meaningful(candidate.item_name, 2) and item_name_score < 0.42:
        return 0.42

    has_target_category = target.category_id is not None or _meaningful(target.category_name, 2)
    has_candidate_category = candidate.category_id is not None or _meaningful(candidate.category_name, 2)
    if has_target_category and has_candidate_category and category_score < 0.45 and identity < 0.82:
        return 0.50

    if identity < 0.45:
        return 0.48

    if identity < 0.62 and metadata < 0.55:
        return 0.56

    if image_score < 0.35 and identity < 0.72:
        return 0.58

    return 1.0


def build_reason(candidate: ItemPayload, breakdown: ScoreBreakdown, info_score: float) -> str:
    if info_score < 0.38:
        return "\uc785\ub825 \uc815\ubcf4\uac00 \ubd80\uc871\ud574 \ucc38\uace0 \uc810\uc218\ub85c \ud45c\uc2dc\ud569\ub2c8\ub2e4"

    reasons = []
    if breakdown.text_similarity >= 0.78:
        reasons.append("\ubb3c\uac74\uba85\uacfc \uc124\uba85\uc774 \uc720\uc0ac\ud569\ub2c8\ub2e4")
    if breakdown.metadata_score >= 0.78:
        reasons.append("\uce74\ud14c\uace0\ub9ac, \uc704\uce58, \ub0a0\uc9dc \uc870\uac74\uc774 \uac00\uae5d\uc2b5\ub2c8\ub2e4")
    if breakdown.image_similarity >= 0.76:
        reasons.append("\uc0ac\uc9c4 \ud2b9\uc9d5\uc774 \uc720\uc0ac\ud569\ub2c8\ub2e4")
    if not reasons:
        reasons.append("\uc785\ub825\ub41c \uc815\ubcf4\ub97c \uae30\uc900\uc73c\ub85c \ube44\uad50\ud588\uc2b5\ub2c8\ub2e4")
    return " / ".join(reasons)


def rank_recommendations(
    target: ItemPayload,
    candidates: list[ItemPayload],
    target_image_embedding: Optional[list[float]],
    target_text_embedding: list[float],
    candidate_embeddings: dict[int, tuple[Optional[list[float]], list[float]]],
    top_k: int,
) -> list[Recommendation]:
    recommendations: list[Recommendation] = []

    for candidate in candidates:
        candidate_image, candidate_text = candidate_embeddings[candidate.item_id]
        image_score = cosine_similarity(target_image_embedding, candidate_image)
        embedding_text_score = cosine_similarity(target_text_embedding, candidate_text)
        lexical = lexical_score(target, candidate)
        identity = identity_score(target, candidate)
        meta = metadata_score(target, candidate)

        if not (has_meaningful_text(target) and has_meaningful_text(candidate)):
            text_score = 0.0
        else:
            text_score = _clamp((0.82 * lexical) + (0.18 * embedding_text_score))

        if target_image_embedding is None or candidate_image is None:
            image_component = 0.0
        elif identity >= 0.58 or meta >= 0.58:
            image_component = image_score
        else:
            image_component = image_score * 0.35

        raw_score = (
            (0.46 * identity)
            + (0.28 * meta)
            + (0.16 * text_score)
            + (0.10 * image_component)
        )

        boost_floor = direct_match_boost(target, candidate, identity, meta)
        boosted = boost_floor > 0.0
        if boosted:
            raw_score = max(raw_score, boost_floor)

        final_score = apply_information_penalty(raw_score, target, candidate, boosted)
        final_score = min(final_score, hard_similarity_cap(target, candidate, identity, meta, image_score))
        pair_info = min(information_score(target), information_score(candidate))

        breakdown = ScoreBreakdown(
            image_similarity=round(image_score, 4),
            text_similarity=round(text_score, 4),
            metadata_score=round(meta, 4),
        )
        recommendations.append(
            Recommendation(
                item=candidate,
                score=round(_clamp(final_score), 4),
                reason=build_reason(candidate, breakdown, pair_info),
                breakdown=breakdown,
            )
        )

    eligible = [result for result in recommendations if result.score >= MIN_VISIBLE_SCORE]
    return sorted(eligible, key=lambda result: result.score, reverse=True)[:top_k]