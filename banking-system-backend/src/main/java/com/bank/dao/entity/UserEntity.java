package com.bank.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-20 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+]).{8,20}$",
        message = "Password must contain at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character"
    )
    @Column(nullable = false)
    private String password;


    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private List<RoleEntity> roles;


    public enum Status { ACTIVE, INACTIVE }

   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   private List<AccountEntity> accounts;

   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<AccountRequestEntity> accountRequests;


}
