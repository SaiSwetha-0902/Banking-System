package com.bank.dao;

import com.bank.dao.entity.AccountRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRequestDao extends JpaRepository<AccountRequestEntity, Long> {
    List<AccountRequestEntity> findByStatus(AccountRequestEntity.Status status);
    List<AccountRequestEntity> findByUser_UserId(Long userId);
}
