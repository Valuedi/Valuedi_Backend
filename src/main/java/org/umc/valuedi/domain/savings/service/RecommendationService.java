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
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.savings.converter.SavingsConverter;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.entity.Recommendation;
import org.umc.valuedi.domain.savings.entity.RecommendationReason;
import org.umc.valuedi.domain.savings.entity.Savings;
import org.umc.valuedi.domain.savings.entity.SavingsOption;
import org.umc.valuedi.domain.savings.enums.ReasonCode;
import org.umc.valuedi.domain.savings.exception.SavingsException;
import org.umc.valuedi.domain.savings.exception.code.SavingsErrorCode;
import org.umc.valuedi.domain.savings.repository.RecommendationRepository;
import org.umc.valuedi.domain.savings.repository.SavingsOptionRespository;
import org.umc.valuedi.domain.savings.repository.SavingsRepository;
import org.umc.valuedi.global.external.genai.client.GeminiClient;
import org.umc.valuedi.global.external.genai.dto.response.GeminiSavingsResponseDTO;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int RECOMMEND_COUNT = 15;
    private static final int TOP3_COUNT = 3;

    private static final int CANDIDATE_LIMIT = 40;

    private final MemberRepository memberRepository;
    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final FinanceMbtiProvider financeMbtiProvider;

    private final SavingsRepository savingsRepository;
    private final SavingsOptionRespository savingsOptionRepository;
    private final RecommendationRepository recommendationRepository;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public SavingsResponseDTO.RecommendResponse recommend(
            Long memberId
    ) {
        // Member 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 금융 mbti 최신 결과 조회
        MemberMbtiTest memberMbtiTest = memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.TYPE_INFO_NOT_FOUND));

        Long memberMbtiTestId = memberMbtiTest.getId();

        boolean exists = recommendationRepository.existsByMemberIdAndMemberMbtiTestId(memberId, memberMbtiTestId);
        if (exists) {
            PageRequest pageable = PageRequest.of(0, RECOMMEND_COUNT);
            List<Recommendation> recs = recommendationRepository.findLatestByMemberAndMemberMbtiTestId(memberId, memberMbtiTestId, pageable);

            List<SavingsResponseDTO.RecommendedProduct> products = recs.stream()
                    .map(r -> new SavingsResponseDTO.RecommendedProduct(
                            r.getSavings().getKorCoNm(),
                            r.getSavings().getFinPrdtCd(),
                            r.getSavings().getFinPrdtNm(),
                            r.getSavingsOption() == null ? null : r.getSavingsOption().getRsrvType(),
                            r.getSavingsOption() == null ? null : r.getSavingsOption().getRsrvTypeNm(),
                            r.getScore()
                    ))
                    .toList();

            return SavingsResponseDTO.RecommendResponse.builder()
                    .products(products)
                    .rationale(null)
                    .build();
        }

        MbtiType mbtiType = memberMbtiTest.getResultType();
        FinanceMbtiTypeInfoDto financeMbtiTypeInfo = financeMbtiProvider.get(mbtiType);

        // 추천 상품 후보 조회
        Pageable candidatePage = PageRequest.of(0, CANDIDATE_LIMIT);
        List<SavingsOption> candidates = savingsOptionRepository.findCandidates(candidatePage);

        if (candidates.isEmpty()) {
            return SavingsResponseDTO.RecommendResponse.builder()
                    .products(List.of())
                    .rationale("조건에 맞는 후보 상품이 없습니다.")
                    .build();
        }

        // 제미나이 프롬프트 생성
        String prompt = buildPrompt(mbtiType, financeMbtiTypeInfo, candidates, RECOMMEND_COUNT);

        log.info("[Gemini] request memberId={}, promptChars={}",
                memberId,
                prompt == null ? 0 : prompt.length()
        );

        // 제미나이 호출
        String raw = geminiClient.generateText(prompt);

        log.info("[Gemini] response memberId={}, rawChars={}, head={}",
                memberId,
                raw == null ? 0 : raw.length(),
                raw == null ? "null" : raw.substring(0, Math.min(raw.length(), 200))
        );

        // JSON 파싱
        GeminiSavingsResponseDTO.Result parsed = parseGeminiJson(raw);

        // 추천 optionId 목록 추출
        List<GeminiSavingsResponseDTO.Item> items = safeList(parsed.recommendations()).stream()
                .filter(i -> i.optionId() != null)
                .sorted(Comparator.comparing((GeminiSavingsResponseDTO.Item i) -> nullSafe(i.score())).reversed())
                .limit(RECOMMEND_COUNT)
                .toList();

        List<Long> optionIds = items.stream()
                .map(GeminiSavingsResponseDTO.Item::optionId)
                .distinct()
                .toList();

        if (optionIds.isEmpty()) {
            return SavingsResponseDTO.RecommendResponse.builder()
                    .products(List.of())
                    .rationale("추천 결과 파싱에 실패했거나 추천 후보가 없습니다.")
                    .build();
        }

        List<SavingsOption> pickedOptions = savingsOptionRepository.findAllByIdInFetchSavings(optionIds);
        Map<Long, SavingsOption> optionById = pickedOptions.stream()
                .collect(Collectors.toMap(SavingsOption::getId, Function.identity()));

        // 추천 상품 저장
        LocalDateTime now = LocalDateTime.now();
        List<Recommendation> toSave = new ArrayList<>();
        List<SavingsResponseDTO.RecommendedProduct> responseProducts = new ArrayList<>();

        for (GeminiSavingsResponseDTO.Item item : items) {
            SavingsOption savingsOption = optionById.get(item.optionId());
            
            if (savingsOption == null) continue;

            Savings savings = savingsOption.getSavings();
            BigDecimal score = nullSafe(item.score());

            Recommendation recommendation = Recommendation.builder()
                    .member(member)
                    .savings(savings)
                    .savingsOption(savingsOption)
                    .memberMbtiTestId(memberMbtiTest.getId())
                    .score(score)
                    .createdAt(now)
                    .expiresAt(null)
                    .build();

            // 추천 상품 근거 저장
            List<RecommendationReason> reasons = safeList(item.reasons()).stream()
                    .map(reason -> RecommendationReason.builder()
                            .recommendation(recommendation)
                            .reasonCode(ReasonCode.from(reason.reasonCode()))
                            .reasonText(reason.reasonText())
                            .delta(nullSafe(reason.delta()))
                            .createdAt(now)
                            .build())
                    .toList();

            recommendation.replaceReasons(reasons);

            toSave.add(recommendation);

            responseProducts.add(new SavingsResponseDTO.RecommendedProduct(
                    savings.getKorCoNm(),
                    savings.getFinPrdtCd(),
                    savings.getFinPrdtNm(),
                    savingsOption.getRsrvType(),
                    savingsOption.getRsrvTypeNm(),
                    score
            ));
        }

        recommendationRepository.saveAll(toSave);

        // DTO 변환 후 반환
        return SavingsResponseDTO.RecommendResponse.builder()
                .products(responseProducts)
                .rationale(parsed.rationale())
                .build();
    }

    // 추천 상품 15개 조회
    @Transactional(readOnly = true)
    public SavingsResponseDTO.SavingsListResponse getRecommendation(Long memberId, String rsrvType) {
        Long mbtiTestId = memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.TYPE_INFO_NOT_FOUND))
                .getId();

        Pageable pageable = PageRequest.of(0, RECOMMEND_COUNT);
        List<Recommendation> recs = recommendationRepository.findLatestByMemberAndMemberMbtiTestId(memberId, mbtiTestId, pageable);

        String normalized = (rsrvType == null ? null : rsrvType.trim().toUpperCase()); // "S" or "F" or null

        List<SavingsResponseDTO.SavingsListResponse.RecommendedSavingProduct> products = recs.stream()
                .filter(r -> {
                    if (normalized == null || normalized.isBlank()) return true;
                    SavingsOption so = r.getSavingsOption();
                    return so != null && normalized.equals(so.getRsrvType());
                })
                .map(r -> {
                    Savings s = r.getSavings();
                    SavingsOption so = r.getSavingsOption();
                    return new SavingsResponseDTO.SavingsListResponse.RecommendedSavingProduct(
                            s.getKorCoNm(),
                            s.getFinPrdtCd(),
                            s.getFinPrdtNm(),
                            so == null ? null : so.getRsrvType(),
                            so == null ? null : so.getRsrvTypeNm()
                    );
                })
                .toList();

        return SavingsResponseDTO.SavingsListResponse.builder()
                .totalCount(products.size())
                .nowPageNo(1)
                .maxPageNo(1)
                .products(products)
                .build();
    }

    // 추천 상품 Top3 조회
    @Transactional(readOnly = true)
    public SavingsResponseDTO.SavingsListResponse getRecommendationTop3(Long memberId) {
        Long mbtiTestId = memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.TYPE_INFO_NOT_FOUND))
                .getId();

        Pageable pageable = PageRequest.of(0, TOP3_COUNT);
        List<Recommendation> recs = recommendationRepository.findLatestByMemberAndMemberMbtiTestId(memberId, mbtiTestId, pageable);

        List<SavingsResponseDTO.SavingsListResponse.RecommendedSavingProduct> products = recs.stream()
                .map(r -> {
                    Savings s = r.getSavings();
                    SavingsOption so = r.getSavingsOption();
                    return new SavingsResponseDTO.SavingsListResponse.RecommendedSavingProduct(
                            s.getKorCoNm(),
                            s.getFinPrdtCd(),
                            s.getFinPrdtNm(),
                            so == null ? null : so.getRsrvType(),
                            so == null ? null : so.getRsrvTypeNm()
                    );
                })
                .toList();

        return SavingsResponseDTO.SavingsListResponse.builder()
                .totalCount(products.size())
                .nowPageNo(1)
                .maxPageNo(1)
                .products(products)
                .build();
    }

    private String buildPrompt(
            MbtiType mbtiType,
            FinanceMbtiTypeInfoDto typeInfo,
            List<SavingsOption> candidates,
            int recommendCount
    ) {
        // 후보군 텍스트(짧게) 만들기: optionId + 상품명 + 금리 + 기간 + 적립유형
        String candidateText = candidates.stream()
                .map(so -> {
                    Savings s = so.getSavings();
                    return String.format(
                            Locale.KOREA,
                            "{\"optionId\":%d,\"finPrdtCd\":\"%s\",\"finPrdtNm\":\"%s\",\"korCoNm\":\"%s\",\"rsrvType\":\"%s\",\"saveTrm\":%s,\"intrRate\":%s,\"intrRate2\":%s}",
                            so.getId(),
                            s.getFinPrdtCd(),
                            safeStr(s.getFinPrdtNm()),
                            safeStr(s.getKorCoNm()),
                            safeStr(so.getRsrvType()),
                            so.getSaveTrm(),
                            so.getIntrRate(),
                            so.getIntrRate2()
                    );
                })
                .collect(Collectors.joining(",\n"));

        // JSON만 출력 강제 + 키 이름 DTO와 일치(GeminiSavingsResponseDTO.Result가 recommendations를 가지도록)
        return """
                당신은 금융 추천 엔진입니다.
                아래 "사용자 금융 MBTI"와 "후보 적금 옵션 목록"을 기반으로, 사용자에게 가장 적합한 적금 옵션 %d개를 추천하세요.

                [사용자 금융 MBTI]
                - type: %s
                - title: %s
                - tagline: %s
                - detail: %s
                - recommend: %s
                - warning: %s

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
            return new GeminiSavingsResponseDTO.Result(null, List.of());
        }
    }

    // 추천 상품 상세 조회
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
