package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    public BigDecimal getBalance(int accountId);

    public BigDecimal getBalanceByAccountId(int accountId);

    public boolean withdrawalBucks(int accountFrom, BigDecimal amount);

    public boolean depositBucks(int accountTo, BigDecimal amount);

    public int getAccountIdByUserId(int userId);

    public int getUserIdByAccountId(int accountId);

}
