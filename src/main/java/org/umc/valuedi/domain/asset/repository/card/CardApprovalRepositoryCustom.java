package org.umc.valuedi.domain.asset.repository.card;

import org.umc.valuedi.domain.asset.entity.CardApproval;
import java.util.List;

public interface CardApprovalRepositoryCustom {
    void bulkInsert(List<CardApproval> approvals);
}
