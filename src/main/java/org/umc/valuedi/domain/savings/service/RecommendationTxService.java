package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.exception.MbtiException;
import org.umc.valuedi.domain.mbti.exception.code.MbtiErrorCode;
import org.umc.valuedi.domain.mbti.repository.MemberMbtiTestRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.entity.*;
import org.umc.valuedi.domain.savings.enums.ReasonCode;
import org.umc.valuedi.domain.savings.enums.RecommendationStatus;
import org.umc.valuedi.domain.savings.repository.RecommendationBatchRepository;
import org.umc.valuedi.domain.savings.repository.RecommendationRepository;
import org.umc.valuedi.domain.savings.repository.SavingsOptionRepository;
import org.umc.valuedi.global.external.genai.dto.response.GeminiSavingsResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationTxService {

    private final RecommendationBatchRepository batchRepository;
    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final MemberRepository memberRepository;
    private final SavingsOptionRepository savingsOptionRepository;
    private final RecommendationRepository recommendationRepository;

    private static final int RECOMMEND_COUNT = 15;

    // 없으면 PENDING 배치 만들거나, 진행 중이면 기존 배치 반환
    @Transactional
    public SavingsResponseDTO.TriggerDecision triggerRecommendation(Long memberId) {
        // 금융 mbti 최신 결과 조회
        Long mbtiTestId = memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.TYPE_INFO_NOT_FOUND))
                .getId();

        Optional<RecommendationBatch> latest = batchRepository.findTopByMemberIdAndMemberMbtiTestIdOrderByIdDesc(memberId, mbtiTestId);

        // PENDING/PROCESSING이면 재실행 금지
        if (latest.isPresent() && latest.get().isPendingOrProcessing()) {
            RecommendationBatch b = latest.get();
            return SavingsResponseDTO.TriggerDecision.builder()
                    .batchId(b.getId())
                    .status(b.getStatus()) // PENDING/PROCESSING
                    .message("추천을 생성 중입니다. 잠시 후 조회 API로 확인해 주세요.")
                    .shouldStartAsync(false)
                    .build();
        }

        // SUCCESS면 재생성 금지
        if (latest.isPresent() && latest.get().getStatus() == RecommendationStatus.SUCCESS) {
            RecommendationBatch b = latest.get();
            return SavingsResponseDTO.TriggerDecision.builder()
                    .batchId(b.getId())
                    .status(b.getStatus())
                    .message("이미 최신 추천이 존재합니다. 조회 API로 확인해 주세요.")
                    .shouldStartAsync(false)
                    .build();
        }

        // 추천 상품 생성
        RecommendationBatch created = batchRepository.save(RecommendationBatch.pending(memberId, mbtiTestId));
        return SavingsResponseDTO.TriggerDecision.builder()
                .batchId(created.getId())
                .status(created.getStatus()) // PENDING
                .message("추천 상품을 생성 중입니다. 잠시 후 조회 API로 확인해 주세요.")
                .shouldStartAsync(true)
                .build();
    }

    // 상태 변경
    @Transactional
    public void markProcessing(Long batchId) {
        RecommendationBatch b = batchRepository.findById(batchId).orElseThrow();
        b.markProcessing();
    }

    @Transactional
    public void markSuccess(Long batchId) {
        RecommendationBatch b = batchRepository.findById(batchId).orElseThrow();
        b.markSuccess();
    }

    @Transactional
    public void markFailed(Long batchId, String errorMessage) {
        RecommendationBatch b = batchRepository.findById(batchId).orElseThrow();
        b.markFailed(errorMessage);
    }

    // 상태 조회용
    @Transactional(readOnly = true)
    public Optional<RecommendationBatch> findLatestBatch(Long memberId) {
        return batchRepository.findTopByMemberIdOrderByIdDesc(memberId);
    }

    // 이미 존재하는 추천 상품 반환
    @Transactional(readOnly = true)
    public SavingsResponseDTO.RecommendResponse buildCachedResponse(Long memberId, Long memberMbtiTestId) {
        PageRequest pageable = PageRequest.of(0, RECOMMEND_COUNT);
        List<Recommendation> recs =
                recommendationRepository.findLatestByMemberAndMemberMbtiTestId(memberId, memberMbtiTestId, pageable);

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

    // 추천 상품 저장
    @Transactional
    public SavingsResponseDTO.RecommendResponse saveRecommendations(
            Long memberId,
            MemberMbtiTest memberMbtiTest,
            GeminiSavingsResponseDTO.Result parsed,
            List<GeminiSavingsResponseDTO.Item> items
    ) {
        // Member 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<Long> optionIds = items.stream()
                .map(GeminiSavingsResponseDTO.Item::optionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<SavingsOption> pickedOptions = savingsOptionRepository.findAllByIdInFetchSavings(optionIds);
        Map<Long, SavingsOption> optionById = pickedOptions.stream()
                .collect(Collectors.toMap(SavingsOption::getId, Function.identity()));

        // 추천 상품 저장
        LocalDateTime now = LocalDateTime.now();
        List<Recommendation> toSave = new ArrayList<>();
        List<SavingsResponseDTO.RecommendedProduct> responseProducts = new ArrayList<>();

        for (GeminiSavingsResponseDTO.Item item : items) {
            SavingsOption so = optionById.get(item.optionId());

            if (so == null) continue;

            Savings s = so.getSavings();
            BigDecimal score = (item.score() == null ? BigDecimal.ZERO : item.score());

            Recommendation rec = Recommendation.builder()
                    .member(member)
                    .savings(s)
                    .savingsOption(so)
                    .memberMbtiTestId(memberMbtiTest.getId())
                    .score(score)
                    .createdAt(now)
                    .expiresAt(null)
                    .build();

            // 추천 상품 근거 저장
            List<RecommendationReason> reasons = (item.reasons() == null ? List.<GeminiSavingsResponseDTO.Reason>of() : item.reasons())
                    .stream()
                    .map(r -> RecommendationReason.builder()
                            .recommendation(rec)
                            .reasonCode(ReasonCode.from(r.reasonCode()))
                            .reasonText(r.reasonText())
                            .delta(r.delta() == null ? BigDecimal.ZERO : r.delta())
                            .createdAt(now)
                            .build())
                    .toList();

            rec.replaceReasons(reasons);

            toSave.add(rec);

            responseProducts.add(new SavingsResponseDTO.RecommendedProduct(
                    s.getKorCoNm(),
                    s.getFinPrdtCd(),
                    s.getFinPrdtNm(),
                    so.getRsrvType(),
                    so.getRsrvTypeNm(),
                    score
            ));
        }

        recommendationRepository.saveAll(toSave);

        return SavingsResponseDTO.RecommendResponse.builder()
                .products(responseProducts)
                .rationale(parsed.rationale())
                .build();
    }
}
