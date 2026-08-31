from __future__ import annotations

import os

from fastapi import FastAPI, Request

from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.model import DEFAULT_MODEL_NAME, get_model
from app.schemas import EmbedRequest, EmbedResponse, RecommendRequest, RecommendResponse
from app.similarity import item_text, rank_recommendations


MODEL_NAME = os.getenv("FINDCAMPUS_AI_MODEL", DEFAULT_MODEL_NAME)

app = FastAPI(title="FindCampus AI Recommendation Server", version="0.1.0")


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    try:
        body = await request.body()
        print(f"[findcampus-ai] validation error on {request.url.path}: {exc.errors()} body={body[:1000]!r}")
    except Exception:
        print(f"[findcampus-ai] validation error on {request.url.path}: {exc.errors()}")
    return JSONResponse(status_code=422, content={"detail": exc.errors()})
@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "model": MODEL_NAME}


@app.post("/embed", response_model=EmbedResponse)
def embed(request: EmbedRequest) -> EmbedResponse:
    model = get_model(MODEL_NAME)
    return EmbedResponse(
        model_name=model.model_name,
        image_embedding=model.image_embedding(request.item.image_url),
        text_embedding=model.text_embedding(item_text(request.item)),
    )


@app.post("/recommend", response_model=RecommendResponse)
def recommend(request: RecommendRequest) -> RecommendResponse:
    model = get_model(MODEL_NAME)
    target_image = model.image_embedding(request.target.image_url)
    target_text = model.text_embedding(item_text(request.target))

    candidate_embeddings = {}
    for candidate in request.candidates:
        candidate_embeddings[candidate.item_id] = (
            model.image_embedding(candidate.image_url),
            model.text_embedding(item_text(candidate)),
        )

    return RecommendResponse(
        model_name=model.model_name,
        recommendations=rank_recommendations(
            target=request.target,
            candidates=request.candidates,
            target_image_embedding=target_image,
            target_text_embedding=target_text,
            candidate_embeddings=candidate_embeddings,
            top_k=request.top_k,
        ),
    )
