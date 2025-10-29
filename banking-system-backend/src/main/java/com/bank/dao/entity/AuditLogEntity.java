package com.bank.dao.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;  // e.g., ACCOUNT_FROZEN, TRANSACTION_FLAGGED, USER_DEACTIVATED
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "performed_by")
    private String performedBy;  // admin username or "SYSTEM" if automated

    @Column(columnDefinition = "TEXT")
    private String details;  // Optional details or JSON describing the action

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

}
