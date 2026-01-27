package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.global.external.codef.client.CodefApiClient;
import org.umc.valuedi.global.external.codef.dto.CodefApiResponse;
import org.umc.valuedi.global.external.codef.dto.res.CodefAssetResDTO;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefCardService {

    private final CodefApiClient codefApiClient;

    /**
     * 보유 카드 목록 조회 (부분 성공 적용)
     * 일부 카드사 조회에 실패하더라도, 성공한 카드사 정보는 반환합니다.
     */
    public List<CardResDTO.CardConnection> getCardList(
            String connectedId,
            List<String> cardOrganizations) {

        List<CardResDTO.CardConnection> allCards = new ArrayList<>();

        for (String organization : cardOrganizations) {
            try {
                List<CardResDTO.CardConnection> cards =
                        getCardListByOrganization(connectedId, organization);
                allCards.addAll(cards);
            } catch (CodefException e) {
                // 개별 카드사 조회 실패 시, 에러 로그를 남기고 다음 조사를 계속 진행
                log.error("CODEF 카드 목록 조회 실패 - 카드사: {}, 원인: {}", organization, e.getMessage());
            }
        }
        return allCards;
    }

    /**
     * 특정 카드사의 보유 카드 목록 조회
     */
    private List<CardResDTO.CardConnection> getCardListByOrganization(
            String connectedId,
            String organization) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("connectedId", connectedId);
        requestBody.put("organization", organization);

        CodefApiResponse<CodefAssetResDTO.CardList> response;
        try {
            response = codefApiClient.getCardList(requestBody);
        } catch (Exception e) {
            // FeignClient 예외 발생 시
            throw new CodefException(CodefErrorCode.CODEF_API_CONNECTION_ERROR);
        }

        if (response == null) {
            throw new CodefException(CodefErrorCode.CODEF_RESPONSE_EMPTY);
        }

        if (!response.isSuccess()) {
            // Codef API가 에러 코드를 반환한 경우
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }

        return parseCardListResponse(response.getData(), organization);
    }

    /**
     * CODEF 응답 데이터를 DTO로 변환
     */
    private List<CardResDTO.CardConnection> parseCardListResponse(CodefAssetResDTO.CardList data, String organization) {
        if (data == null) {
            return Collections.emptyList();
        }

        List<CodefAssetResDTO.Card> allCards = new ArrayList<>();

        if (data.getResCardList() != null && !data.getResCardList().isEmpty()) {
            // 여러 카드 응답 처리
            allCards.addAll(data.getResCardList());
        } else if (data.getResCardName() != null) {
            // 단일 카드 응답 처리: CardList DTO의 필드들을 사용하여 Card DTO 객체를 직접 생성
            CodefAssetResDTO.Card singleCard = CodefAssetResDTO.Card.builder()
                    .resCardName(data.getResCardName())
                    .resCardNo(data.getResCardNo())
                    .resCardType(data.getResCardType())
                    .resUserNm(data.getResUserNm())
                    .resSleepYN(data.getResSleepYN())
                    .resTrafficYN(data.getResTrafficYN())
                    .resValidPeriod(data.getResValidPeriod())
                    .resIssueDate(data.getResIssueDate())
                    .resState(data.getResState())
                    .resImageLink(data.getResImageLink())
                    .build();
            allCards.add(singleCard);
        }

        try {
            return allCards.stream()
                    .map(card -> buildCardConnection(card, organization))
                    .toList();
        } catch (Exception e) {
            throw new CodefException(CodefErrorCode.CODEF_JSON_PARSE_ERROR);
        }
    }

    /**
     * 카드 데이터를 DTO로 변환하는 헬퍼 메서드
     */
    private CardResDTO.CardConnection buildCardConnection(CodefAssetResDTO.Card card, String organization) {
        try {
            return CardResDTO.CardConnection.builder()
                    .cardId(card.getResCardNo())
                    .cardName(card.getResCardName())
                    .cardNum(card.getResCardNo())
                    .cardCompany(organization)
                    .cardCompanyCode(organization)
                    .build();
        } catch (Exception e) {
            throw new CodefException(CodefErrorCode.CODEF_JSON_PARSE_ERROR);
        }
    }
}
