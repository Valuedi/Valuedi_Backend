package org.umc.valuedi.domain.connection.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.goal.repository.GoalRepository;

@Service
@RequiredArgsConstructor
public class ConnectionDeleteCommandService {

    private final CodefConnectionRepository codefConnectionRepository;
    private final GoalRepository goalRepository;

    /**
     * CodefConnection 및 관련 데이터를 짧은 트랜잭션에서 소프트 딜리트한다.
     * detached entity 문제 방지를 위해 connectionId로 재조회 후 삭제한다.
     */
    @Transactional
    public void deleteConnectionData(Long connectionId, BusinessType businessType) {
        CodefConnection connection = codefConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.CONNECTION_NOT_FOUND));

        if (businessType == BusinessType.BK) {
            goalRepository.softDeleteGoalsByConnectionId(connectionId);
        }
        // 하위 계좌/카드도 Soft Delete (Cascade)
        codefConnectionRepository.delete(connection);
    }
}
