from __future__ import annotations

import argparse
from pathlib import Path

import torch
import torch.nn.functional as F
from torch.utils.data import DataLoader
from tqdm import tqdm
from transformers import CLIPModel, CLIPProcessor

from app.dataset import PairDataset
from app.model import DEFAULT_MODEL_NAME


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--train-jsonl", required=True)
    parser.add_argument("--model-name", default=DEFAULT_MODEL_NAME)
    parser.add_argument("--output-dir", default="models/findcampus-clip")
    parser.add_argument("--epochs", type=int, default=3)
    parser.add_argument("--batch-size", type=int, default=8)
    parser.add_argument("--lr", type=float, default=1e-5)
    parser.add_argument("--margin", type=float, default=0.35)
    return parser.parse_args()


def collate(batch: list[dict], processor: CLIPProcessor) -> dict:
    left_images = [row["left_image"] for row in batch]
    right_images = [row["right_image"] for row in batch]
    left_texts = [row["left_text"] or "unknown item" for row in batch]
    right_texts = [row["right_text"] or "unknown item" for row in batch]
    labels = torch.tensor([row["label"] for row in batch], dtype=torch.float32)

    left = processor(text=left_texts, images=left_images, return_tensors="pt", padding=True, truncation=True)
    right = processor(text=right_texts, images=right_images, return_tensors="pt", padding=True, truncation=True)
    return {"left": left, "right": right, "labels": labels}


def encode_item(model: CLIPModel, inputs: dict[str, torch.Tensor], device: str) -> torch.Tensor:
    inputs = {key: value.to(device) for key, value in inputs.items()}
    image_features = model.get_image_features(pixel_values=inputs["pixel_values"])
    text_features = model.get_text_features(input_ids=inputs["input_ids"], attention_mask=inputs["attention_mask"])
    features = F.normalize((image_features + text_features) / 2.0, dim=-1)
    return features


def contrastive_loss(left: torch.Tensor, right: torch.Tensor, labels: torch.Tensor, margin: float) -> torch.Tensor:
    cosine = F.cosine_similarity(left, right)
    positive_loss = labels * (1.0 - cosine).pow(2)
    negative_loss = (1.0 - labels) * F.relu(cosine - margin).pow(2)
    return (positive_loss + negative_loss).mean()


def main() -> None:
    args = parse_args()
    device = "cuda" if torch.cuda.is_available() else "cpu"
    processor = CLIPProcessor.from_pretrained(args.model_name)
    model = CLIPModel.from_pretrained(args.model_name).to(device)
    dataset = PairDataset(args.train_jsonl)
    loader = DataLoader(dataset, batch_size=args.batch_size, shuffle=True, collate_fn=lambda rows: collate(rows, processor))
    optimizer = torch.optim.AdamW(model.parameters(), lr=args.lr)

    model.train()
    for epoch in range(args.epochs):
        total_loss = 0.0
        for batch in tqdm(loader, desc=f"epoch {epoch + 1}/{args.epochs}"):
            labels = batch["labels"].to(device)
            left = encode_item(model, batch["left"], device)
            right = encode_item(model, batch["right"], device)
            loss = contrastive_loss(left, right, labels, args.margin)

            optimizer.zero_grad()
            loss.backward()
            optimizer.step()
            total_loss += float(loss.item())
        print(f"epoch={epoch + 1} loss={total_loss / max(1, len(loader)):.4f}")

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    model.save_pretrained(output_dir)
    processor.save_pretrained(output_dir)


if __name__ == "__main__":
    main()
