package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.validation.constraints.AssertFalse;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class JdbcTransferDaoTest {

    private static SingleConnectionDataSource dataSource;
    JdbcTransferDao transferDao;
    JdbcAccountDao accountDao;

    private static final Account andyTest = new Account(9001, 8001, new BigDecimal("1000.00"));
    private static final Account nicoleTest = new Account(9002, 8002, new BigDecimal("1001.00"));
    private static final Transfer transferTest = new Transfer(7001, 2, 2,
            9001, 9002, new BigDecimal("10.00"));
    private static final Transfer transferTestTwo = new Transfer(7002, 2, 2,
            9002, 9001, new BigDecimal("15.00"));
    private static final Transfer errorTransferTest = new Transfer(7003, 2, 2,
            9001, 9001, new BigDecimal("10.00"));
    private static final Transfer errorTransferTestTwo = new Transfer(7001, 2, 2,
            9001, 9002, new BigDecimal("10000.00"));
    private static final Transfer transferTestRequest = new Transfer(7005,1,1,
            9001,9002, new BigDecimal("10.00"));
    private static final Transfer transferRequestApproved = new Transfer(7006,1,2,
            9001,9002,new BigDecimal("10.00"));


    @BeforeClass
    public static void setup() {
        dataSource = new SingleConnectionDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/tenmo");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres1");
        dataSource.setAutoCommit(false);
    }

    @Before
    public void setupData(){
        String tenmoUserOne = "INSERT INTO tenmo_user (user_id, username, password_hash) " +
                "VALUES (8001, 'andyTest', 'andy');";
        String tenmoUserTwo = "INSERT INTO tenmo_user (user_id, username, password_hash) " +
                "VALUES (8002, 'nicoleTest', 'nicole');";
        String sqlAccountOne = "INSERT INTO account (account_id, user_id, balance) " +
                "VALUES (9001, 8001, 1000.00);";
        String sqlAccountTwo = "INSERT INTO account (account_id, user_id, balance) " +
                "VALUES (9002, 8002, 1001.00);";
        String transferOne = "INSERT INTO transfer (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (7001,2,2,9001,9002,10);";
        String transferTwo = "INSERT INTO transfer (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (7002,2,2,9002,9001,15);";
        String transferThree = "INSERT INTO transfer (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (7005,1,1,9001,9002,10);";
        String transferFour = "INSERT INTO transfer (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (7006,1,2,9001,9002,10);";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(tenmoUserOne);
        jdbcTemplate.update(tenmoUserTwo);
        jdbcTemplate.update(sqlAccountOne);
        jdbcTemplate.update(sqlAccountTwo);
        jdbcTemplate.update(transferOne);
        jdbcTemplate.update(transferTwo);
        jdbcTemplate.update(transferThree);
        jdbcTemplate.update(transferFour);
        accountDao = new JdbcAccountDao(dataSource);
        transferDao = new JdbcTransferDao(accountDao, dataSource);
    }

    @After
    public void rollback() throws SQLException {
        dataSource.getConnection().rollback();
    }

    @AfterClass
    public static void closeDataSource(){
        dataSource.destroy();
    }

    @Test
    public void send_transfer_returns_true_when_transfer_successful(){
        boolean sendATransfer = transferDao.sendTransfer(transferTest);
        Assert.assertTrue("Send transfer was not successful", sendATransfer);
        BigDecimal balance = accountDao.getBalanceByAccountId(9001);
        Assert.assertEquals(new BigDecimal("990.00"), balance);
        BigDecimal balanceReceived = accountDao.getBalanceByAccountId(9002);
        Assert.assertEquals(new BigDecimal("1011.00"), balanceReceived);
    }

    @Test
    public void send_transfer_returns_true_when_transfer_successful_test_two(){
        boolean sendATransferTwo = transferDao.sendTransfer(transferTestTwo);
        Assert.assertTrue("Send transfer was not successful", sendATransferTwo);
        BigDecimal balance = accountDao.getBalanceByAccountId(9002);
        Assert.assertEquals(new BigDecimal("986.00"), balance);
        BigDecimal balanceReceived = accountDao.getBalanceByAccountId(9001);
        Assert.assertEquals(new BigDecimal("1015.00"), balanceReceived);
    }

    @Test
    public void send_transfer_returns_false_when_transfer_incorrect(){
        boolean sendErrorTransfer = transferDao.sendTransfer(errorTransferTest);
        Assert.assertFalse(sendErrorTransfer);
        boolean sendErrorTransferTwo = transferDao.sendTransfer(errorTransferTestTwo);
        Assert.assertFalse(sendErrorTransferTwo);
        BigDecimal balance = accountDao.getBalanceByAccountId(9001);
        Assert.assertEquals(new BigDecimal("1000.00"), balance);
        BigDecimal balanceTwo = accountDao.getBalanceByAccountId(9002);
        Assert.assertEquals(new BigDecimal("1001.00"), balanceTwo);
    }

    @Test
    public void view_transfers_returns_list_of_transfers_for_andyTest(){
        List<Transfer> transferList = transferDao.viewTransfers("andyTest");
        Assert.assertEquals(4, transferList.size());
        Assert.assertEquals(7001, transferList.get(0).getTransferId());
        Assert.assertEquals("nicoleTest", transferList.get(0).getAccountToUsername());
        Assert.assertEquals(new BigDecimal("10.00"), transferList.get(0).getAmount());
    }

    @Test
    public void view_transfers_returns_list_of_transfers_for_nicoleTest(){
        List<Transfer> transferList = transferDao.viewTransfers("nicoleTest");
        Assert.assertEquals(4, transferList.size());
        Assert.assertEquals(7001, transferList.get(0).getTransferId());
        Assert.assertEquals("andyTest", transferList.get(0).getAccountFromUsername());
        Assert.assertEquals(new BigDecimal("10.00"), transferList.get(0).getAmount());
    }

    @Test
    public void get_username_by_account_id_returns_correct_user(){
        String username = transferDao.getUsernameByAccountId(9001);
        String usernameTwo = transferDao.getUsernameByAccountId(9002);

        Assert.assertEquals("andyTest", username);
        Assert.assertEquals("nicoleTest", usernameTwo);
    }

    @Test
    public void get_username_by_account_id_returns_null_with_improper_account_id(){
        String username = transferDao.getUsernameByAccountId(99999);
        String usernameTwo = transferDao.getUsernameByAccountId(-9002);

        Assert.assertNull(username);
        Assert.assertNull(usernameTwo);
    }

    @Test
    public void view_transfer_details_returns_correct_transfer_object(){
        Transfer transferTest = transferDao.viewTransferDetails(7001);
        Assert.assertEquals(7001, transferTest.getTransferId());
        Assert.assertEquals("Send", transferTest.getTransferTypeDesc());
        Assert.assertEquals("Approved", transferTest.getTransferStatusDesc());
        Assert.assertEquals("andyTest", transferTest.getAccountFromUsername());
        Assert.assertEquals("nicoleTest", transferTest.getAccountToUsername());
        Assert.assertEquals(new BigDecimal("10.00"), transferTest.getAmount());
    }

    @Test
    public void view_transfer_details_returns_null_with_improper_transfer_id(){
        Transfer transferTest = transferDao.viewTransferDetails(99999);
        Assert.assertEquals(0, transferTest.getTransferId());
        assertNull(transferTest.getTransferTypeDesc());
        assertNull(transferTest.getTransferStatusDesc());
        assertNull(transferTest.getAccountFromUsername());
        assertNull(transferTest.getAccountToUsername());
        assertNull(transferTest.getAmount());
    }

    @Test
    public void request_transfer_returns_true_when_successful(){
        boolean makeRequest = transferDao.requestTransfer(transferTestRequest);
        assertTrue(makeRequest);
        Assert.assertEquals(7005, transferTestRequest.getTransferId());
        Assert.assertEquals(1, transferTestRequest.getTransferTypeId());
        Assert.assertEquals(1, transferTestRequest.getTransferStatusId());
        Assert.assertEquals(9001, transferTestRequest.getAccountFrom());
        Assert.assertEquals(9002, transferTestRequest.getAccountTo());
        Assert.assertEquals(new BigDecimal("10.00"), transferTestRequest.getAmount());
    }

    @Test
    public void update_pending_transfer_updates_balances_when_successful(){
        boolean updateTransfer = transferDao.updatePendingTransfer(transferRequestApproved);
        assertTrue(updateTransfer);
        Assert.assertEquals(new BigDecimal("990.00"), accountDao.getBalanceByAccountId(9001));
        Assert.assertEquals(new BigDecimal("1011.00"), accountDao.getBalanceByAccountId(9002));
    }

    @Test
    public void view_pending_requests_returns_list_and_correct_variables(){
        List<Transfer> pendingRequest = transferDao.viewPendingRequests("andyTest");
        Assert.assertEquals(1, pendingRequest.size());
        Assert.assertEquals(7005, pendingRequest.get(0).getTransferId());
        Assert.assertEquals("nicoleTest", pendingRequest.get(0).getUsername());
        Assert.assertEquals(new BigDecimal("10.00"), pendingRequest.get(0).getAmount());
    }

}
