package com.bank.pojos;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String email;
    private String password;
}
