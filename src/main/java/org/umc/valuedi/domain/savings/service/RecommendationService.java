package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.enums.MbtiType;
import org.umc.valuedi.domain.mbti.exception.MbtiException;
import org.umc.valuedi.domain.mbti.exception.code.MbtiErrorCode;
import org.umc.valuedi.domain.mbti.repository.MemberMbtiTestRepository;
import org.umc.valuedi.domain.mbti.service.FinanceMbtiProvider;
import org.umc.valuedi.domain.savings.converter.SavingsConverter;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.entity.Recommendation;
import org.umc.valuedi.domain.savings.entity.Savings;
import org.umc.valuedi.domain.savings.entity.SavingsOption;
import org.umc.valuedi.domain.savings.enums.RecommendationStatus;
import org.umc.valuedi.domain.savings.exception.SavingsException;
import org.umc.valuedi.domain.savings.exception.code.SavingsErrorCode;
import org.umc.valuedi.domain.savings.repository.RecommendationRepository;
import org.umc.valuedi.domain.savings.repository.SavingsOptionRepository;
import org.umc.valuedi.domain.savings.repository.SavingsRepository;
import org.umc.valuedi.global.external.genai.client.GeminiClient;
import org.umc.valuedi.global.external.genai.dto.response.GeminiSavingsResponseDTO;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int RECOMMEND_COUNT = 15;
    private static final int TOP3_COUNT = 3;

    private static final int CANDIDATE_LIMIT = 40;

    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final FinanceMbtiProvider financeMbtiProvider;

    private final SavingsRepository savingsRepository;
    private final SavingsOptionRepository savingsOptionRepository;
    private final RecommendationRepository recommendationRepository;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private final RecommendationTxService recommendationTxService;

    @Transactional
    public SavingsResponseDTO.SavingsListResponse generateAndSaveRecommendations(Long memberId) {
        // 금융 mbti 최신 결과 조회
        MemberMbtiTest memberMbtiTest = memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.TYPE_INFO_NOT_FOUND));

        // 추천 상품 후보 조회
        Pageable candidatePage = PageRequest.of(0, CANDIDATE_LIMIT);
        List<SavingsOption> candidates = savingsOptionRepository.findCandidates(candidatePage);

        // 제미나이 프롬프트 생성
        MbtiType mbtiType = memberMbtiTest.getResultType();
        FinanceMbtiTypeInfoDto financeMbtiTypeInfo = financeMbtiProvider.get(mbtiType);
        String prompt = buildPrompt(mbtiType, financeMbtiTypeInfo, candidates, RECOMMEND_COUNT);

        // 제미나이 호출
        log.info("[Recommend] Gemini request. memberId={}, promptChars={}", memberId, prompt.length());
        String raw = geminiClient.generateText(prompt);
        log.info("[Recommend] Gemini response. memberId={}, rawChars={}", memberId, raw == null ? 0 : raw.length());

        // JSON 파싱
        GeminiSavingsResponseDTO.Result parsed = parseGeminiJson(raw);

        // 추천 optionId 목록 추출
        List<GeminiSavingsResponseDTO.Item> items = safeList(parsed.recommendations()).stream()
                .filter(i -> i.optionId() != null)
                .sorted(Comparator.comparing((GeminiSavingsResponseDTO.Item i) -> nullSafe(i.score())).reversed())
                .limit(RECOMMEND_COUNT)
                .toList();

        if (items.isEmpty()) {
            throw new SavingsException(SavingsErrorCode.RECOMMENDATION_FAILED);
        }
        SavingsResponseDTO.RecommendResponse savedResult = recommendationTxService.saveRecommendations(memberId, memberMbtiTest, parsed, items);

        List<Savings> savingsList = items.stream()
                .map(i -> savingsRepository.findByFinPrdtCd(i.finPrdtCd()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return SavingsConverter.toSavingsListResponseDTO(savingsList, savingsList.size(), 1, 1);
    }

    // 추천 상품 15개 조회
    @Transactional(readOnly = true)
    public SavingsResponseDTO.SavingsListResponse getRecommendation(Long memberId, String rsrvType) {
        Long mbtiTestId = memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.TYPE_INFO_NOT_FOUND))
                .getId();

        Pageable pageable = PageRequest.of(0, RECOMMEND_COUNT);
        List<Recommendation> recs = recommendationRepository.findLatestByMemberAndMemberMbtiTestId(memberId, mbtiTestId, pageable);

        if (recs.isEmpty()) {
            throw new SavingsException(SavingsErrorCode.RECOMMENDATION_NOT_FOUND);
        }

        String normalized = (rsrvType == null ? null : rsrvType.trim().toUpperCase()); // "S" or "F" or null

        List<Savings> filteredSavings = recs.stream()
                .filter(r -> {
                    if (normalized == null || normalized.isBlank()) return true;
                    SavingsOption so = r.getSavingsOption();
                    return so != null && normalized.equals(so.getRsrvType());
                })
                .map(Recommendation::getSavings)
                .toList();

        if (filteredSavings.isEmpty()) {
            throw new SavingsException(SavingsErrorCode.FILTERED_RECOMMENDATION_NOT_FOUND);
        }
        return SavingsConverter.toSavingsListResponseDTO(filteredSavings, filteredSavings.size(), 1, 1);
    }

    // 추천 상품 Top3 조회
    @Transactional(readOnly = true)
    public SavingsResponseDTO.SavingsListResponse getRecommendationTop3(Long memberId) {
        Long mbtiTestId = memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.TYPE_INFO_NOT_FOUND))
                .getId();

        Pageable pageable = PageRequest.of(0, TOP3_COUNT);
        List<Recommendation> recs = recommendationRepository.findLatestByMemberAndMemberMbtiTestId(memberId, mbtiTestId, pageable);

        if (recs.isEmpty()) {
            // 추천 자체가 없는 경우
            throw new SavingsException(SavingsErrorCode.RECOMMENDATION_NOT_FOUND);
        }

        List<Savings> top3Savings = recs.stream().map(Recommendation::getSavings).toList();

        return SavingsConverter.toSavingsListResponseDTO(top3Savings, top3Savings.size(), 1, 1);
    }

    // 추천 결과가 없을 때 사용할 공통 응답
    private SavingsResponseDTO.SavingsListResponse emptyResponse() {
        return SavingsConverter.toSavingsListResponseDTO(Collections.emptyList(), 0, 1, 1);
    }

    private String buildPrompt(
            MbtiType mbtiType,
            FinanceMbtiTypeInfoDto typeInfo,
            List<SavingsOption> candidates,
            int recommendCount
    ) {
        // 후보군 텍스트(짧게) 만들기: optionId + 상품명 + 금리 + 기간 + 적립유형
        List<Map<String, Object>> candidatePayload = candidates.stream()
                .map(so -> {
                    Savings s = so.getSavings();
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("optionId", so.getId());
                    m.put("korCoNm", s.getKorCoNm());
                    m.put("finPrdtCd", s.getFinPrdtCd());
                    m.put("finPrdtNm", s.getFinPrdtNm());
                    m.put("rsrvType", so.getRsrvType());
                    m.put("saveTrm", so.getSaveTrm());
                    m.put("intrRate", so.getIntrRate());
                    m.put("intrRate2", so.getIntrRate2());
                    return m;
                })
                .toList();

        String candidateText;
        try {
            candidateText = objectMapper.writeValueAsString(candidatePayload);
        } catch (Exception e) {
            log.warn("[Gemini] 후보군 JSON 직렬화 실패 size={}", candidatePayload.size(), e);
            candidateText = "[]";
        }

        // JSON만 출력 강제 + 키 이름 DTO와 일치(GeminiSavingsResponseDTO.Result가 recommendations를 가지도록)
        return """
                당신은 금융 추천 엔진입니다.
                아래 "사용자 금융 MBTI"와 "후보 적금 옵션 목록"을 기반으로, 사용자에게 가장 적합한 적금 옵션 %d개를 추천하세요.

                [사용자 금융 MBTI]
                - type: %s
                - title: %s
                - tagline: %s
                - detail: %s
                - warning: %s
                - recommend: %s

                [후보 적금 옵션 목록]
                %s

                [출력 규칙]
                - 반드시 JSON만 출력하세요. (설명 문장, 마크다운 금지)
                - 반드시 아래 스키마를 정확히 지키세요.
                - optionId는 후보 목록에 있는 값만 사용하세요.
                - score는 0~1 사이 숫자(소수)로, 높을수록 추천 우선순위입니다.
                - reasons는 1~3개. reasonCode는 대문자 스네이크로 작성하세요(예: HIGH_RATE, MATCH_TERM, MBTI_FIT).

                [JSON 스키마]
                {
                  "rationale": "전체 추천 요약(한 문단)",
                  "recommendations": [
                    {
                      "finPrdtCd": "string",
                      "optionId": 0,
                      "score": 0.0,
                      "reasons": [
                        { "reasonCode": "string", "reasonText": "string", "delta": 0.0 }
                      ]
                    }
                  ]
                }
                """.formatted(
                recommendCount,
                mbtiType.name(),
                safeStr(typeInfo.title()),
                safeStr(typeInfo.tagline()),
                safeStr(typeInfo.detail()),
                safeStr(String.join(" / ", safeList(typeInfo.cautions()))),
                safeStr(String.join(" / ", safeList(typeInfo.recommendedActions()))),
                candidateText
        );
    }

    private GeminiSavingsResponseDTO.Result parseGeminiJson(String raw) {
        // Gemini가 ```json ...``` 형태로 주는 경우 제거
        String cleaned = raw == null ? "" : raw.trim();
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        try {
            return objectMapper.readValue(cleaned, GeminiSavingsResponseDTO.Result.class);
        } catch (Exception e) {
            log.warn("[Gemini] JSON 파싱 실패 rawChars={}", raw == null ? 0 : raw.length(), e);
            return new GeminiSavingsResponseDTO.Result(null, List.of());
        }
    }

    // 추천 상품 상세 조회
    @Transactional(readOnly = true)
    public SavingsResponseDTO.SavingsDetailResponse getSavingsDetail(String finPrdtCd) {
        // Savings 엔티티 조회
        Savings savings = savingsRepository.findByFinPrdtCd(finPrdtCd)
                .orElseThrow(() -> new SavingsException(SavingsErrorCode.SAVINGS_NOT_FOUND));

        // DTO 변환 후 반환
        return SavingsConverter.toSavingsDetailResponseDTO(savings);
    }

    // NullPointerException 방지
    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static String safeStr(String s) {
        return s == null ? "" : s;
    }

    private static BigDecimal nullSafe(BigDecimal bd) {
        return bd == null ? BigDecimal.ZERO : bd;
    }
}
