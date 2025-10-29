package com.bank.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.bank.dao.RoleDao;
import com.bank.dao.UserDao;
import com.bank.dao.entity.RoleEntity;
import com.bank.dao.entity.UserEntity;
import com.bank.pojos.RolePojo;
import com.bank.pojos.UserPojo;



import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

   public UserPojo createCustomer(UserPojo userPojo) {

    UserEntity userEntity = new UserEntity();
    userEntity.setUsername(userPojo.getUsername());
    userEntity.setEmail(userPojo.getEmail());
    userEntity.setPassword(userPojo.getPassword()); 
    userEntity.setPhone(userPojo.getPhone());
    userEntity.setStatus(UserEntity.Status.ACTIVE);

    RoleEntity customerRole = roleDao.findByRoleName("CUSTOMER");
    userEntity.setRoles(List.of(customerRole));


    UserEntity savedEntity = userDao.saveAndFlush(userEntity);


    List<RolePojo> rolesPojo = savedEntity.getRoles().stream()
            .map(role -> new RolePojo(role.getRoleId(), role.getRoleName()))
            .collect(Collectors.toList());

    UserPojo savedUserPojo = new UserPojo(
            savedEntity.getUserId(),
            savedEntity.getUsername(),
            savedEntity.getEmail(),
            null, 
            savedEntity.getPhone(),
            UserPojo.Status.valueOf(savedEntity.getStatus().name()),
            rolesPojo
    );

    return savedUserPojo;
}

    public UserPojo getCustomerById(Integer userId) {
        UserEntity userEntity = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
  
        UserPojo userPojo = new UserPojo(
            userEntity.getUserId(),
            userEntity.getUsername(),
            userEntity.getEmail(),
            null, 
            userEntity.getPhone(),
            UserPojo.Status.valueOf(userEntity.getStatus().name())
    );

        return userPojo;
    }


    public UserPojo getCustomerByEmail(String email) {
        UserEntity userEntity = userDao.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        UserPojo userPojo = new UserPojo(
            userEntity.getUserId(),
            userEntity.getUsername(),
            userEntity.getEmail(),
            null, 
            userEntity.getPhone(),
            UserPojo.Status.valueOf(userEntity.getStatus().name())
    );

        return userPojo;
    }

    public List<UserPojo> getAllCustomers() {
        return userDao.findAll().stream()
                .map((userEntity)->{
                     UserPojo userPojo = new UserPojo(
                        userEntity.getUserId(),
                        userEntity.getUsername(),
                        userEntity.getEmail(),
                        null, 
                        userEntity.getPhone(),
                        UserPojo.Status.valueOf(userEntity.getStatus().name()));

                        return userPojo;
                })
                .collect(Collectors.toList());
    }

    // --------------------------
    // Update customer
    // --------------------------
    public UserPojo updateCustomer(Integer userId, UserPojo userPojo) {
        UserEntity existing = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        existing.setUsername(userPojo.getUsername());
        existing.setEmail(userPojo.getEmail());
        existing.setPhone(userPojo.getPhone());
       if (userPojo.getStatus() != null) {
            existing.setStatus(UserEntity.Status.valueOf(userPojo.getStatus().name()));
        }

        UserEntity userEntity = userDao.save(existing);

         UserPojo resUserPojo = new UserPojo(
                        userEntity.getUserId(),
                        userEntity.getUsername(),
                        userEntity.getEmail(),
                        null, 
                        userEntity.getPhone(),
                        UserPojo.Status.valueOf(userEntity.getStatus().name()));

                     

        return resUserPojo;
    }

    
    public void deleteCustomer(Integer userId) {
        userDao.deleteById(userId);
    }
}
