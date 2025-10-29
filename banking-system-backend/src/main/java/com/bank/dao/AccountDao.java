package com.bank.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.dao.entity.AccountEntity;
import com.bank.dao.entity.UserEntity;

public interface AccountDao extends JpaRepository<AccountEntity,String> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    List<AccountEntity> findByUser(UserEntity user);
     boolean existsByUserAndAccountType(UserEntity user, AccountEntity.AccountType accountType);

    // Option B (alternate): check by userId and accountType — useful if you only have the id
    boolean existsByUser_UserIdAndAccountType(Integer userId, AccountEntity.AccountType accountType);
    
}
