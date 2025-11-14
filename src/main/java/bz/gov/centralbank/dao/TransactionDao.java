package bz.gov.centralbank.dao;

import bz.gov.centralbank.config.DatabaseConfig;
import bz.gov.centralbank.models.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {

    public List<Transaction> findRecentByUserId(int userId, int limit) throws SQLException {
        String sql = "SELECT t.id, t.account_id, t.amount, t.type, t.created_at, t.description " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.id " +
                "WHERE a.user_id = ? " +
                "ORDER BY t.created_at DESC " +
                "LIMIT ?";
        List<Transaction> txs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setAccountId(rs.getInt("account_id"));
                    tx.setAmount(rs.getBigDecimal("amount"));
                    tx.setType(rs.getString("type"));
                    tx.setDescription(rs.getString("description"));
                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        tx.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
                    }
                    txs.add(tx);
                }
            }
        }
        return txs;
    }
}
