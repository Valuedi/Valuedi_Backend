package org.umc.valuedi.domain.mbti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.mbti.entity.MbtiTypeInfo;
import org.umc.valuedi.domain.mbti.enums.MbtiType;

import java.util.Optional;

public interface MbtiTypeInfoRepository extends JpaRepository<MbtiTypeInfo, Long> {
    Optional<MbtiTypeInfo> findByType(MbtiType type);
}