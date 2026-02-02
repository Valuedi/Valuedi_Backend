package org.umc.valuedi.domain.ledger.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LedgerEntryJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<LedgerEntry> entries) {
        String sql = "INSERT IGNORE INTO ledger_entry " +
                "(member_id, bank_transaction_id, card_approval_id, category_id, title, memo, is_user_modified, transaction_at, transaction_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql,
                entries,
                100, // 배치 사이즈 (한 번에 100개씩)
                (PreparedStatement ps, LedgerEntry entry) -> {
                    ps.setLong(1, entry.getMember().getId());

                    if (entry.getBankTransaction() != null) {
                        ps.setLong(2, entry.getBankTransaction().getId());
                    } else {
                        ps.setNull(2, java.sql.Types.BIGINT);
                    }

                    if (entry.getCardApproval() != null) {
                        ps.setLong(3, entry.getCardApproval().getId());
                    } else {
                        ps.setNull(3, java.sql.Types.BIGINT);
                    }

                    if (entry.getCategory() != null) {
                        ps.setLong(4, entry.getCategory().getId());
                    } else {
                        ps.setNull(4, java.sql.Types.BIGINT);
                    }

                    ps.setString(5, entry.getTitle());
                    ps.setString(6, entry.getMemo());
                    ps.setBoolean(7, entry.getIsUserModified());
                    ps.setTimestamp(8, Timestamp.valueOf(entry.getTransactionAt()));
                    ps.setString(9, entry.getTransactionType().name()); // Enum -> String
                });
    }
}
