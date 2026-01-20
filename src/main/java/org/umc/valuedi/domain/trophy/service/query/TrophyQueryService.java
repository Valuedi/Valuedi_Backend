package org.umc.valuedi.domain.trophy.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.trophy.converter.TrophyConverter;
import org.umc.valuedi.domain.trophy.dto.response.TrophyMetaResponse;
import org.umc.valuedi.domain.trophy.dto.response.TrophyResponse;
import org.umc.valuedi.domain.trophy.entity.MemberTrophySnapshot;
import org.umc.valuedi.domain.trophy.entity.Trophy;
import org.umc.valuedi.domain.trophy.enums.PeriodType;
import org.umc.valuedi.domain.trophy.exception.TrophyException;
import org.umc.valuedi.domain.trophy.exception.code.TrophyErrorCode;
import org.umc.valuedi.domain.trophy.repository.MemberTrophyRepository;
import org.umc.valuedi.domain.trophy.repository.MemberTrophySnapshotRepository;
import org.umc.valuedi.domain.trophy.repository.TrophyRepository;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrophyQueryService {

    private final TrophyRepository trophyRepository;
    private final MemberTrophySnapshotRepository snapshotRepository;
    private final MemberRepository memberRepository;

    private static final Pattern PERIOD_KEY_PATTERN = Pattern.compile("^\\d{4}-\\d{2}(-\\d{2})?$");

    public List<TrophyResponse> getMyTrophies(Long memberId, PeriodType periodType, String periodKey) {
        // 1. 회원 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new TrophyException(TrophyErrorCode.MEMBER_NOT_FOUND));

        // 2. 날짜 형식 검증
        validatePeriodKey(periodKey);

        // 3. 조회
        List<MemberTrophySnapshot> snapshots = snapshotRepository.findAllByMemberIdAndPeriodTypeAndPeriodKey(member, periodType, periodKey);

        return TrophyConverter.toTrophyResponseList(snapshots);
    }

    public List<TrophyMetaResponse> getAllTrophies() {
        List<Trophy> trophies = trophyRepository.findAll();
        return TrophyConverter.toTrophyMetaResponseList(trophies);
    }

    private void validatePeriodKey(String periodKey) {
        if (periodKey == null || !PERIOD_KEY_PATTERN.matcher(periodKey).matches()) {
            throw new TrophyException(TrophyErrorCode.INVALID_PERIOD_KEY_FORMAT);
        }
    }

}
