package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.global.external.codef.client.CodefApiClient;
import org.umc.valuedi.global.external.codef.dto.res.CodefApiResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefCardService {
    private final CodefApiClient codefApiClient;

    /**
     * 보유 카드 목록 조회
     * CODEF API: /v1/kr/card/p/account/card-list
     */
    public List<ConnectionResDTO.CardConnection> getCardList(String connectedId) {
        try {
            // 요청 바디 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("connectedId", connectedId);
            requestBody.put("organization", "0000");  // 전체 카드사 조회

            // CODEF API 호출
            CodefApiResponse<Object> response = codefApiClient.getCardList(requestBody);

            if (!response.isSuccess()) {
                log.error("CODEF 카드 목록 조회 실패: {}",
                        response.getResult().getMessage());
                return Collections.emptyList();
            }

            // 응답 데이터 파싱
            return parseCardListResponse(response.getData());

        } catch (Exception e) {
            log.error("카드 목록 조회 중 에러 발생", e);
            return Collections.emptyList();
        }
    }

    /**
     * CODEF 응답 데이터를 DTO로 변환
     */
    private List<ConnectionResDTO.CardConnection> parseCardListResponse(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            List<Map<String, Object>> cardList =
                    (List<Map<String, Object>>) dataMap.get("resCardList");

            if (cardList == null || cardList.isEmpty()) {
                log.info("조회된 카드가 없습니다.");
                return Collections.emptyList();
            }

            return cardList.stream()
                    .map(card -> ConnectionResDTO.CardConnection.builder()
                            .cardId((String) card.get("resCardId"))
                            .cardName((String) card.get("resCardName"))
                            .cardNum((String) card.get("resCardNo"))
                            .cardCompany((String) card.get("resCardCompany"))
                            .cardCompanyCode((String) card.get("resCardCompanyCode"))
                            .build())
                    .toList();

        } catch (Exception e) {
            log.error("카드 목록 파싱 실패", e);
            return Collections.emptyList();
        }
    }
}
