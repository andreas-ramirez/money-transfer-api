# Money Transfer Application

## Overview
This a P2P money transfer application running off a CLI. Built off of Java and Spring Boot - this implement both the server and client for full interactivity.  

## How To Use
The server and the client should be launched in separate windows due to the program's structure and the way the dependencies are structured.  
  
To populate an SQL database, the database folder has a .sql file to quickly populate the necessary tables before launching/

### Future Developments
- Develop full frontend UI
- Expand database structure
- Implement mobile use (for testing purposes)

### Use Cases

1. As a user of the system, I need to be able to register myself with a username and password.
   1. A new registered user starts with an initial balance of 1,000 TE Bucks.
   2. The ability to register has been provided in your starter code.
2. As a user of the system, I need to be able to log in using my registered username and password.
   1. Logging in returns an Authentication Token. I need to include this token with all my subsequent interactions with the system outside of registering and logging in.
   2. The ability to log in has been provided in your starter code.
3. As an authenticated user of the system, I need to be able to see my Account Balance.
4. As an authenticated user of the system, I need to be able to *send* a transfer of a specific amount of TE Bucks to a registered user.
   1. I should be able to choose from a list of users to send TE Bucks to.
   2. I must not be allowed to send money to myself.
   3. A transfer includes the User IDs of the from and to users and the amount of TE Bucks.
   4. The receiver's account balance is increased by the amount of the transfer.
   5. The sender's account balance is decreased by the amount of the transfer.
   6. I can't send more TE Bucks than I have in my account.
   7. I can't send a zero or negative amount.
   8. A Sending Transfer has an initial status of *Approved*.
5. As an authenticated user of the system, I need to be able to see transfers I have sent or received.
6. As an authenticated user of the system, I need to be able to retrieve the details of any transfer based upon the transfer ID.
7. As an authenticated user of the system, I need to be able to *request* a transfer of a specific amount of TE Bucks from another registered user.
   1. I should be able to choose from a list of users to request TE Bucks from.
   2. I must not be allowed to request money from myself.
   3. I can't request a zero or negative amount.
   4. A transfer includes the User IDs of the from and to users and the amount of TE Bucks.
   5. A Request Transfer has an initial status of *Pending*.
   6. No account balance changes until the request is approved.
   7. The transfer request should appear in both users' list of transfers (use case #5).
8. As an authenticated user of the system, I need to be able to see my *Pending* transfers.

9. As an authenticated user of the system, I need to be able to either approve or reject a Request Transfer.
   
   I can't "approve" a given Request Transfer for more TE Bucks than I have in my account.
   
   The Request Transfer status is *Approved* if I approve, or *Rejected* if I reject the request.
   
   If the transfer is approved, the requester's account balance is increased by the amount of the request.
   
   If the transfer is approved, the requestee's account balance is decreased by the amount of the request.
   
   If the transfer is rejected, no account balance changes.
