from __future__ import annotations

import argparse
import json
from pathlib import Path

from app.model import get_model
from app.schemas import ItemPayload
from app.similarity import item_text, rank_recommendations


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--eval-jsonl", required=True)
    parser.add_argument("--model-name", default=None)
    parser.add_argument("--top-k", type=int, default=5)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    model = get_model(args.model_name) if args.model_name else get_model()
    total = 0
    recall_1 = 0
    recall_5 = 0

    with Path(args.eval_jsonl).open("r", encoding="utf-8") as file:
        for line in file:
            if not line.strip():
                continue
            row = json.loads(line)
            target = ItemPayload(**row["target"])
            candidates = [ItemPayload(**candidate) for candidate in row["candidates"]]
            positive_ids = set(row["positive_ids"])

            target_image = model.image_embedding(target.image_url)
            target_text = model.text_embedding(item_text(target))
            candidate_embeddings = {
                candidate.item_id: (model.image_embedding(candidate.image_url), model.text_embedding(item_text(candidate)))
                for candidate in candidates
            }
            ranked = rank_recommendations(target, candidates, target_image, target_text, candidate_embeddings, args.top_k)
            ids = [result.item.item_id for result in ranked]

            total += 1
            recall_1 += int(bool(ids[:1] and ids[0] in positive_ids))
            recall_5 += int(bool(set(ids[:5]) & positive_ids))

    print(json.dumps({
        "total": total,
        "recall@1": recall_1 / total if total else 0.0,
        "recall@5": recall_5 / total if total else 0.0,
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
