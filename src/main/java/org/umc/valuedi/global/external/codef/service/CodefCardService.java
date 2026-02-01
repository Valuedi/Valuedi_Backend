package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.global.external.codef.client.CodefApiClient;
import org.umc.valuedi.global.external.codef.dto.CodefApiResponse;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefCardService {

    private final CodefApiClient codefApiClient;

    /**
     * 보유 카드 목록 조회
     * 연동된 모든 카드사에서 카드 목록 조회
     */
    public List<CardResDTO.CardConnection> getCardList(
            String connectedId,
            List<String> cardOrganizations) {

        List<CardResDTO.CardConnection> allCards = new ArrayList<>();

        // 각 카드사별로 조회
        for (String organization : cardOrganizations) {
            try {
                List<CardResDTO.CardConnection> cards =
                        getCardListByOrganization(connectedId, organization);
                allCards.addAll(cards);
            } catch (Exception e) {
                log.error("카드사 {} 조회 중 에러 발생", organization, e);
            }
        }

        log.info("전체 조회된 카드 개수: {}", allCards.size());
        return allCards;
    }

    /**
     * 특정 카드사의 보유 카드 목록 조회
     */
    private List<CardResDTO.CardConnection> getCardListByOrganization(
            String connectedId,
            String organization) {

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("connectedId", connectedId);
            requestBody.put("organization", organization);

            // CODEF API 호출
            CodefApiResponse<Object> response = codefApiClient.getCardList(requestBody);

            if (!response.isSuccess()) {
                log.error("CODEF 카드 목록 조회 실패 [{}] - code: {}, message: {}",
                        organization,
                        response.getResult().getCode(),
                        response.getResult().getMessage());
                return Collections.emptyList();
            }

            return parseCardListResponse(response.getData(), organization);

        } catch (Exception e) {
            log.error("카드사 {} 조회 중 에러 발생", organization, e);
            return Collections.emptyList();
        }
    }

    /**
     * CODEF 응답 데이터를 DTO로 변환
     */
    private List<CardResDTO.CardConnection> parseCardListResponse(
            Object data,
            String organization) {

        if (data == null) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> dataMap = (Map<String, Object>) data;

            if (dataMap.containsKey("resCardList")) {
                List<Map<String, Object>> cardList =
                        (List<Map<String, Object>>) dataMap.get("resCardList");

                if (cardList == null || cardList.isEmpty()) {
                    return Collections.emptyList();
                }

                log.info("카드사 {} - 조회된 카드 {}개", organization, cardList.size());

                return cardList.stream()
                        .map(card -> buildCardConnection(card, organization))
                        .toList();
            }
            if (dataMap.containsKey("resCardName")) {
                return List.of(buildCardConnection(dataMap, organization));
            }
            return Collections.emptyList();

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 카드 데이터를 DTO로 변환하는 헬퍼 메서드
     */
    private CardResDTO.CardConnection buildCardConnection(
            Map<String, Object> card,
            String organization) {

        return CardResDTO.CardConnection.builder()
                .cardId((String) card.get("resCardId"))
                .cardName((String) card.get("resCardName"))
                .cardNum((String) card.get("resCardNo"))
                .cardCompany(organization)
                .cardCompanyCode(organization)
                .build();
    }
}