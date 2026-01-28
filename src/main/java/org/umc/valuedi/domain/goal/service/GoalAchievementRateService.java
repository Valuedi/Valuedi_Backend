package org.umc.valuedi.domain.goal.service;

import org.springframework.stereotype.Service;

@Service
public class GoalAchievementRateService {

    /**
     * @return 0~100 (정수)
     */
    public int calculateRate(Long savedAmount, Long targetAmount) {
        if (targetAmount <= 0) return 0;
        if (savedAmount <= 0) return 0;

        double rate = (savedAmount * 100.0) / targetAmount;
        if (rate < 0) rate = 0;
        if (rate > 100) rate = 100;
        return (int) Math.floor(rate);
    }
}
