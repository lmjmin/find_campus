# FindCampus AI Server

FastAPI based recommendation server for similar lost/found items.

## Features

- Pretrained CLIP image embedding
- CLIP text embedding
- Cosine similarity
- Metadata scoring for category, color, location and date
- Top-K recommendation API
- Contrastive fine-tuning scaffold
- Recall@1 and Recall@5 evaluation

## Local Run

```powershell
cd C:\dev\ws_st\find_campus\spring-server\ai-server
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Health check:

```powershell
curl http://127.0.0.1:8000/health
```

The first run downloads the pretrained CLIP model. If you want to use a fine-tuned model:

```powershell
$env:FINDCAMPUS_AI_MODEL="models/findcampus-clip"
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

## API

### POST `/embed`

Creates image/text embeddings for one item.

### POST `/recommend`

Ranks opposite-type candidates for a target item and returns TOP-K results.

Lost item target should receive found item candidates. Found item target should receive lost item candidates.

## Fine-tuning

Training data is JSONL:

```json
{"left_image":"data/lost/1.jpg","left_text":"black laptop bag","right_image":"data/found/7.jpg","right_text":"black laptop bag found in library","label":1}
{"left_image":"data/lost/1.jpg","left_text":"black laptop bag","right_image":"data/found/8.jpg","right_text":"white umbrella","label":0}
```

Run:

```powershell
python -m app.train --train-jsonl data/pairs.jsonl --output-dir models/findcampus-clip
```

## Evaluation

Evaluation data is JSONL:

```json
{"target":{"item_type":"lost","item_id":1,"title":"black laptop bag"},"candidates":[{"item_type":"found","item_id":7,"title":"black laptop bag found"},{"item_type":"found","item_id":8,"title":"white umbrella"}],"positive_ids":[7]}
```

Run:

```powershell
python -m app.evaluate --eval-jsonl data/eval.jsonl --top-k 5
```
