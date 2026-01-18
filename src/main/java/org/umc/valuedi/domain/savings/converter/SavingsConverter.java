package org.umc.valuedi.domain.savings.converter;

import org.umc.valuedi.domain.savings.dto.response.SavingsListResponse;
import org.umc.valuedi.infra.fss.dto.response.FssSavingsResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SavingsConverter {

    // 상품 목록 조회 응답 DTO 변환
    public static SavingsListResponse toSavingsListResponseDTO(FssSavingsResponse response) {
        FssSavingsResponse.SavingResult result = response.result();

        Map<String, List<FssSavingsResponse.SavingOptionItem>> optionMap = safeList(result.optionList()).stream()
                .collect(Collectors.groupingBy(FssSavingsResponse.SavingOptionItem::finPrdtCd));

        List<SavingsListResponse.RecommendedSavingProduct> products = safeList(result.baseList()).stream()
                .map(base -> {
                    List<FssSavingsResponse.SavingOptionItem> options =
                            safeList(optionMap.get(base.finPrdtCd()));

                    String rsrvType = options.stream()
                            .map(FssSavingsResponse.SavingOptionItem::rsrvType)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(","));

                    String rsrvTypeNm = options.stream()
                            .map(FssSavingsResponse.SavingOptionItem::rsrvTypeNm)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(","));

                    return new SavingsListResponse.RecommendedSavingProduct(
                            base.korCoNm(),
                            base.finPrdtCd(),
                            base.finPrdtNm(),
                            rsrvType,
                            rsrvTypeNm
                    );
                })
                .toList();

        return SavingsListResponse.builder()
                .totalCount(result.totalCount() != null ? result.totalCount() : products.size())
                .maxPageNo(result.maxPageNo() != null ? result.maxPageNo() : 1)
                .nowPageNo(result.nowPageNo() != null ? result.nowPageNo() : 1)
                .products(products)
                .build();
    }

    // NullPointerException 방지 메서드
    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
