package org.umc.valuedi.domain.mbti.service;


import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.enums.MbtiType;

import java.util.List;

public interface FinanceMbtiProvider {
    FinanceMbtiTypeInfoDto get(MbtiType type);
    List<FinanceMbtiTypeInfoDto> getAll();
}