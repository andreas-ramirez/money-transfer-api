package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class TransferService {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public TransferService(String url){
        this.baseUrl = url;
    }

    public boolean sendTransfer(Transfer transfer) {
        boolean successful = false;
        try {
            restTemplate.postForObject(baseUrl + "/transfer/send", makeTransferEntity(transfer), Transfer.class);
            successful = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return successful;
    }

    public boolean requestTransfer(Transfer transfer){
        boolean successful = false;
        try {
            restTemplate.postForObject(baseUrl + "/transfer/request", makeTransferEntity(transfer), Transfer.class);
            successful = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return successful;
    }

    public boolean updatePendingTransfer(Transfer transfer){
        boolean successful = false;
        try {
            restTemplate.put(baseUrl + "/transfer/update", makeTransferEntity(transfer), Transfer.class);
            successful = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return successful;
    }

    public Transfer[] viewTransfer(){
        Transfer[] transferHistory = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "/transfer/history", HttpMethod.GET,
                    makeEntity(), Transfer[].class);
            transferHistory = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transferHistory;
    }

    public Transfer[] viewPendingRequests(){
        Transfer[] pendingRequests = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "/transfer/pending", HttpMethod.GET,
                    makeEntity(), Transfer[].class);
            pendingRequests = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return pendingRequests;
    }

    public Transfer viewTransferDetails(int transferId){
        Transfer transfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(baseUrl + "/transfer/details/" + transferId,
                    HttpMethod.GET, makeEntity(), Transfer.class);
            transfer = response.getBody();
        }catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    public HttpEntity<Void> makeEntity(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    public HttpEntity<Transfer> makeTransferEntity(Transfer transfer){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer,headers);
    }
}
