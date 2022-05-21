package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    public boolean sendTransfer(Transfer transfer);

    public List<Transfer> viewTransfers(String username);

    public String getUsernameByAccountId(int accountId);

    public Transfer viewTransferDetails(int transferId);

    public boolean requestTransfer(Transfer transfer);

    public List<Transfer> viewPendingRequests(String username);

    public boolean updatePendingTransfer(Transfer transfer);
}
