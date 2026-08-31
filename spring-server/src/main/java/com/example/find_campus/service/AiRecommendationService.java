package com.example.find_campus.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.IAiRecommendationDao;
import com.example.find_campus.dao.IItemDao;
import com.example.find_campus.dto.AiEmbeddingDto;
import com.example.find_campus.dto.AiItemPayload;
import com.example.find_campus.dto.AiRecommendationDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.NotificationDto;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationService {

    private static final double MIN_RECOMMENDATION_SCORE = 0.60;

    private final IItemDao itemDao;
    private final IAiRecommendationDao aiRecommendationDao;
    private final ObjectMapper objectMapper;
    private final AppNotificationService appNotificationService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${findcampus.ai.enabled:true}")
    private boolean enabled;

    @Value("${findcampus.ai.base-url:http://127.0.0.1:8000}")
    private String aiBaseUrl;

    @Value("${findcampus.public-base-url:http://127.0.0.1:8085}")
    private String publicBaseUrl;

    @Value("${findcampus.ai.top-k:5}")
    private int topK;

    public void refreshRecommendationsAfterCommit(String itemType, Long itemId) {
        if (!enabled || itemId == null || !StringUtils.hasText(itemType)) {
            return;
        }

        Runnable task = () -> CompletableFuture.runAsync(() -> refreshRecommendations(itemType, itemId));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }
        task.run();
    }
    @Transactional
    public void refreshRecommendations(String itemType, Long itemId) {
        if (!enabled || itemId == null || !StringUtils.hasText(itemType)) {
            return;
        }

        try {
            ItemViewDto target = findTarget(itemType, itemId);
            if (target == null) {
                return;
            }

            AiItemPayload targetPayload = toPayload(target);
            saveEmbedding(targetPayload);

            List<ItemViewDto> candidates = findOppositeCandidates(itemType);
            if (candidates.isEmpty()) {
                aiRecommendationDao.deleteRecommendations(targetPayload.getItemType(), targetPayload.getItemId());
                return;
            }
            JsonNode response = postJson("/recommend", buildRecommendRequest(targetPayload, candidates));
            saveRecommendations(targetPayload, response);
        } catch (Exception e) {
            log.warn("AI recommendation refresh skipped for {} {}: {}", itemType, itemId, e.getMessage());
        }
    }

    public List<AiRecommendationDto> findRecommendations(String sourceType, Long sourceId) {
        if (!StringUtils.hasText(sourceType) || sourceId == null) {
            return List.of();
        }
        try {
            try {
            return aiRecommendationDao.findRecommendations(sourceType, sourceId).stream()
                    .filter(this::isRecommendationVisible)
                    .toList();
        } catch (Exception e) {
            log.warn("AI recommendation table lookup skipped for {} {}: {}", sourceType, sourceId, e.getMessage());
            return List.of();
        }
        } catch (Exception e) {
            log.warn("AI recommendation table lookup skipped for {} {}: {}", sourceType, sourceId, e.getMessage());
            return List.of();
        }
    }

    public List<AiRecommendationDto> recommendNow(String sourceType, Long sourceId) {
        if (!StringUtils.hasText(sourceType) || sourceId == null) {
            return List.of();
        }

        try {
            List<AiRecommendationDto> liveRecommendations = buildLiveRecommendations(sourceType, sourceId);
            if (!liveRecommendations.isEmpty()) {
                return liveRecommendations;
            }
        } catch (Exception e) {
            log.warn("Live AI recommendation failed for {} {}: {}", sourceType, sourceId, e.getMessage());
        }

        try {
            refreshRecommendations(sourceType, sourceId);
            return findRecommendations(sourceType, sourceId);
        } catch (Exception e) {
            log.warn("Stored AI recommendation unavailable for {} {}: {}", sourceType, sourceId, e.getMessage());
            return List.of();
        }
    }
    public AiRecommendationDto comparePair(String sourceType, Long sourceId, String targetType, Long targetId) {
        if (!StringUtils.hasText(sourceType) || sourceId == null || !StringUtils.hasText(targetType) || targetId == null) {
            return null;
        }

        ItemViewDto source = findTarget(sourceType, sourceId);
        ItemViewDto target = findTarget(targetType, targetId);
        if (source == null || target == null) {
            return null;
        }

        try {
            AiItemPayload sourcePayload = toPayload(source);
            JsonNode response = postJson("/recommend", buildRecommendRequest(sourcePayload, List.of(target)));
            List<AiRecommendationDto> results = toLiveRecommendationDtos(sourcePayload, response, List.of(target));
            if (!results.isEmpty()) {
                return results.get(0);
            }
        } catch (Exception e) {
            log.warn("Live AI pair comparison failed for {} {} -> {} {}: {}", sourceType, sourceId, targetType, targetId, e.getMessage());
        }

        return buildLocalPairScore(sourceType, sourceId, targetType, targetId, source, target);
    }

    private AiRecommendationDto buildLocalPairScore(String sourceType, Long sourceId, String targetType, Long targetId,
                                                    ItemViewDto source, ItemViewDto target) {
        double textScore = textSimilarity(source, target);
        double metadataScore = metadataSimilarity(source, target);
        double imageScore = imageSimilarity(source, target);

        double score = (textScore * 0.56) + (metadataScore * 0.36) + (imageScore * 0.08);
        if (sameMeaningfulText(source.getItemName(), target.getItemName())) {
            score = Math.max(score, metadataScore >= 0.75 ? 0.90 : 0.84);
        }
        double quality = informationQuality(source, target);
        score = score * (0.88 + (0.12 * quality));

        if (quality < 0.35) {
            score = Math.min(score, 0.32);
        }

        AiRecommendationDto dto = new AiRecommendationDto();
        dto.setSourceType(sourceType);
        dto.setSourceId(sourceId);
        dto.setTargetType(targetType);
        dto.setTargetId(targetId);
        dto.setTotalScore(clamp(score));
        dto.setImageScore(clamp(imageScore));
        dto.setTextScore(clamp(textScore));
        dto.setMetadataScore(clamp(metadataScore));
        dto.setTitle(target.getTitle());
        dto.setItemName(target.getItemName());
        dto.setCategoryName(target.getCategoryName());
        dto.setColor(target.getColor());
        dto.setLocationName(target.getLocationName());
        dto.setLocationDetail(target.getLocationDetail());
        dto.setItemDate(target.getItemDate());
        dto.setStatus(target.getStatus());
        dto.setImageUrl(target.getImageUrl());
        dto.setReason("local-fallback");
        dto.setModelName("local-metadata-similarity");
        return dto;
    }

    private double textSimilarity(ItemViewDto source, ItemViewDto target) {
        java.util.Set<String> sourceTokens = tokenize(joinText(source.getTitle(), source.getItemName(), source.getDescription(), source.getColor(), source.getBrand()));
        java.util.Set<String> targetTokens = tokenize(joinText(target.getTitle(), target.getItemName(), target.getDescription(), target.getColor(), target.getBrand()));
        if (sourceTokens.isEmpty() || targetTokens.isEmpty()) {
            return 0.0;
        }
        java.util.Set<String> intersection = new java.util.HashSet<>(sourceTokens);
        intersection.retainAll(targetTokens);
        java.util.Set<String> union = new java.util.HashSet<>(sourceTokens);
        union.addAll(targetTokens);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private java.util.Set<String> tokenize(String text) {
        java.util.Set<String> tokens = new java.util.LinkedHashSet<>();
        if (!StringUtils.hasText(text)) {
            return tokens;
        }
        String normalized = text.toLowerCase()
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (!StringUtils.hasText(normalized)) {
            return tokens;
        }
        for (String token : normalized.split(" ")) {
            if (token.length() >= 2) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private double metadataSimilarity(ItemViewDto source, ItemViewDto target) {
        double score = 0.0;
        double weight = 0.0;

        weight += 0.32;
        if (sameText(source.getCategoryName(), target.getCategoryName())) {
            score += 0.32;
        }

        weight += 0.18;
        if (sameText(source.getColor(), target.getColor())) {
            score += 0.18;
        }

        weight += 0.12;
        if (sameText(source.getBrand(), target.getBrand())) {
            score += 0.12;
        }

        weight += 0.24;
        if (sameText(source.getLocationName(), target.getLocationName())) {
            score += 0.24;
        } else if (StringUtils.hasText(source.getLocationName()) && StringUtils.hasText(target.getLocationName())
                && (source.getLocationName().contains(target.getLocationName()) || target.getLocationName().contains(source.getLocationName()))) {
            score += 0.12;
        }

        weight += 0.14;
        score += 0.14 * dateScore(source.getItemDate(), target.getItemDate());

        return weight == 0.0 ? 0.0 : score / weight;
    }

    private double dateScore(java.util.Date sourceDate, java.util.Date targetDate) {
        if (sourceDate == null || targetDate == null) {
            return 0.0;
        }
        long dayMillis = 24L * 60L * 60L * 1000L;
        long days = Math.abs(sourceDate.getTime() - targetDate.getTime()) / dayMillis;
        if (days == 0) {
            return 1.0;
        }
        if (days <= 1) {
            return 0.75;
        }
        if (days <= 3) {
            return 0.45;
        }
        if (days <= 7) {
            return 0.20;
        }
        return 0.0;
    }

    private double imageSimilarity(ItemViewDto source, ItemViewDto target) {
        if (!StringUtils.hasText(source.getImageUrl()) || !StringUtils.hasText(target.getImageUrl())) {
            return 0.0;
        }
        return source.getImageUrl().equals(target.getImageUrl()) ? 1.0 : 0.35;
    }

    private double informationQuality(ItemViewDto source, ItemViewDto target) {
        double sourceQuality = itemQuality(source);
        double targetQuality = itemQuality(target);
        return Math.min(sourceQuality, targetQuality);
    }

    private double itemQuality(ItemViewDto item) {
        double score = 0.0;
        if (StringUtils.hasText(item.getTitle())) score += 0.18;
        if (StringUtils.hasText(item.getItemName())) score += 0.22;
        if (StringUtils.hasText(item.getDescription())) score += 0.18;
        if (StringUtils.hasText(item.getCategoryName())) score += 0.14;
        if (StringUtils.hasText(item.getColor())) score += 0.08;
        if (StringUtils.hasText(item.getBrand())) score += 0.06;
        if (StringUtils.hasText(item.getLocationName())) score += 0.08;
        if (item.getItemDate() != null) score += 0.06;
        return clamp(score);
    }


    private boolean sameMeaningfulText(String left, String right) {
        if (!StringUtils.hasText(left) || !StringUtils.hasText(right)) {
            return false;
        }
        String normalizedLeft = normalizeMeaning(left);
        String normalizedRight = normalizeMeaning(right);
        return normalizedLeft.length() >= 2 && normalizedLeft.equals(normalizedRight);
    }

    private String normalizeMeaning(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.toLowerCase()
                .replaceAll("^\\s*\\d+\\.\\s*", "")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "")
                .trim();

        if (normalized.equals("\uD734\uB300\uC6A9\uBC30\uD130\uB9AC")
                || normalized.equals("\uBCF4\uC870\uBC30\uD130\uB9AC")
                || normalized.equals("powerbank")) {
            return "\uBCF4\uC870\uBC30\uD130\uB9AC";
        }
        return normalized;
    }
    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                builder.append(' ').append(value);
            }
        }
        return builder.toString();
    }

    private boolean sameText(String left, String right) {
        return StringUtils.hasText(left) && StringUtils.hasText(right) && left.trim().equalsIgnoreCase(right.trim());
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    private List<AiRecommendationDto> buildLiveRecommendations(String sourceType, Long sourceId) throws IOException, InterruptedException {
        if (!enabled) {
            return List.of();
        }

        ItemViewDto target = findTarget(sourceType, sourceId);
        if (target == null) {
            return List.of();
        }

        List<ItemViewDto> candidates = findOppositeCandidates(sourceType);
        if (candidates.isEmpty()) {
            return List.of();
        }

        AiItemPayload source = toPayload(target);
        JsonNode response = postJson("/recommend", buildRecommendRequest(source, candidates));
        return toLiveRecommendationDtos(source, response, candidates);
    }

    private List<AiRecommendationDto> toLiveRecommendationDtos(AiItemPayload source, JsonNode response, List<ItemViewDto> candidates) {
        Map<String, ItemViewDto> candidateMap = new LinkedHashMap<>();
        for (ItemViewDto candidate : candidates) {
            candidateMap.put(candidate.getItemType() + ":" + candidate.getId(), candidate);
        }

        String modelName = response.path("model_name").asText("");
        java.util.ArrayList<AiRecommendationDto> results = new java.util.ArrayList<>();
        for (JsonNode result : response.path("recommendations")) {
            JsonNode item = result.path("item");
            JsonNode breakdown = result.path("breakdown");
            String targetType = item.path("item_type").asText();
            Long targetId = item.path("item_id").asLong();
            ItemViewDto candidate = candidateMap.get(targetType + ":" + targetId);
            if (candidate == null) {
                continue;
            }

            double score = result.path("score").asDouble();
            if (!isScoreVisible(score)) {
                continue;
            }


            AiRecommendationDto recommendation = new AiRecommendationDto();
            recommendation.setSourceType(source.getItemType());
            recommendation.setSourceId(source.getItemId());
            recommendation.setTargetType(targetType);
            recommendation.setTargetId(targetId);
            recommendation.setTotalScore(score);
            recommendation.setImageScore(breakdown.path("image_similarity").asDouble());
            recommendation.setTextScore(breakdown.path("text_similarity").asDouble());
            recommendation.setMetadataScore(breakdown.path("metadata_score").asDouble());
            recommendation.setReason(result.path("reason").asText(""));
            recommendation.setModelName(modelName);
            recommendation.setTitle(candidate.getTitle());
            recommendation.setItemName(candidate.getItemName());
            recommendation.setCategoryName(candidate.getCategoryName());
            recommendation.setColor(candidate.getColor());
            recommendation.setLocationName(candidate.getLocationName());
            recommendation.setLocationDetail(candidate.getLocationDetail());
            recommendation.setItemDate(candidate.getItemDate());
            recommendation.setStatus(candidate.getStatus());
            recommendation.setImageUrl(candidate.getImageUrl());
            results.add(recommendation);
        }
        return results;
    }

    private ItemViewDto findTarget(String itemType, Long itemId) {
        if ("lost".equals(itemType)) {
            return itemDao.findLostItemById(itemId);
        }
        if ("found".equals(itemType)) {
            return itemDao.findFoundItemById(itemId);
        }
        return null;
    }

    private List<ItemViewDto> findOppositeCandidates(String itemType) {
        ItemSearchDto searchDto = new ItemSearchDto();
        searchDto.setSort("latest");
        if ("lost".equals(itemType)) {
            return itemDao.findFoundItems(searchDto);
        }
        return itemDao.findLostItems(searchDto);
    }

    private Map<String, Object> buildRecommendRequest(AiItemPayload target, List<ItemViewDto> candidates) {
        return Map.of(
                "target", toApiItemMap(target),
                "candidates", candidates.stream().map(this::toPayload).map(this::toApiItemMap).toList(),
                "top_k", topK
        );
    }

    private void saveEmbedding(AiItemPayload target) throws IOException, InterruptedException {
        JsonNode response = postJson("/embed", Map.of("item", toApiItemMap(target)));
        AiEmbeddingDto embedding = new AiEmbeddingDto();
        embedding.setItemType(target.getItemType());
        embedding.setItemId(target.getItemId());
        embedding.setModelName(response.path("model_name").asText(""));
        embedding.setImageEmbedding(response.path("image_embedding").isMissingNode() || response.path("image_embedding").isNull()
                ? null
                : objectMapper.writeValueAsString(response.path("image_embedding")));
        embedding.setTextEmbedding(objectMapper.writeValueAsString(response.path("text_embedding")));
        aiRecommendationDao.upsertEmbedding(embedding);
    }

    private void saveRecommendations(AiItemPayload source, JsonNode response) {
        aiRecommendationDao.deleteRecommendations(source.getItemType(), source.getItemId());
        String modelName = response.path("model_name").asText("");

        for (JsonNode result : response.path("recommendations")) {
            JsonNode item = result.path("item");
            JsonNode breakdown = result.path("breakdown");

            double score = result.path("score").asDouble();
            if (!isScoreVisible(score)) {
                continue;
            }


            AiRecommendationDto recommendation = new AiRecommendationDto();
            recommendation.setSourceType(source.getItemType());
            recommendation.setSourceId(source.getItemId());
            recommendation.setTargetType(item.path("item_type").asText());
            recommendation.setTargetId(item.path("item_id").asLong());
            recommendation.setTotalScore(score);
            recommendation.setImageScore(breakdown.path("image_similarity").asDouble());
            recommendation.setTextScore(breakdown.path("text_similarity").asDouble());
            recommendation.setMetadataScore(breakdown.path("metadata_score").asDouble());
            recommendation.setReason(result.path("reason").asText(""));
            recommendation.setModelName(modelName);
            aiRecommendationDao.insertRecommendation(recommendation);
            notifyRecommendationTargetOwner(source, recommendation);
            notifyAdminsAboutHighScoreMatch(source, recommendation);
        }
    }


    private void notifyRecommendationTargetOwner(AiItemPayload source, AiRecommendationDto recommendation) {
        if (source == null || recommendation == null || recommendation.getTotalScore() == null
                || recommendation.getTotalScore() < MIN_RECOMMENDATION_SCORE) {
            return;
        }
        try {
            ItemViewDto matchedItem = findTarget(recommendation.getTargetType(), recommendation.getTargetId());
            if (matchedItem == null || matchedItem.getUserId() == null) {
                return;
            }
            String title;
            String message;
            if ("found".equals(source.getItemType())) {
                title = "등록한 분실물과 비슷한 습득물이 올라왔습니다.";
                message = "AI가 " + percent(recommendation.getTotalScore()) + "% 유사한 습득물을 찾았습니다. 유사 물건 추천에서 확인해 주세요.";
            } else {
                title = "보관 중인 습득물과 비슷한 분실물 글이 등록되었습니다.";
                message = "AI가 " + percent(recommendation.getTotalScore()) + "% 유사한 분실물 글을 찾았습니다. 유사 물건 추천에서 확인해 주세요.";
            }
            NotificationDto notification = new NotificationDto();
            notification.setUserId(matchedItem.getUserId());
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType("AI_MATCH");
            notification.setTargetType(source.getItemType());
            notification.setTargetId(source.getItemId());
            appNotificationService.createNotification(notification);
        } catch (RuntimeException ignored) {
        }
    }

    private void notifyAdminsAboutHighScoreMatch(AiItemPayload source, AiRecommendationDto recommendation) {
        if (source == null || recommendation == null || recommendation.getTotalScore() == null
                || recommendation.getTotalScore() < MIN_RECOMMENDATION_SCORE) {
            return;
        }
        try {
            ItemViewDto sourceItem = findTarget(source.getItemType(), source.getItemId());
            ItemViewDto matchedItem = findTarget(recommendation.getTargetType(), recommendation.getTargetId());

            String sourceLabel = "found".equals(source.getItemType()) ? "습득물" : "분실물";
            String targetLabel = "found".equals(recommendation.getTargetType()) ? "습득물" : "분실물";
            String sourceTitle = sourceItem != null && StringUtils.hasText(sourceItem.getTitle())
                    ? sourceItem.getTitle()
                    : sourceLabel;
            String targetTitle = matchedItem != null && StringUtils.hasText(matchedItem.getTitle())
                    ? matchedItem.getTitle()
                    : targetLabel;

            appNotificationService.notifyAdmins(
                    "AI_HIGH_MATCH",
                    "AI 고유사도 매칭이 발생했습니다.",
                    sourceLabel + " '" + sourceTitle + "'와 " + targetLabel + " '" + targetTitle
                            + "'의 AI 유사도가 " + percent(recommendation.getTotalScore())
                            + "%입니다. 허위 수령 가능성이 있는지 참고해 주세요.",
                    "ai_match",
                    source.getItemId());
        } catch (RuntimeException ignored) {
        }
    }


    private String percent(Double score) {
        if (score == null) {
            return "0";
        }
        return String.valueOf(Math.round(Math.max(0.0, Math.min(1.0, score)) * 100.0));
    }

    private boolean isRecommendationVisible(AiRecommendationDto recommendation) {
        return recommendation != null
                && recommendation.getTotalScore() != null
                && recommendation.getTotalScore() >= MIN_RECOMMENDATION_SCORE;
    }

    private boolean isScoreVisible(double score) {
        return score >= MIN_RECOMMENDATION_SCORE;
    }
    private JsonNode postJson(String path, Object body) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiBaseUrl + path))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("AI server returned HTTP " + response.statusCode() + ": " + response.body());
        }
        return objectMapper.readTree(response.body());
    }

    private AiItemPayload toPayload(ItemViewDto item) {
        AiItemPayload payload = new AiItemPayload();
        payload.setItemType(item.getItemType());
        payload.setItemId(item.getId());
        payload.setTitle(defaultString(item.getTitle()));
        payload.setItemName(defaultString(item.getItemName()));
        payload.setCategoryId(item.getCategoryId());
        payload.setCategoryName(item.getCategoryName());
        payload.setColor(item.getColor());
        payload.setBrand(item.getBrand());
        payload.setLocationId(item.getLocationId());
        payload.setLocationName(item.getLocationName());
        payload.setLocationDetail(item.getLocationDetail());
        payload.setItemDate(item.getItemDate());
        payload.setItemTime(defaultString(item.getItemTime()));
        payload.setDescription(item.getDescription());
        payload.setImageUrl(toAbsoluteImageUrl(item.getImageUrl()));
        return payload;
    }

    private Map<String, Object> toApiItemMap(AiItemPayload item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("item_type", defaultString(item.getItemType()));
        map.put("item_id", item.getItemId());
        map.put("title", defaultString(item.getTitle()));
        map.put("item_name", defaultString(item.getItemName()));
        map.put("category_id", item.getCategoryId());
        map.put("category_name", defaultString(item.getCategoryName()));
        map.put("color", defaultString(item.getColor()));
        map.put("brand", defaultString(item.getBrand()));
        map.put("location_id", item.getLocationId());
        map.put("location_name", defaultString(item.getLocationName()));
        map.put("location_detail", defaultString(item.getLocationDetail()));
        map.put("item_date", formatDate(item.getItemDate()));
        map.put("item_time", defaultString(item.getItemTime()));
        map.put("description", defaultString(item.getDescription()));
        map.put("image_url", item.getImageUrl());
        return map;
    }
    private String toAbsoluteImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        String path = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return base + path;
    }


    private String formatDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    private String defaultString(String value) {
        return StringUtils.hasText(value) ? value : "";
    }
}
