package com.bank.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPojo {

    private Integer userId;
    private String username;
    private String email;
    private String password;
    private String phone;
    private Status status;
    public enum Status { ACTIVE, INACTIVE }
    private List<RolePojo> roles;
    List<AccountPojo> accounts;
    public Status getStatus() { 
        return this.status;
     }
    public void setStatus(Status status) { 
        this.status = status;
     }


    public UserPojo(Integer userId, String username, String email, String password, String phone, Status status) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.status = status;
    }
     public UserPojo(Integer userId, String username, String email, String password, String phone, Status status,List<RolePojo> roles) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.status = status;
        this.roles=roles;
    }

}
