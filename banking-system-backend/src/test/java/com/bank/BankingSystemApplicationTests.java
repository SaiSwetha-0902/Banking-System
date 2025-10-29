package com.bank;

import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.dao.entity.AccountEntity;
import com.bank.pojos.TransactionPojo;
import com.bank.service.TransactionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BankingSystemApplicationTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountDao accountRepository;

    @Autowired
    private TransactionDao transactionRepository;

    // Using your existing account numbers from the SQL
    private static final String FROM_ACCOUNT_1 = "ACCT-20251028-00001"; // John's account
    private static final String FROM_ACCOUNT_2 = "ACCT-20251028-00003"; // Jahnavi's account  
    private static final String TO_ACCOUNT = "ACCT-20251028-00005";     // Meena's account

    private static final double TRANSFER_AMOUNT = 200.0;
@Test
void testConcurrentTransfersToSameTargetAccount() throws Exception {
    System.out.println("=== Testing TRULY Concurrent Transfers to Same Account ===");
    System.out.println("From: " + FROM_ACCOUNT_1 + " & " + FROM_ACCOUNT_2);
    System.out.println("To: " + TO_ACCOUNT);
    System.out.println("Amount: " + TRANSFER_AMOUNT);

    // Get initial balances
    AccountEntity toAccount = accountRepository.findById(TO_ACCOUNT).orElseThrow();
    double initialTargetBalance = toAccount.getBalance();
    System.out.println("Initial balance of target account: " + initialTargetBalance);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);
    CountDownLatch startLatch = new CountDownLatch(1); // Both threads wait for this
    CountDownLatch endLatch = new CountDownLatch(2);   // Main thread waits for both to finish
    ExecutorService executor = Executors.newFixedThreadPool(2);

    // Thread 1: Transfer from ACCT-20251028-00001 to ACCT-20251028-00005
    executor.submit(() -> {
        try {
            System.out.println("🚀 Thread 1 READY: " + FROM_ACCOUNT_1 + " -> " + TO_ACCOUNT);
            startLatch.await(); // Wait for the start signal
            
            System.out.println("🎬 Thread 1 STARTING: " + FROM_ACCOUNT_1 + " -> " + TO_ACCOUNT);
            var result = transactionService.transfer(FROM_ACCOUNT_1, TO_ACCOUNT, TRANSFER_AMOUNT);
            
            if (result.getStatus() == TransactionPojo.Status.SUCCESS) {
                successCount.incrementAndGet();
                System.out.println("✅ Thread 1: SUCCESS - " + result.getDescription());
            } else {
                failureCount.incrementAndGet();
                System.out.println("❌ Thread 1: FAILED - " + result.getDescription());
            }
        } catch (Exception e) {
            failureCount.incrementAndGet();
            System.out.println("❌ Thread 1: EXCEPTION - " + e.getMessage());
        } finally {
            endLatch.countDown();
        }
    });

    // Thread 2: Transfer from ACCT-20251028-00003 to ACCT-20251028-00005
    executor.submit(() -> {
        try {
            System.out.println("🚀 Thread 2 READY: " + FROM_ACCOUNT_2 + " -> " + TO_ACCOUNT);
            startLatch.await(); // Wait for the same start signal
            
            System.out.println("🎬 Thread 2 STARTING: " + FROM_ACCOUNT_2 + " -> " + TO_ACCOUNT);
            var result = transactionService.transfer(FROM_ACCOUNT_2, TO_ACCOUNT, TRANSFER_AMOUNT);
            
            if (result.getStatus() == TransactionPojo.Status.SUCCESS) {
                successCount.incrementAndGet();
                System.out.println("✅ Thread 2: SUCCESS - " + result.getDescription());
            } else {
                failureCount.incrementAndGet();
                System.out.println("❌ Thread 2: FAILED - " + result.getDescription());
            }
        } catch (Exception e) {
            failureCount.incrementAndGet();
            System.out.println("❌ Thread 2: EXCEPTION - " + e.getMessage());
        } finally {
            endLatch.countDown();
        }
    });

    // Give threads time to initialize and get ready
    Thread.sleep(1000);
    System.out.println("🏁 STARTING BOTH TRANSACTIONS SIMULTANEOUSLY!");
    startLatch.countDown(); // Release both threads at the same time

    // Wait for both threads to complete
    boolean completed = endLatch.await(10, TimeUnit.SECONDS);
    assertTrue(completed, "Test should complete within timeout");

    executor.shutdown();
    executor.awaitTermination(2, TimeUnit.SECONDS);

    // Verify final balance
    AccountEntity finalToAccount = accountRepository.findById(TO_ACCOUNT).orElseThrow();
    double finalBalance = finalToAccount.getBalance();
    
    System.out.println("\n=== TEST RESULTS ===");
    System.out.println("Success count: " + successCount.get());
    System.out.println("Failure count: " + failureCount.get());
    System.out.println("Initial balance: " + initialTargetBalance);
    System.out.println("Final balance: " + finalBalance);
    System.out.println("Balance difference: " + (finalBalance - initialTargetBalance));

    // Assertions
    assertEquals(1, successCount.get(), "Exactly one transaction should succeed");
    assertEquals(1, failureCount.get(), "Exactly one transaction should fail");
    
    double expectedBalance = initialTargetBalance + TRANSFER_AMOUNT;
    assertEquals(expectedBalance, finalBalance, 0.001, 
                "Target account should receive exactly one transfer amount (200)");

    System.out.println("🎉 TEST PASSED: One transaction succeeded, one failed due to optimistic locking!");
}
  

    @Test
    void testSequentialTransfersBothSucceed() {
        System.out.println("=== Testing Sequential Transfers ===");
        
        AccountEntity toAccount = accountRepository.findById(TO_ACCOUNT).orElseThrow();
        double initialBalance = toAccount.getBalance();
        System.out.println("Initial balance: " + initialBalance);

        // First transfer
        System.out.println("1st transfer: " + FROM_ACCOUNT_1 + " -> " + TO_ACCOUNT);
        var result1 = transactionService.transfer(FROM_ACCOUNT_1, TO_ACCOUNT, TRANSFER_AMOUNT);
        assertEquals(TransactionPojo.Status.SUCCESS, result1.getStatus(), "First transfer should succeed");

        // Check intermediate balance
        AccountEntity intermediateAccount = accountRepository.findById(TO_ACCOUNT).orElseThrow();
        double intermediateBalance = intermediateAccount.getBalance();
        System.out.println("After 1st transfer: " + intermediateBalance);

        // Small delay
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Second transfer
        System.out.println("2nd transfer: " + FROM_ACCOUNT_2 + " -> " + TO_ACCOUNT);
        var result2 = transactionService.transfer(FROM_ACCOUNT_2, TO_ACCOUNT, TRANSFER_AMOUNT);
        assertEquals(TransactionPojo.Status.SUCCESS, result2.getStatus(), "Second transfer should succeed when sequential");

        // Verify final balance
        AccountEntity finalToAccount = accountRepository.findById(TO_ACCOUNT).orElseThrow();
        double finalBalance = finalToAccount.getBalance();
        double expectedBalance = initialBalance + (2 * TRANSFER_AMOUNT);
        
        System.out.println("Final balance: " + finalBalance);
        System.out.println("Expected balance: " + expectedBalance);

        assertEquals(expectedBalance, finalBalance, 0.001,
                    "Target account should receive both transfers when sequential");

        System.out.println("✅ Sequential test passed - Both transfers succeeded!");
    }

    @Test
    void testConcurrentTransfersWithCompletableFuture() throws Exception {
        System.out.println("=== Testing with CompletableFuture ===");

        double initialBalance = accountRepository.findById(TO_ACCOUNT)
                .orElseThrow().getBalance();

        // Execute transfers concurrently
        CompletableFuture<TransactionPojo> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Future 1: Transferring from " + FROM_ACCOUNT_1);
            return transactionService.transfer(FROM_ACCOUNT_1, TO_ACCOUNT, TRANSFER_AMOUNT);
        });

        CompletableFuture<TransactionPojo> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50); // Ensure concurrency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Future 2: Transferring from " + FROM_ACCOUNT_2);
            return transactionService.transfer(FROM_ACCOUNT_2, TO_ACCOUNT, TRANSFER_AMOUNT);
        });

        // Wait for both to complete
        TransactionPojo result1 = future1.get(5, TimeUnit.SECONDS);
        TransactionPojo result2 = future2.get(5, TimeUnit.SECONDS);

        System.out.println("Result 1: " + result1.getStatus() + " - " + result1.getDescription());
        System.out.println("Result 2: " + result2.getStatus() + " - " + result2.getDescription());

        // Verify one success, one failure
        boolean oneSuccessOneFailure = 
            (result1.getStatus() == TransactionPojo.Status.SUCCESS && result2.getStatus() == TransactionPojo.Status.FAILED) ||
            (result1.getStatus() == TransactionPojo.Status.FAILED && result2.getStatus() == TransactionPojo.Status.SUCCESS);

        assertTrue(oneSuccessOneFailure, 
            "One transfer should succeed and one should fail due to concurrent access");

        // Verify final balance
        double finalBalance = accountRepository.findById(TO_ACCOUNT).orElseThrow().getBalance();
        assertEquals(initialBalance + TRANSFER_AMOUNT, finalBalance, 0.001,
                    "Only one transfer amount should be added");

        System.out.println("✅ CompletableFuture test passed!");
    }
}