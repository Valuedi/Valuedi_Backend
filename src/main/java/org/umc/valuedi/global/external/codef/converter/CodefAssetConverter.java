package org.umc.valuedi.global.external.codef.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.enums.CancelStatus;
import org.umc.valuedi.domain.asset.enums.HomeForeignType;
import org.umc.valuedi.domain.asset.enums.PaymentType;
import org.umc.valuedi.domain.asset.enums.TransactionDirection;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.global.external.codef.dto.res.CodefAssetResDTO;
import org.umc.valuedi.global.external.codef.util.EncryptUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodefAssetConverter {

    private final ObjectMapper objectMapper;
    private final EncryptUtil encryptUtil;

    public List<BankAccount> toBankAccountList(List<CodefAssetResDTO.BankAccount> data, CodefConnection connection) {
        return data.stream()
                .map(item -> toBankAccount(item, connection))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private BankAccount toBankAccount(CodefAssetResDTO.BankAccount item, CodefConnection connection) {
        try {
            String originalAccountNo = item.getResAccount();
            return BankAccount.builder()
                    .accountName(item.getResAccountName())
                    .accountDisplay(item.getResAccountDisplay())
                    .accountNoEnc(encryptUtil.encryptAES(originalAccountNo))
                    .accountNoHash(UUID.nameUUIDFromBytes(originalAccountNo.getBytes()).toString())
                    .accountDepositCode(item.getResAccountDeposit() != null ? item.getResAccountDeposit() : "UNKNOWN")
                    .balanceAmount(parseAmount(item.getResAccountBalance()))
                    .currency(item.getResAccountCurrency() != null ? item.getResAccountCurrency() : "KRW")
                    .lastTranDate(parseDate(item.getResLastTranDate()))
                    .isOverdraft("1".equals(item.getResOverdraftAcctYN()))
                    .codefConnection(connection)
                    .lastSyncedAt(LocalDateTime.now())
                    .rawJson(objectMapper.writeValueAsString(item))
                    .build();
        } catch (Exception e) {
            log.error("BankAccount 변환 실패: {}", e.getMessage());
            return null;
        }
    }

    public List<BankTransaction> toBankTransactionList(List<CodefAssetResDTO.BankTransaction> data, BankAccount account) {
        return data.stream()
                .map(item -> toBankTransaction(item, account))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private BankTransaction toBankTransaction(CodefAssetResDTO.BankTransaction item, BankAccount account) {
        try {
            LocalDate trDate = parseDate(item.getResAccountTrDate());
            LocalTime trTime = parseTime(item.getResAccountTrTime());
            LocalDateTime trDatetime = LocalDateTime.of(trDate, trTime);

            Long inAmount = parseAmount(item.getResAccountIn());
            Long outAmount = parseAmount(item.getResAccountOut());
            TransactionDirection direction = inAmount > 0 ? TransactionDirection.IN : TransactionDirection.OUT;

            return BankTransaction.builder()
                    .trDate(trDate)
                    .trTime(trTime)
                    .trDatetime(trDatetime)
                    .outAmount(outAmount)
                    .inAmount(inAmount)
                    .afterBalance(parseAmount(item.getResAfterTranBalance()))
                    .desc1(item.getResAccountDesc1())
                    .desc2(item.getResAccountDesc2())
                    .desc3(item.getResAccountDesc3())
                    .desc4(item.getResAccountDesc4())
                    .direction(direction)
                    .bankAccount(account)
                    .syncedAt(LocalDateTime.now())
                    .rawJson(objectMapper.writeValueAsString(item))
                    .build();
        } catch (Exception e) {
            log.error("BankTransaction 변환 실패: {}", e.getMessage());
            return null;
        }
    }

    public List<Card> toCardList(List<CodefAssetResDTO.Card> data, CodefConnection connection) {
        return data.stream()
                .map(item -> toCard(item, connection))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Card toCard(CodefAssetResDTO.Card item, CodefConnection connection) {
        try {
            return Card.builder()
                    .cardName(item.getResCardName())
                    .cardNoMasked(item.getResCardNo())
                    .cardType(item.getResCardType())
                    .codefConnection(connection)
                    .lastSyncedAt(LocalDateTime.now())
                    .rawJson(objectMapper.writeValueAsString(item))
                    .build();
        } catch (Exception e) {
            log.error("Card 변환 실패: {}", e.getMessage());
            return null;
        }
    }

    public List<CardApproval> toCardApprovalList(List<CodefAssetResDTO.CardApproval> data, Map<String, Card> cardMap) {
        return data.stream()
                .map(item -> toCardApproval(item, cardMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CardApproval toCardApproval(CodefAssetResDTO.CardApproval item, Map<String, Card> cardMap) {
        try {
            LocalDate usedDate = parseDate(item.getResUsedDate());
            LocalTime usedTime = parseTime(item.getResUsedTime());

            Card card = cardMap.get(item.getResCardNo());
            if (card == null) {
                log.warn("승인 내역에 해당하는 카드를 찾을 수 없습니다. 카드번호: {}", item.getResCardNo());
                return null;
            }

            String merchantType = item.getResMemberStoreType();
            if (merchantType == null || merchantType.isBlank()) {
                merchantType = "기타"; // 기본값 설정
            }

            return CardApproval.builder()
                    .card(card)
                    .usedDate(usedDate)
                    .usedTime(usedTime)
                    .usedDatetime(LocalDateTime.of(usedDate, usedTime))
                    .usedAmount(parseAmount(item.getResUsedAmount()))
                    .paymentType(parsePaymentType(item.getResPaymentType()))
                    .installmentMonth(parseInteger(item.getResInstallmentMonth()))
                    .approvalNo(item.getResApprovalNo())
                    .homeForeignType("1".equals(item.getResHomeForeignType()) ? HomeForeignType.DOMESTIC : HomeForeignType.FOREIGN)
                    .currency(item.getResAccountCurrency())
                    .cancelYn("1".equals(item.getResCancelYN()) ? CancelStatus.CANCEL : CancelStatus.NORMAL)
                    .cancelAmount(parseAmount(item.getResCancelAmount()))
                    .merchantCorpNo(item.getResMemberStoreCorpNo())
                    .merchantName(item.getResMemberStoreName())
                    .merchantType(merchantType) // 기본값이 설정된 변수 사용
                    .merchantNo(item.getResMemberStoreNo())
                    .commStartDate(parseDate(item.getCommStartDate()).atStartOfDay())
                    .commEndDate(parseDate(item.getCommEndDate()).atStartOfDay())
                    .syncedAt(LocalDateTime.now())
                    .rawJson(objectMapper.writeValueAsString(item))
                    .build();
        } catch (Exception e) {
            log.error("CardApproval 변환 실패: {}", e.getMessage());
            return null;
        }
    }

    private Long parseAmount(String amount) {
        if (amount == null || amount.isEmpty()) return 0L;
        try {
            return Long.parseLong(amount.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) return LocalDate.now();
        return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null) return LocalTime.MIN;
        if (timeStr.length() == 6) {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HHmmss"));
        }
        return LocalTime.MIN;
    }

    private PaymentType parsePaymentType(String type) {
        if ("1".equals(type)) return PaymentType.LUMP_SUM;
        return PaymentType.INSTALLMENT;
    }
}
