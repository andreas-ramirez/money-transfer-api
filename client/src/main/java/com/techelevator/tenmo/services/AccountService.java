package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountService {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public AccountService(String url){
        this.baseUrl = url;
    }

    public BigDecimal getBalance(){
        BigDecimal balance = new BigDecimal(0);
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(baseUrl + "account/balance",
                    HttpMethod.GET, makeEntity(), BigDecimal.class);
            balance = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }

    public User[] findAll(){
        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(baseUrl + "account/users", HttpMethod.GET,
                    makeEntity(), User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    public int getAccountIdByUserId(int userId) {
        int accountId = 0;
        try {
            ResponseEntity<Integer> response = restTemplate.exchange(baseUrl + "/account/" + userId, HttpMethod.GET, makeEntity(), Integer.class);
            accountId = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return accountId;
    }

    public HttpEntity<Void> makeEntity(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

}
