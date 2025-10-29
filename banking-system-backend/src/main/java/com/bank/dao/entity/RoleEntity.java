package com.bank.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @NotBlank(message = "Role name is required")
    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private List<UserEntity> users;

}
