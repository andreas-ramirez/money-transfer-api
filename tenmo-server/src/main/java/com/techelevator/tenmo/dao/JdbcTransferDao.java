package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;
    private AccountDao accountDao;
    private final int TRANSFER_TYPE_ID_SEND = 2;
    private final int TRANSFER_STATUS_ID_APPROVED = 2;

    private final int TRANSFER_TYPE_ID_REQUEST = 1;
    private final int TRANSFER_STATUS_ID_PENDING = 1;
    private final int TRANSFER_STATUS_ID_REJECTED = 3;


    public JdbcTransferDao(AccountDao accountDao, DataSource dataSource){
        this.accountDao = accountDao;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean sendTransfer(Transfer transfer) {
        boolean successful = false;
        String sql = "INSERT INTO transfer(account_from, account_to, amount, transfer_type_id, transfer_status_id) "
                + "VALUES((SELECT account_id FROM account WHERE user_id = ?),(SELECT account_id FROM account WHERE user_id = ?),?,?,?)";
            successful = accountDao.withdrawalBucks(transfer.getAccountFrom(), transfer.getAmount());
            if (successful){
                accountDao.depositBucks(transfer.getAccountTo(), transfer.getAmount());
                return jdbcTemplate.update(sql, transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount(),
                                        transfer.getTransferTypeId(), transfer.getTransferStatusId()) == 1;
            } return successful;
        }


    @Override
    public boolean requestTransfer(Transfer transfer) {
        boolean successful = false;
        String sql = "INSERT INTO transfer(account_from, account_to, amount, transfer_type_id, transfer_status_id) "
                + "VALUES((SELECT account_id FROM account WHERE user_id = ?),(SELECT account_id FROM account WHERE user_id = ?),?,?,?)";
        if (transfer.getAccountFrom() != transfer.getAccountTo()){
            successful = jdbcTemplate.update(sql, transfer.getAccountFrom(), transfer.getAccountTo(),
                    transfer.getAmount(), transfer.getTransferTypeId(), transfer.getTransferStatusId()) == 1;
        } else {
            System.out.println("Cannot request money from yourself");
        }
        return successful;
    }

    @Override
    public boolean updatePendingTransfer(Transfer transfer){
        boolean successful = false;
        String sql = "UPDATE transfer SET transfer_type_id = ?, transfer_status_id = ? WHERE transfer_id = ?";
        String sqlFrom = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                        "FROM transfer WHERE transfer_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlFrom, transfer.getTransferId());
        Transfer updatedTransfer = new Transfer();
        if (rowSet.next()) {
            updatedTransfer = mapToTransfer(rowSet);
        }
        int transferStatus = transfer.getTransferStatusId();
        if (transferStatus == TRANSFER_STATUS_ID_APPROVED) {
            successful = accountDao.withdrawalBucks(accountDao.getUserIdByAccountId(updatedTransfer.getAccountFrom()), updatedTransfer.getAmount());
            if (successful) {
                accountDao.depositBucks(accountDao.getUserIdByAccountId(updatedTransfer.getAccountTo()), updatedTransfer.getAmount());
            } else {
                transferStatus = TRANSFER_STATUS_ID_REJECTED;
            }
        }
        return jdbcTemplate.update(sql, updatedTransfer.getTransferTypeId(), transferStatus, transfer.getTransferId()) == 1;
    }

    @Override
    public List<Transfer> viewPendingRequests(String username) {
        List<Transfer> requests = new ArrayList<>();
        String sql = "SELECT transfer_id, username, amount FROM account " +
                "JOIN transfer ON account.account_id = transfer.account_to " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE transfer_status_id = ? AND account_from = (SELECT DISTINCT account_id FROM account " +
                "JOIN transfer ON account.account_id = transfer.account_from " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE username = ?)";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, TRANSFER_STATUS_ID_PENDING, username);
        while (results.next()){
            Transfer request = new Transfer();
            request.setTransferId(results.getInt("transfer_id"));
            request.setUsername(results.getString("username"));
            request.setAmount(results.getBigDecimal("amount"));
            requests.add(request);
        }
        return requests;
    }

    @Override
    public List<Transfer> viewTransfers(String username) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, amount, t.transfer_type_id, d.username AS user_from, b.username AS user_to " +
                "FROM transfer t " +
                "JOIN account a ON a.account_id = t.account_to " +
                "JOIN tenmo_user b ON a.user_id = b.user_id " +
                "JOIN account c ON c.account_id = t.account_from " +
                "JOIN tenmo_user d ON c.user_id = d.user_id " +
                "WHERE (d.username = ?) " +
                "OR (b.username = ?) " +
                "ORDER BY transfer_id";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username, username);
        while (results.next()){
            Transfer transfer = new Transfer();
            transfer.setTransferId(results.getInt("transfer_id"));
            transfer.setAmount(results.getBigDecimal("amount"));
            transfer.setTransferTypeId(results.getInt("transfer_type_id"));
            transfer.setAccountFromUsername(results.getString("user_from"));
            transfer.setAccountToUsername(results.getString("user_to"));
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public String getUsernameByAccountId(int accountId) {
        String username = null;
        String sql = "SELECT username FROM tenmo_user " +
                    "JOIN account ON tenmo_user.user_id = account.user_id WHERE account_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        if (rowSet.next()) {
            username = rowSet.getString("username");
        }
        return username;
    }

    @Override
    public Transfer viewTransferDetails(int transferId) {
        Transfer transfer = new Transfer();
        String sql = "SELECT transfer_id, transfer_type_desc, transfer_status_desc, account_from, account_to, amount " +
                "FROM transfer JOIN transfer_type ON transfer_type.transfer_type_id = transfer.transfer_type_id " +
                "JOIN transfer_status ON transfer_status.transfer_status_id = transfer.transfer_status_id WHERE transfer_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transferId);
        if (rowSet.next()) {
            transfer.setTransferId(rowSet.getInt("transfer_id"));
            transfer.setTransferTypeDesc(rowSet.getString("transfer_type_desc"));
            transfer.setTransferStatusDesc(rowSet.getString("transfer_status_desc"));
            transfer.setAccountFromUsername(getUsernameByAccountId(rowSet.getInt("account_from")));
            transfer.setAccountToUsername(getUsernameByAccountId(rowSet.getInt("account_to")));
            transfer.setAmount(rowSet.getBigDecimal("amount"));
        }
        return transfer;
    }


    public Transfer mapToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setTransferTypeId(rowSet.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rowSet.getInt("transfer_status_id"));
        transfer.setAccountFrom(rowSet.getInt("account_from"));
        transfer.setAccountTo(rowSet.getInt("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        return transfer;
    }
}
