package com.bank.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.dao.entity.UserEntity;

public interface UserDao extends JpaRepository<UserEntity, Integer> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
}
