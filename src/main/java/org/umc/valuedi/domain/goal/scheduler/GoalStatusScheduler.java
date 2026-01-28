package org.umc.valuedi.domain.goal.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.goal.service.GoalStatusChangeService;

@Component
@RequiredArgsConstructor
public class GoalStatusScheduler {

    private final GoalStatusChangeService goalAutoStatusService;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul") // 매일 00시 05분
    public void refreshGoalStatuses() {
        goalAutoStatusService.refreshGoalStatuses();
    }
}
