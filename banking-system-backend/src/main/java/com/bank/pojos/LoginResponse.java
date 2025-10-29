package com.bank.pojos;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Integer userId;
    private List<RolePojo> allRoles;
}
