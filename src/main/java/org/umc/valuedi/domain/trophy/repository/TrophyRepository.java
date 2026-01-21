package org.umc.valuedi.domain.trophy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.trophy.entity.Trophy;
import org.umc.valuedi.domain.trophy.enums.TrophyType;

import java.util.Optional;

public interface TrophyRepository extends JpaRepository<Trophy, Long> {
    Optional<Trophy> findByType(TrophyType type);
}
