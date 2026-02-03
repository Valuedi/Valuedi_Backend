package org.umc.valuedi.domain.asset.repository.card.cardApproval;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.umc.valuedi.domain.asset.entity.CardApproval;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CardApprovalRepositoryImpl implements CardApprovalRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsert(List<CardApproval> approvals) {
        if (approvals == null || approvals.isEmpty()) {
            return;
        }

        String sql = "INSERT IGNORE INTO card_approval " +
                "(card_id, used_date, used_time, used_datetime, used_amount, payment_type, installment_month, " +
                "approval_no, home_foreign_type, currency, cancel_yn, cancel_amount, merchant_corp_no, " +
                "merchant_name, merchant_type, merchant_no, comm_start_date, comm_end_date, raw_json, synced_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CardApproval approval = approvals.get(i);
                ps.setLong(1, approval.getCard().getId());
                ps.setObject(2, approval.getUsedDate());
                ps.setObject(3, approval.getUsedTime());
                ps.setTimestamp(4, Timestamp.valueOf(approval.getUsedDatetime()));
                ps.setLong(5, approval.getUsedAmount());
                ps.setString(6, approval.getPaymentType() != null ? approval.getPaymentType().name() : null);
                if (approval.getInstallmentMonth() != null) {
                    ps.setInt(7, approval.getInstallmentMonth());
                } else {
                    ps.setNull(7, java.sql.Types.INTEGER);
                }
                ps.setString(8, approval.getApprovalNo());
                ps.setString(9, approval.getHomeForeignType().name());
                ps.setString(10, approval.getCurrency());
                ps.setString(11, approval.getCancelYn().name());
                ps.setLong(12, approval.getCancelAmount());
                ps.setString(13, approval.getMerchantCorpNo());
                ps.setString(14, approval.getMerchantName());
                ps.setString(15, approval.getMerchantType());
                ps.setString(16, approval.getMerchantNo());

                if (approval.getCommStartDate() != null) {
                    ps.setTimestamp(17, Timestamp.valueOf(approval.getCommStartDate())); // comm_start_date
                } else {
                    ps.setNull(17, java.sql.Types.TIMESTAMP);
                }

                if (approval.getCommEndDate() != null) {
                    ps.setTimestamp(18, Timestamp.valueOf(approval.getCommEndDate())); // comm_end_date
                } else {
                    ps.setNull(18, java.sql.Types.TIMESTAMP);
                }
                
                ps.setString(19, approval.getRawJson());

                if (approval.getSyncedAt() != null) {
                    ps.setTimestamp(20, Timestamp.valueOf(approval.getSyncedAt())); // synced_at
                } else {
                    ps.setNull(20, java.sql.Types.TIMESTAMP);
                }
            }

            @Override
            public int getBatchSize() {
                return approvals.size();
            }
        });
    }
}
