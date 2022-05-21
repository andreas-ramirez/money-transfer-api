package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class JdbcAccountDaoTest {


    private static SingleConnectionDataSource dataSource;
    JdbcAccountDao accountDao;

    private static final Account andyTest = new Account(9001, 8001, new BigDecimal("1000.00"));
    private static final Account nicoleTest = new Account(9002, 8002, new BigDecimal("1001.00"));

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
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(tenmoUserOne);
        jdbcTemplate.update(tenmoUserTwo);
        jdbcTemplate.update(sqlAccountOne);
        jdbcTemplate.update(sqlAccountTwo);
        accountDao = new JdbcAccountDao(dataSource);
    }

    @Test
    public void check_if_user_id_returns_correct_balance(){
        BigDecimal balance = accountDao.getBalance(8001);
        BigDecimal balanceTwo = accountDao.getBalance(8002);

        Assert.assertEquals(andyTest.getBalance(),balance);
        Assert.assertEquals(nicoleTest.getBalance(),balanceTwo);
    }

    @Test
    public void check_if_account_id_returns_correct_balance(){
        BigDecimal balance = accountDao.getBalanceByAccountId(9001);
        BigDecimal balanceTwo = accountDao.getBalanceByAccountId(9002);

        Assert.assertEquals(andyTest.getBalance(),balance);
        Assert.assertEquals(nicoleTest.getBalance(),balanceTwo);
    }

    @Test
    public void withdrawal_bucks_lowers_the_account_balance_and_returns_true(){
        boolean withdrawalSuccessful = accountDao.withdrawalBucks(9001, BigDecimal.valueOf(100.00));
        BigDecimal balance = accountDao.getBalanceByAccountId(9001);

        boolean withdrawalTwoSuccessful = accountDao.withdrawalBucks(9002, BigDecimal.valueOf(50.00));
        BigDecimal balanceTwo = accountDao.getBalanceByAccountId(9002);

        Assert.assertTrue("withdrawalBucks was not successful",withdrawalSuccessful);
        Assert.assertEquals(new BigDecimal("900.00"),balance);

        Assert.assertTrue("withdrawalBucks was not successful",withdrawalTwoSuccessful);
        Assert.assertEquals(new BigDecimal("951.00"),balanceTwo);
    }

    @Test
    public void withdrawal_bucks_returns_false_when_amount_greater_than_balance(){
        boolean withdrawalSuccess = accountDao.withdrawalBucks(9001, BigDecimal.valueOf(2000.00));
        BigDecimal balance = accountDao.getBalanceByAccountId(9001);

        boolean withdrawalTwoSuccess = accountDao.withdrawalBucks(9002, BigDecimal.valueOf(2000.00));
        BigDecimal balanceTwo = accountDao.getBalanceByAccountId(9002);

        Assert.assertFalse("withdrawalBucks was successful, but account has insufficient funds",withdrawalSuccess);
        Assert.assertEquals(new BigDecimal("1000.00"),balance);

        Assert.assertFalse("withdrawalBucks was successful, but account has insufficient funds",withdrawalTwoSuccess);
        Assert.assertEquals(new BigDecimal("1001.00"),balanceTwo);
    }

    @Test
    public void deposit_bucks_returns_true_and_increases_account_balance(){
        boolean depositSuccessful = accountDao.depositBucks(9001, BigDecimal.valueOf(100.00));
        BigDecimal balance = accountDao.getBalanceByAccountId(9001);

        boolean depositTwoSuccessful = accountDao.depositBucks(9002, BigDecimal.valueOf(50.00));
        BigDecimal balanceTwo = accountDao.getBalanceByAccountId(9002);

        Assert.assertTrue("deposit was not successful",depositSuccessful);
        Assert.assertEquals(new BigDecimal("1100.00"),balance);

        Assert.assertTrue("deposit was not successful",depositTwoSuccessful);
        Assert.assertEquals(new BigDecimal("1051.00"),balanceTwo);
    }

    @Test
    public void deposit_bucks_returns_false_with_improper_account_id(){
        boolean depositSuccessful = accountDao.depositBucks(99999, BigDecimal.valueOf(100.00));
        boolean depositTwoSuccessful = accountDao.depositBucks(-9002, BigDecimal.valueOf(50.00));

        Assert.assertFalse("depositBucks was successful, but accountId did not exist",depositSuccessful);
        Assert.assertFalse("depositBucks was successful, but accountId did not exist",depositTwoSuccessful);
    }

    @Test
    public void user_id_provides_proper_account_id(){
        int accountId = accountDao.getAccountIdByUserId(8001);
        Assert.assertEquals(accountId, andyTest.getAccountId());

        int accountIdTwo = accountDao.getAccountIdByUserId(8002);
        Assert.assertEquals(accountIdTwo, nicoleTest.getAccountId());
    }

    @Test
    public void invalid_user_id_returns_zero(){
        int accountId = accountDao.getAccountIdByUserId(88888);
        Assert.assertEquals(0,accountId);

        int accountIdTwo = accountDao.getAccountIdByUserId(-8002);
        Assert.assertEquals(0,accountIdTwo);
    }

    @After
    public void rollback() throws SQLException {
        dataSource.getConnection().rollback();
    }

    @AfterClass
    public static void closeDataSource(){
        dataSource.destroy();
    }
}
