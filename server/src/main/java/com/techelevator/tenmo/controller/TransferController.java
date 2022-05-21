package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/transfer")
public class TransferController {

    @Autowired
    private UserDao userDao;
    private TransferDao transferDao;

    public TransferController(TransferDao transferDao){
        this.transferDao = transferDao;
    }

    @ApiOperation("Send transfer between users")
    @PostMapping(path = "/send")
    public void sendTransfer(@ApiParam("Transfer object") @Valid @RequestBody Transfer transfer){
         transferDao.sendTransfer(transfer);
    }

    @ApiOperation("Retrieves transfer history for current user")
    @GetMapping(path = "/history")
    public List<Transfer> viewTransfers(@ApiParam("username") Principal principal){
       return transferDao.viewTransfers(principal.getName());
    }

    @ApiOperation("Retrieves transfer details by transfer ID")
    @GetMapping(path = "/details/{transferId}")
    public Transfer viewTransferDetails(@ApiParam("transfer ID") @PathVariable int transferId){
        return transferDao.viewTransferDetails(transferId);
    }

    @ApiOperation("Creates transfer request")
    @PostMapping(path = "/request")
    public void requestTransfer(@ApiParam("Transfer object") @Valid @RequestBody Transfer transfer){
        transferDao.requestTransfer(transfer);
    }

    @ApiOperation("Retrieves pending transfer requests")
    @GetMapping(path = "/pending")
    public List<Transfer> viewPendingRequests(@ApiParam("username") Principal principal){
        return transferDao.viewPendingRequests(principal.getName());
    }

    @ApiOperation("Updates pending transfer when user accepts or rejects")
    @PutMapping(path = "/update")
    public void updatePendingTransfer(@ApiParam("Transfer object") @Valid @RequestBody Transfer transfer){
        transferDao.updatePendingTransfer(transfer);
    }

}
