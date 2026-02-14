package org.umc.valuedi.domain.ledger.service.query;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.ledger.entity.Category;
import org.umc.valuedi.domain.ledger.entity.CategoryKeyword;
import org.umc.valuedi.domain.ledger.repository.CategoryKeywordRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryMatchingService {

    private final CategoryKeywordRepository categoryKeywordRepository;

    private static final List<String> CARD_SETTLEMENT_KEYWORDS = List.of(
            "카드대금", "신용카드대금", "카드청구", "카드자동이체"
    );

    private volatile Map<String, Category> keywordCache;

    @PostConstruct
    public void init() {
        refreshKeywordCache();
    }

    public void refreshKeywordCache() {
        List<CategoryKeyword> keywords = categoryKeywordRepository.findAllWithCategory();
        this.keywordCache = keywords.stream()
                .collect(Collectors.toMap(
                        CategoryKeyword::getKeyword,
                        CategoryKeyword::getCategory,
                        (e, r) -> e
                ));
    }

    public Category mapCategoryByKeyword(String text, Category defaultCategory) {
        if (text == null || text.isEmpty()) return defaultCategory;
        for (Map.Entry<String, Category> entry : keywordCache.entrySet()) {
            if (text.contains(entry.getKey())) return entry.getValue();
        }
        return defaultCategory;
    }

    public boolean isCardSettlement(String text) {
        return text != null && CARD_SETTLEMENT_KEYWORDS.stream().anyMatch(text::contains);
    }
}
