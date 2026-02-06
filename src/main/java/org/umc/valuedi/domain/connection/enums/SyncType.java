package org.umc.valuedi.domain.connection.enums;

public enum SyncType {
    ALL,    // 전체 동기화 (계좌 + 카드)
    BANK,   // 은행/계좌만 동기화
    CARD    // 카드/승인내역만 동기화
}
