package bz.gov.centralbank.dao;

import bz.gov.centralbank.config.DatabaseConfig;
import bz.gov.centralbank.models.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    public List<Account> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, account_number, balance, currency FROM accounts WHERE user_id = ? ORDER BY id";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account();
                    account.setId(rs.getInt("id"));
                    account.setUserId(rs.getInt("user_id"));
                    account.setAccountNumber(rs.getString("account_number"));
                    account.setBalance(rs.getBigDecimal("balance"));
                    account.setCurrency(rs.getString("currency"));
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }

    public boolean transfer(int fromAccountId, int toAccountId, BigDecimal amount, String description) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String selectSql = "SELECT id, balance FROM accounts WHERE id IN (?, ?) FOR UPDATE";
        String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        String txSql = "INSERT INTO transactions (account_id, amount, type, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                selectPs.setInt(1, fromAccountId);
                selectPs.setInt(2, toAccountId);
                BigDecimal fromBalance = null;

                try (ResultSet rs = selectPs.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (id == fromAccountId) {
                            fromBalance = rs.getBigDecimal("balance");
                        }
                    }
                }

                if (fromBalance == null || fromBalance.compareTo(amount) < 0) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement debitPs = conn.prepareStatement(debitSql);
                     PreparedStatement creditPs = conn.prepareStatement(creditSql);
                     PreparedStatement txPs = conn.prepareStatement(txSql)) {

                    debitPs.setBigDecimal(1, amount);
                    debitPs.setInt(2, fromAccountId);
                    debitPs.executeUpdate();

                    creditPs.setBigDecimal(1, amount);
                    creditPs.setInt(2, toAccountId);
                    creditPs.executeUpdate();

                    txPs.setInt(1, fromAccountId);
                    txPs.setBigDecimal(2, amount.negate());
                    txPs.setString(3, "DEBIT");
                    txPs.setString(4, description);
                    txPs.executeUpdate();

                    txPs.setInt(1, toAccountId);
                    txPs.setBigDecimal(2, amount);
                    txPs.setString(3, "CREDIT");
                    txPs.setString(4, description);
                    txPs.executeUpdate();
                }
            }
            conn.commit();
            return true;
        }
    }
}
