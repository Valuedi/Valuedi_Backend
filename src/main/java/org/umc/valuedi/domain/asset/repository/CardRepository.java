package org.umc.valuedi.domain.asset.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}
