package com.techelevator.tenmo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao {


    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public BigDecimal getBalance(int userId) {
        BigDecimal balance = new BigDecimal(0);
        String sql = "SELECT balance FROM account WHERE user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId);
        if (rowSet.next()) {
            balance = rowSet.getBigDecimal("balance");
        }
        return balance;
    }

    @Override
    public BigDecimal getBalanceByAccountId(int accountId) {
        BigDecimal balance = new BigDecimal(0);
        String sql = "SELECT balance FROM account WHERE user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        if (rowSet.next()) {
            balance = rowSet.getBigDecimal("balance");
        }
        return balance;
    }

    @Override
    public boolean withdrawalBucks(int accountFrom, BigDecimal amount){
        String sql = "UPDATE account SET balance = balance - ? WHERE user_id = ?";
        if (getBalanceByAccountId(accountFrom).compareTo(amount) >= 0){
            return jdbcTemplate.update(sql,amount,accountFrom) == 1;
        }
        return false;
    }

    @Override
    public boolean depositBucks(int accountTo, BigDecimal amount) {
        String sql = "UPDATE account SET balance = balance + ? WHERE user_id = ?";
        return jdbcTemplate.update(sql,amount,accountTo) == 1;
    }

    @Override
    public int getAccountIdByUserId(int userId) {
        int accountId = 0;
        String sql = "SELECT account_id FROM account WHERE user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql,userId);
        if (rowSet.next()) {
            accountId = rowSet.getInt("account_id");
        }
        return accountId;
    }

    @Override
    public int getUserIdByAccountId(int account) {
        int accountId = 0;
        String sql = "SELECT user_id FROM account WHERE account_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql,account);
        if (rowSet.next()) {
            accountId = rowSet.getInt("user_id");
        }
        return accountId;
    }
}
