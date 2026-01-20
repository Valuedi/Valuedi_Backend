package org.umc.valuedi.domain.asset.repository.bank;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.umc.valuedi.domain.asset.entity.BankTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
public class BankTransactionRepositoryImpl implements BankTransactionRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsert(List<BankTransaction> transactions) {
        if (transactions.isEmpty()) {
            return;
        }

        String sql = "INSERT IGNORE INTO bank_transaction " +
                "(bank_account_id, tr_date, tr_time, tr_datetime, out_amount, in_amount, after_balance, " +
                "desc1, desc2, desc3, desc4, direction, raw_json, synced_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BankTransaction tx = transactions.get(i);
                ps.setLong(1, tx.getBankAccount().getId());
                ps.setObject(2, tx.getTrDate());
                ps.setObject(3, tx.getTrTime());

                if (tx.getTrDatetime() != null) {
                    ps.setTimestamp(4, Timestamp.valueOf(tx.getTrDatetime()));
                } else {
                    ps.setNull(4, java.sql.Types.TIMESTAMP);
                }

                ps.setLong(5, tx.getOutAmount());
                ps.setLong(6, tx.getInAmount());
                ps.setLong(7, tx.getAfterBalance());
                ps.setString(8, tx.getDesc1());
                ps.setString(9, tx.getDesc2());
                ps.setString(10, tx.getDesc3());
                ps.setString(11, tx.getDesc4());

                String directionName = (tx.getDirection() != null) ? tx.getDirection().name() : null;
                ps.setString(12, directionName);

                ps.setString(13, tx.getRawJson());
                
                if (tx.getSyncedAt() != null) {
                    ps.setTimestamp(14, Timestamp.valueOf(tx.getSyncedAt()));
                } else {
                    ps.setNull(14, java.sql.Types.TIMESTAMP);
                }
            }

            @Override
            public int getBatchSize() {
                return transactions.size();
            }
        });
    }
}
