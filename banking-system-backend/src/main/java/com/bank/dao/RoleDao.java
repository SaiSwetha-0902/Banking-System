package com.bank.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.dao.entity.RoleEntity;


public interface RoleDao extends JpaRepository<RoleEntity, Integer>{

    RoleEntity findByRoleName(String roleName);
}
