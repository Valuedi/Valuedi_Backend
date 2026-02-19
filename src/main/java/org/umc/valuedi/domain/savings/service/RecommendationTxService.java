package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.entity.*;
import org.umc.valuedi.domain.savings.enums.ReasonCode;
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

    private final MemberRepository memberRepository;
    private final SavingsOptionRepository savingsOptionRepository;
    private final RecommendationRepository recommendationRepository;

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

        // 기존 추천 삭제 (중복 방지)
        List<Recommendation> existing = recommendationRepository.findAllByMemberIdAndMemberMbtiTestId(memberId, memberMbtiTest.getId());
        if (!existing.isEmpty()) {
            recommendationRepository.deleteAll(existing);
        }

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
