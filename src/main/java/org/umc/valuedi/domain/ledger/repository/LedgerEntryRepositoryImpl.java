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
public class LedgerEntryRepositoryImpl implements LedgerEntryRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsert(List<LedgerEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO ledger_entry (member_id, category_id, bank_transaction_id, card_approval_id, transaction_at, transaction_type, title, memo, is_user_modified, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        jdbcTemplate.batchUpdate(sql,
                entries,
                100,
                (PreparedStatement ps, LedgerEntry le) -> {
                    ps.setLong(1, le.getMember().getId());
                    ps.setLong(2, le.getCategory().getId());
                    ps.setObject(3, le.getBankTransaction() != null ? le.getBankTransaction().getId() : null);
                    ps.setObject(4, le.getCardApproval() != null ? le.getCardApproval().getId() : null);
                    ps.setTimestamp(5, Timestamp.valueOf(le.getTransactionAt()));
                    ps.setString(6, le.getTransactionType().toString());
                    ps.setString(7, le.getTitle());
                    ps.setString(8, le.getMemo());
                    ps.setBoolean(9, le.getIsUserModified());
                });
    }
}