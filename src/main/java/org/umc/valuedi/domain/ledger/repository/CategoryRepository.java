package org.umc.valuedi.domain.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.ledger.entity.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);
}
