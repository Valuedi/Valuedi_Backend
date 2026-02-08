package org.umc.valuedi.domain.savings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.savings.entity.RecommendationBatch;

import java.util.Optional;

public interface RecommendationBatchRepository extends JpaRepository<RecommendationBatch, Long> {

    Optional<RecommendationBatch> findTopByMemberIdOrderByIdDesc(Long memberId);

    Optional<RecommendationBatch> findTopByMemberIdAndMemberMbtiTestIdOrderByIdDesc(Long memberId, Long memberMbtiTestId);
}
