package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;

import java.math.BigDecimal;

public class App {

    private final int TRANSFER_TYPE_ID_REQUEST = 1;
    private final int TRANSFER_TYPE_ID_SEND = 2;

    private final int TRANSFER_STATUS_ID_PENDING = 1;
    private final int TRANSFER_STATUS_ID_APPROVED = 2;
    private final int TRANSFER_STATUS_ID_REJECTED = 3;

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            System.out.println("Invalid username/password");
            loginMenu();
        } else {
            accountService.setAuthToken(currentUser.getToken());
            transferService.setAuthToken(currentUser.getToken());
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                loginMenu();
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        System.out.println("Your current account balance is: $" + accountService.getBalance());
    }

    private void viewTransferHistory() {
        Transfer[] transferHistory = transferService.viewTransfer();
        consoleService.transfersHeader();
        for (Transfer transfer : transferHistory) {
            if (transfer.getAccountFromUsername().equals(currentUser.getUser().getUsername())) {
                System.out.printf("%-10d %-5s %-10s %1s %8.2f\n", transfer.getTransferId()
                        , "To:", transfer.getAccountToUsername(), "$", transfer.getAmount());
            } else if (transfer.getAccountToUsername().equals(currentUser.getUser().getUsername())){
                System.out.printf("%-10d %-5s %-10s %1s %8.2f\n", transfer.getTransferId()
                        , "From:", transfer.getAccountFromUsername(), "$", transfer.getAmount());
            }
        }
        int transferId = consoleService.promptForInt("Please enter transfer ID to view details: ");
        boolean validId = false;
        for (Transfer transfer : transferHistory) {
            if (transfer.getTransferId() == transferId) {
                transferDetails(transferId);
                validId = true;
            }
        }
        if (!validId) {
            System.out.println("Invalid transfer ID");
        }
    }

    private void viewPendingRequests() {
        boolean updateRequestSuccess = false;
        Transfer[] pendingRequests = transferService.viewPendingRequests();
        consoleService.pendingRequestsHeader();
        for (Transfer request : pendingRequests) {
            System.out.printf("%-10d %-15s %1s %8.2f\n", request.getTransferId(),
                    request.getUsername(), "$", request.getAmount());
        }
        int id = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): ");
        boolean isValid = false;
        BigDecimal amountToCompare = new BigDecimal("0.00");
        for (Transfer pendingRequest : pendingRequests) {
            if (id == pendingRequest.getTransferId()){
                isValid = true;
                amountToCompare = amountToCompare.add(pendingRequest.getAmount());
                break;
            }
        }
        if (isValid){
            consoleService.pendingRequestsOptions();
            int option = consoleService.promptForInt("Please choose an option: ");
            if (option == 1 && amountToCompare.compareTo(accountService.getBalance()) > 0) {
                System.out.println("Insufficient funds, request will be rejected.");
            } updateRequest(id, option);
        } else if (id != 0){
            System.out.println("Invalid selection");
        }
    }

    private void updateRequest(int id, int option){
        Transfer transfer = new Transfer();
        transfer.setTransferId(id);
        if (option != 0){
            if (option == 1){
                transfer.setTransferStatusId(TRANSFER_STATUS_ID_APPROVED);
                transferService.updatePendingTransfer(transfer);
            } else if (option == 2){
                transfer.setTransferStatusId(TRANSFER_STATUS_ID_REJECTED);
                transferService.updatePendingTransfer(transfer);
            } else {
                System.out.println("Invalid option");
            }
        }
    }

    private void sendBucks() {
        listAllUsers();
        int accountTo = consoleService.promptForInt("\nEnter ID of user you are sending to (0 to cancel):");
        int accountFrom = currentUser.getUser().getId();
        if (accountFrom != accountTo) {
            if (accountTo != 0) {
                BigDecimal amountToTransfer = consoleService.promptForBigDecimal("\nEnter the amount you would like to transfer:");
                handleSendBucks(accountFrom, accountTo, amountToTransfer);
            }
        } else {
            System.out.println("Error, please try again");
        }
    }

    public void handleSendBucks(int accountFrom, int accountTo, BigDecimal amountToTransfer) {
        Transfer transfer;
        boolean successful = false;
        if (amountToTransfer.compareTo(accountService.getBalance()) <= 0) {
            transfer = new Transfer(accountFrom, accountTo, amountToTransfer, TRANSFER_TYPE_ID_SEND, TRANSFER_STATUS_ID_APPROVED);
            successful = transferService.sendTransfer(transfer);
        } if (!successful) {
            System.out.println("Unable to process transaction");
        }
    }

    private void requestBucks() {
        Transfer transfer;
        listAllUsers();
        int accountFrom = consoleService.promptForInt("\nEnter ID of user you are requesting from (0 to cancel):");
        int accountTo = currentUser.getUser().getId();
        if (accountFrom != accountTo) {
            if (accountTo != 0) {
                BigDecimal amountToTransfer = consoleService.promptForBigDecimal("\nEnter the amount you would like to request:");
                transfer = new Transfer(accountFrom, accountTo, amountToTransfer, TRANSFER_TYPE_ID_REQUEST, TRANSFER_STATUS_ID_PENDING);
               if (!transferService.requestTransfer(transfer)){
                   System.out.println("Error, request failed");
               }
            }
        } else {
            System.out.println("Cannot request money from yourself");
        }
    }

    private void listAllUsers() {
        System.out.println("----------------------");
        System.out.printf("%-10s %-10s", "User ID", "Name");
        System.out.println("\n----------------------");
        User[] users = accountService.findAll();
        if (users != null) {
            for (User user : users) {
                if (!user.getUsername().equals(currentUser.getUser().getUsername())) {
                    System.out.printf("%-10s %-10s\n", user.getId(), user.getUsername());
                }
            }
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void transferDetails(int transferId) {
        System.out.println("-------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("-------------------------------------------");
        Transfer transfer = transferService.viewTransferDetails(transferId);
        System.out.println("Id: " + transfer.getTransferId());
        System.out.println("From: " + transfer.getAccountFromUsername());
        System.out.println("To: " + transfer.getAccountToUsername());
        System.out.println("Type: " + transfer.getTransferTypeDesc());
        System.out.println("Status: " + transfer.getTransferStatusDesc());
        System.out.println("Amount: $" + transfer.getAmount());
    }


}
