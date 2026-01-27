package org.umc.valuedi.domain.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.ledger.entity.CategoryKeyword;

import java.util.List;

public interface CategoryKeywordRepository extends JpaRepository<CategoryKeyword, Long> {

    // 모든 키워드와 카테고리를 한 번에 로딩 (캐싱용)
    @Query("SELECT ck FROM CategoryKeyword ck JOIN FETCH ck.category")
    List<CategoryKeyword> findAllWithCategory();
}
