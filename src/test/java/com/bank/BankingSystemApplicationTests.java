package com.bank;

import com.bank.dao.AccountDao;
import com.bank.dao.entity.AccountEntity;
import com.bank.service.TransactionService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class BankingSystemApplicationTests {

    @Autowired
    TransactionService transactionService;

	@Autowired
	AccountDao accountDao;

    @Test
    void testConcurrentWithdrawals() throws InterruptedException {
        String accountNumber = "ACC1000002"; // test account
        double withdrawal1 = 2000;
        double withdrawal2 = 3000;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread 1
        executor.submit(() -> {
            boolean success = false;
            while (!success) {
                try {
                    System.out.println("Thread 1: Trying to withdraw " + withdrawal1);
                    var result = transactionService.withdraw(accountNumber, withdrawal1);
                    System.out.println("Thread 1 result: " + result.getStatus());
                    success = true; // exit loop on success
					 AccountEntity account = accountDao.findById(accountNumber).orElse(null);
            		if (account != null) {
                		System.out.println("Thread 1 sees balance: " + account.getBalance());
            		}
                } catch (Exception e) {
                    System.out.println("Thread 1 deadlock or failure, retrying... " + e.getMessage());
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        });

        // Thread 2
        executor.submit(() -> {
            boolean success = false;
            while (!success) {
                try {
                    System.out.println("Thread 2: Trying to withdraw " + withdrawal2);
                    var result = transactionService.withdraw(accountNumber, withdrawal2);
                    System.out.println("Thread 2 result: " + result.getStatus());
					 AccountEntity account = accountDao.findById(accountNumber).orElse(null);
            		if (account != null) {
                		System.out.println("Thread 2 sees balance: " + account.getBalance());
            		}
                    success = true; // exit loop on success
                } catch (Exception e) {
                    System.out.println("Thread 2 deadlock or failure, retrying... " + e.getMessage());
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        });

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        System.out.println("All withdrawals completed.");
    }

@Test
    void testAtomicityFailsWithoutTransactional() {
        // Get initial balances
        AccountEntity fromAccount = accountDao.findById("ACC1000001").get();
        AccountEntity toAccount = accountDao.findById("ACC1000002").get();
        double initialFrom = fromAccount.getBalance();
        double initialTo = toAccount.getBalance();

        try {
            // Perform a transfer that fails mid-way (simulate crash inside method)
            transactionService.transfer("ACC1000001", "ACC1000002", 500);
        } catch (Exception ignored) {
        }

        // Reload accounts
        double afterFrom = accountDao.findById("ACC1000001").get().getBalance();
        double afterTo = accountDao.findById("ACC1000002").get().getBalance();

        System.out.println("Initial From: " + initialFrom + ", After From: " + afterFrom);
        System.out.println("Initial To: " + initialTo + ", After To: " + afterTo);

        // Here atomicity fails: sender is deducted but receiver never received
        Assertions.assertNotEquals(initialFrom, afterFrom, "From account changed without full transfer");
        Assertions.assertEquals(initialTo, afterTo, "To account unchanged due to failure");
    }

}
