package org.umc.valuedi.domain.mbti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.entity.MbtiTypeInfo;
import org.umc.valuedi.domain.mbti.enums.MbtiType;
import org.umc.valuedi.domain.mbti.repository.MbtiTypeInfoRepository;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FinanceMbtiTypeInfo implements FinanceMbtiProvider {

    private final MbtiTypeInfoRepository mbtiTypeInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public FinanceMbtiTypeInfoDto get(MbtiType type) {
        MbtiTypeInfo e = mbtiTypeInfoRepository.findByType(type)
                .orElseThrow(() -> new IllegalStateException("Finance MBTI type info not found: " + type));

        return toDto(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceMbtiTypeInfoDto> getAll() {
        return Arrays.stream(MbtiType.values())
                .map(this::get)
                .toList();
    }

    private FinanceMbtiTypeInfoDto toDto(MbtiTypeInfo e) {
        return new FinanceMbtiTypeInfoDto(
                e.getType(),
                e.getTitle(),
                e.getTagline(),
                e.getDetail(),
                splitLines(e.getWarning()),
                splitLines(e.getRecommend())
        );
    }

    private List<String> splitLines(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split("\\r?\\n"))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .toList();
    }
}
