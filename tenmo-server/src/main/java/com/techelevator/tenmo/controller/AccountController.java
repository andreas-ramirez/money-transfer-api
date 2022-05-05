package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/account")
public class AccountController {

    @Autowired
    private UserDao userDao;
    private AccountDao accountDao;

    public AccountController(AccountDao accountDao){
        this.accountDao = accountDao;
    }

    @ApiOperation("Retrieves balance for current user")
    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal getBalance(@ApiParam("user ID") Principal principal){
        int userId = userDao.findIdByUsername(principal.getName());
        return accountDao.getBalance(userId);
    }

    @ApiOperation("Retrieves list of all TEnmo users")
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> findAll(){
        return userDao.findAll();
    }

    @ApiOperation("Converts user ID to account ID")
    @GetMapping(path = "{userId}")
    public int getAccountIdByUserId(@ApiParam("user ID") @PathVariable int userId){
        return accountDao.getAccountIdByUserId(userId);
    }

}
