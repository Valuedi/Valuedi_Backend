package org.umc.valuedi.domain.trophy.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.trophy.dto.TrophyCalculationDto;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class TrophyAnalysisRepository {

    private final  JPAQueryFactory jpaQueryFactory;

    // Ledger(가계부) 엔티티 생성 후 활성화
    // 실제 프로젝트의 Ledger 엔티티 패키지 경로에 맞춰 QClass를 import 해야 합니다.

    /*
    public TrophyCalculationDto calculateStats(Long memberId, LocalDateTime start, LocalDateTime end) {
        QLedger ledger = QLedger.ledger;

        return queryFactory
                .select(Projections.constructor(TrophyCalculationDto.class,
                        ledger.amount.sum().coalesce(0L),
                        ledger.amount.max().coalesce(0L),
                        ledger.count().intValue()
                ))
                .from(ledger)
                .where(
                        ledger.memberId.eq(memberId),
                        ledger.transactionDate.between(start, end)
                        // 필요한 경우 카테고리 필터 추가
                )
                .fetchOne();
    }
    */

    // QueryDSL 설정이 완료되기 전까지 사용할 수 있는 Mock 메서드입니다.
    public TrophyCalculationDto calculateStatsMock(Long memberId, LocalDateTime start, LocalDateTime end) {
        // 실제 DB 조회 로직으로 대체 필요
        return TrophyCalculationDto.builder()
                .totalAmount(10000L)
                .maxAmount(5000L)
                .transactionCount(3)
                .build();
    }
}