package org.umc.valuedi.domain.trophy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.trophy.enums.PeriodType;
import org.umc.valuedi.domain.trophy.service.command.TrophyCommandService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrophyScheduler {

    private final TrophyCommandService trophyCommandService;
    private final MemberRepository memberRepository;

    private static final int BATCH_SIZE = 100;

    @Scheduled(cron = "0 0 3 * * *") // 매일 03:00:00
    public void runDailyTrophyBatch() {
        log.info("🚀 [Batch] Daily Trophy Calculation Started");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        String periodKey = yesterday.format(DateTimeFormatter.ISO_DATE); // "2023-10-27"
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(LocalTime.MAX);

        executeBatch(PeriodType.DAILY, periodKey, start, end);

        log.info("✅ [Batch] Daily Trophy Calculation Finished");
    }

    @Scheduled(cron = "0 0 4 1 * *") // 매월 1일 04:00:00
    public void runMonthlyTrophyBatch() {
        log.info("🚀 [Batch] Monthly Trophy Calculation Started");

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        String periodKey = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")); // "2023-10"
        LocalDateTime start = lastMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).atTime(LocalTime.MAX);

        executeBatch(PeriodType.MONTHLY, periodKey, start, end);

        log.info("✅ [Batch] Monthly Trophy Calculation Finished");
    }

    private void executeBatch(PeriodType periodType, String periodKey, LocalDateTime start, LocalDateTime end) {
        int page = 0;
        Page<Member> memberPage;

        do {
            memberPage = memberRepository.findAll(PageRequest.of(page, BATCH_SIZE));

            for (Member member : memberPage.getContent()) {
                try {
                    trophyCommandService.calculateAndSnapshot(
                            member.getId(),
                            periodType,
                            periodKey,
                            start,
                            end
                    );
                } catch (Exception e) {
                    log.error("❌ Failed to calculate trophy for member: {} (Period: {})", member.getId(), periodKey, e);
                    // 개별 실패가 전체 배치를 멈추지 않도록 예외 처리
                }
            }

            page ++;
        } while (memberPage.hasNext());
    }
}
