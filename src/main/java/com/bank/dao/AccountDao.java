package com.bank.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.dao.entity.AccountEntity;
import com.bank.dao.entity.UserEntity;

public interface AccountDao extends JpaRepository<AccountEntity,String> {

    Optional<AccountEntity> findById(String accountNumber);
    List<AccountEntity> findByUser(UserEntity user);
    
}
