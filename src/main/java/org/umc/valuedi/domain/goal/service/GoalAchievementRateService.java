package org.umc.valuedi.domain.goal.service;

import org.springframework.stereotype.Service;

@Service
public class GoalAchievementRateService {

    public int calculateRate(Long currentBalance, Long targetAmount) {
        if (targetAmount <= 0) return 0;
        if (currentBalance <= 0) return 0;

        double rate = (currentBalance * 100.0) / targetAmount;
        if (rate < 0) rate = 0;
        if (rate > 100) rate = 100;
        return (int) Math.floor(rate);
    }
}
