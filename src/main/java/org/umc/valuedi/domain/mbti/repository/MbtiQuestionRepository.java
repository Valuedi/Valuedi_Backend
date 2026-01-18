package org.umc.valuedi.domain.mbti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;

import java.util.List;

public interface MbtiQuestionRepository extends JpaRepository<MbtiQuestion, Long> {
    List<MbtiQuestion> findAllByOrderByIdAsc();
}
