-- =========================
-- ROLES
-- =========================
INSERT INTO roles (role_name) VALUES
('ADMIN'),
( 'CUSTOMER');

-- =========================
-- USERS
-- =========================
INSERT INTO users (username, email, password, phone, status)
VALUES
( 'admin_user', 'admin@bank.com', '$2a$12$NpcgSi2G930CxnfnBdu1je8LUpwa.M6zwsBWm88c1xqb1KekP5Adi', '+919490847144', 'ACTIVE'),
/*Admin@1234*/
( 'john_doe', 'john@bank.com', '$2a$12$FKnTAAYEJvYCzfSMd.FL.OHjFR2AnrhKKkls9V9dSk/tXgeYfRWlO', '+918341921744', 'ACTIVE'),
/*John@1234*/
( 'jane_doe', 'jane@bank.com', '$2a$12$AI9WFzHeV43R079ga3abU.WZXUeWIA2JXdwW7arRbCWV6BNRuflau', '+918125864927', 'ACTIVE'),
/*jane@1234*/
( 'jahnavi', 'jahavi@bank.com', '$2a$12$PUhmy9t80gN63.s0hqcxMOwPi600p2k0Yyc50BEhiYBDOdrUh5zva', '+918341231344', 'ACTIVE'),
/*jahnavi@123*/
( 'vijay', 'vijay@bank.com', '$2a$12$4X/EL1Z31DT2x23NdLYd1.7ijEPc8WYqxPPil5U6Rti1MXTbD0mu.', '+919701552653', 'ACTIVE'),
( 'meena', 'meena@bank.com', '$2a$12$HJuS5Q8hT7uKp9JdE0z4PuEJ0aLz1H2gV8mKp1xQ6p5Kq2wYd2Tt6', '+918977287524', 'ACTIVE');

-- =========================
-- USER_ROLES (mapping)
-- =========================
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 2),
(5, 2),
(6, 2);

-- =========================
-- ACCOUNTS
-- =========================
INSERT INTO accounts (
    account_number,
    account_type,
    balance,
    status,
    user_id,
    branch_name,
    ifsc_code,
    nominee_name,
    nominee_relation,
    debit_card_required,
    net_banking_enabled,
    version
)
VALUES
('ACCT-20251028-00001', 'SAVINGS', 5000.00, 'ACTIVE', 2, 'Hyderabad Main Branch', 'BANK0001234', 'Mary Doe', 'Mother', TRUE, TRUE,0),
('ACCT-20251028-00002', 'SAVINGS', 7000.00, 'FROZEN', 3, 'Chennai Central Branch', 'BANK0005678', 'John Doe', 'Brother', FALSE, TRUE,0),
('ACCT-20251028-00003', 'CURRENT', 12000.00, 'ACTIVE', 4, 'Bangalore Koramangala Branch', 'BANK0009876', 'Kiran Rao', 'Father', TRUE, TRUE,0),
('ACCT-20251028-00004', 'SAVINGS', 3000.00, 'ACTIVE', 5, 'Mumbai Fort Branch', 'BANK0006543', 'Asha Vijay', 'Wife', TRUE, TRUE,0),
('ACCT-20251028-00005', 'CURRENT', 8500.00, 'ACTIVE', 6, 'Delhi Connaught Place Branch', 'BANK0001111', 'Arun Meena', 'Husband', FALSE, TRUE,0);


-- =========================
-- TRANSACTIONS
-- =========================
INSERT INTO transactions (
    from_account_number,
    to_account_number,
    amount,
    type,
    status,
    description,
    timestamp,
    is_suspicious,
    suspicious_reason
)
VALUES
('ACCT-20251028-00001', 'ACCT-20251028-00002', 1000.00, 'TRANSFER', 'SUCCESS', 'John sent 1000 to Jane', NOW(), FALSE, ''),
('ACCT-20251028-00003', 'ACCT-20251028-00004', 2500.00, 'TRANSFER', 'SUCCESS', 'Jahnavi paid Vijay for project', NOW(), FALSE, ''),
('ACCT-20251028-00002', NULL, 500.00, 'WITHDRAWAL', 'SUCCESS', 'ATM cash withdrawal', NOW(), FALSE, ''),
('ACCT-20251028-00004', NULL, 3000.00, 'DEPOSIT', 'SUCCESS', 'Cash deposit to Vijays account', NOW(), FALSE, ''),
('ACCT-20251028-00005', 'ACCT-20251028-00001', 750.00, 'TRANSFER', 'SUCCESS', 'Meena transferred to John', NOW(), FALSE, ''),
('ACCT-20251028-00001', 'ACCT-20251028-00005', 1500.00, 'TRANSFER', 'SUCCESS', 'John paid Meena', NOW(), FALSE, ''),
('ACCT-20251028-00002', 'ACCT-20251028-00003', 100.00, 'TRANSFER', 'FAILED', 'Insufficient funds during test', NOW(), TRUE, 'Suspicious repeated failure'),
('ACCT-20251028-00003', NULL, 5000.00, 'DEPOSIT', 'SUCCESS', 'Corporate deposit to Jahnavi account', NOW(), FALSE, ''),
('ACCT-20251028-00005', NULL, 2000.00, 'WITHDRAWAL', 'SUCCESS', 'Meena withdrew for bill payments', NOW(), FALSE, '');

-- =========================
-- ACCOUNT REQUESTS
-- =========================
INSERT INTO account_requests (
   
    account_type,
    branch_name,
    debit_card_required,
    ifs_code,
    initial_deposit,
    net_banking_enabled,
    nominee_name,
    nominee_relation,
    status,
    user_id
)
VALUES
( 'SAVINGS', 'Pune MG Road Branch', TRUE, 'BANK0009001', 5000.00, TRUE, 'Rahul Kumar', 'Father', 'PENDING', 3),
( 'CURRENT', 'Delhi Rajouri Branch', FALSE, 'BANK0007001', 2000.00, TRUE, 'Anjali Meena', 'Wife', 'PENDING', 6);

INSERT INTO audit_logs (action, user_id, performed_by, details, timestamp)
VALUES ('SUSPICIOUS_TRANSACTION', 3, 'SYSTEM', 'Repeated failed transfer attempts from account ACCT-20251028-00002 detected.', NOW());

INSERT INTO audit_logs (action, user_id, performed_by, details, timestamp)
VALUES ('ACCOUNT_FROZEN', 3, 'SYSTEM', 'Account ACCT-20251028-00002 frozen automatically due to suspicious activities.', NOW());

