package org.umc.valuedi.domain.asset.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.CardApproval;

public interface CardApprovalRepository extends JpaRepository<CardApproval, Long> {
}
