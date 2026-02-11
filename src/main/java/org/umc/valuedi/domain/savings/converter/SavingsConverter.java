package org.umc.valuedi.domain.savings.converter;

import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.entity.Savings;
import org.umc.valuedi.domain.savings.entity.SavingsOption;
import org.umc.valuedi.global.external.fss.dto.response.FssSavingsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SavingsConverter {

    // DTO -> entity
    public static List<Savings> toSavings(FssSavingsResponse response, LocalDateTime loadedAt) {
        FssSavingsResponse.SavingResult result = response.result();

        // 옵션을 상품코드(finPrdtCd)로 그룹핑
        Map<String, List<FssSavingsResponse.SavingOptionItem>> optionMap = safeList(result.optionList()).stream()
                .collect(Collectors.groupingBy(FssSavingsResponse.SavingOptionItem::finPrdtCd));

        // baseList(상품들)를 돌며 Savings 엔티티 생성
        return safeList(result.baseList()).stream()
                .map(base -> {
                    List<FssSavingsResponse.SavingOptionItem> options = safeList(optionMap.get(base.finPrdtCd()));

                    String rsrvType = options.stream()
                            .map(FssSavingsResponse.SavingOptionItem::rsrvType)
                            .filter(s -> s != null && !s.isBlank())
                            .distinct()
                            .collect(Collectors.collectingAndThen(Collectors.joining(","), s -> s.isBlank() ? null : s));

                    String rsrvTypeNm = options.stream()
                            .map(FssSavingsResponse.SavingOptionItem::rsrvTypeNm)
                            .filter(s -> s != null && !s.isBlank())
                            .distinct()
                            .collect(Collectors.collectingAndThen(Collectors.joining(","), s -> s.isBlank() ? null : s));

                    // max_limit 파싱(원본이 문자열로 옴)
                    Integer maxLimit = parseIntegerOrNull(base.maxLimit());

                    Savings savings = Savings.builder()
                            .korCoNm(base.korCoNm())
                            .finPrdtCd(base.finPrdtCd())
                            .finPrdtNm(base.finPrdtNm())
                            .joinWay(base.joinWay())
                            .mtrtInt(base.mtrtInt())
                            .spclCnd(base.spclCnd())
                            .joinDeny(base.joinDeny())
                            .joinMember(base.joinMember())
                            .etcNote(base.etcNote())
                            .maxLimit(maxLimit)
                            .rsrvType(rsrvType)
                            .rsrvTypeNm(rsrvTypeNm)
                            .loadedAt(loadedAt)
                            .build();

                    // 옵션 엔티티 생성 + 연관관계 연결
                    List<SavingsOption> toOptions = options.stream()
                            .map(opt -> SavingsOption.builder()
                                    .intrRateType(opt.intrRateType())
                                    .intrRateTypeNm(opt.intrRateTypeNm())
                                    .rsrvType(opt.rsrvType())
                                    .rsrvTypeNm(opt.rsrvTypeNm())
                                    .saveTrm(parseIntegerOrNull(opt.saveTrm()))
                                    .intrRate(opt.intrRate())
                                    .intrRate2(opt.intrRate2())
                                    .build())
                            .toList();

                    savings.replaceOptions(toOptions);
                    return savings;
                })
                .toList();
    }

    // entity -> 목록 조회 응답 DTO
    public static SavingsResponseDTO.SavingsListResponse toSavingsListResponseDTO(List<Savings> savingsList, int totalCount, int nowPageNo, int maxPageNo) {
        List<SavingsResponseDTO.SavingsListResponse.RecommendedSavingProduct> products = savingsList.stream()
                .map(s -> new SavingsResponseDTO.SavingsListResponse.RecommendedSavingProduct(
                        s.getKorCoNm(),
                        s.getFinPrdtCd(),
                        s.getFinPrdtNm(),
                        s.getRsrvType(),
                        s.getRsrvTypeNm()
                ))
                .toList();

        return SavingsResponseDTO.SavingsListResponse.builder()
                .totalCount(totalCount)
                .nowPageNo(nowPageNo)
                .hasNext(nowPageNo < maxPageNo)
                .products(products)
                .build();
    }

    // entity -> 상세 조회 응답 DTO
    public static SavingsResponseDTO.SavingsDetailResponse toSavingsDetailResponseDTO(Savings savings) {
        List<SavingsResponseDTO.SavingsDetailResponse.Option> options = savings.getSavingsOptionList().stream()
                .map(o -> new SavingsResponseDTO.SavingsDetailResponse.Option(
                        o.getIntrRateType(),
                        o.getIntrRateTypeNm(),
                        o.getRsrvType(),
                        o.getRsrvTypeNm(),
                        o.getSaveTrm() == null ? null : String.valueOf(o.getSaveTrm()),
                        o.getIntrRate(),
                        o.getIntrRate2()
                ))
                .toList();

        return SavingsResponseDTO.SavingsDetailResponse.builder()
                .korCoNm(savings.getKorCoNm())
                .finPrdtCd(savings.getFinPrdtCd())
                .finPrdtNm(savings.getFinPrdtNm())
                .joinWay(savings.getJoinWay())
                .mtrtInt(savings.getMtrtInt())
                .spclCnd(savings.getSpclCnd())
                .joinDeny(savings.getJoinDeny())
                .joinMember(savings.getJoinMember())
                .etcNote(savings.getEtcNote())
                .maxLimit(savings.getMaxLimit() == null ? null : String.valueOf(savings.getMaxLimit()))
                .options(options)
                .build();
    }

    // NullPointerException 방지
    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    // 문자열을 Integer로 안전 변환
    private static Integer parseIntegerOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
