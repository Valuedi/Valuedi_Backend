package org.umc.valuedi.domain.asset.repository.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.CardApproval;

public interface CardApprovalRepository extends JpaRepository<CardApproval, Long>, CardApprovalRepositoryCustom {
}
