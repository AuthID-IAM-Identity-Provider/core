-- #####################################################################
-- # Bagian 0: Skema Tabel Pengguna (Users)
-- # Tabel fundamental yang menyimpan data semua pengguna terdaftar.
-- #####################################################################

-- Enhanced Users Table Schema
-- Comprehensive user management with additional information fields

CREATE TABLE users (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Basic User Information
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    email_verified_at TIMESTAMP NULL,
    password VARCHAR(255) NOT NULL,

    -- User Status (Enum stored as String)
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION' CHECK (status IN ('PENDING_VERIFICATION','ACTIVE', 'INCOMPLETE_PROFILE','SUSPENDED','LOCKED','DORMANT','DEACTIVATED', 'BANNED','DELETED','INVITED')),

    -- Security and Authentication
    remember_token VARCHAR(100) NULL,
    two_factor_secret VARCHAR(255) NULL,
    two_factor_recovery_codes JSON NULL,
    two_factor_confirmed_at TIMESTAMP NULL,
    password_changed_at TIMESTAMP NULL,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,

    -- Activity Tracking
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(45) NULL,
    last_activity_at TIMESTAMP NULL,
    login_count INT NOT NULL DEFAULT 0,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes for Performance
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_email_status (email, status),
    INDEX idx_can_login (can_login),
    INDEX idx_can_perform_actions (can_perform_actions),
    INDEX idx_email_verified (email_verified_at),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_last_login (last_login_at),
    INDEX idx_last_activity (last_activity_at),
    INDEX idx_password_changed (password_changed_at),
    INDEX idx_failed_attempts (failed_login_attempts),
    INDEX idx_locked_until (locked_until),
    INDEX idx_two_factor (two_factor_confirmed_at),

    -- Composite indexes for common queries
    INDEX idx_status_login (status, can_login),
    INDEX idx_status_actions (status, can_perform_actions),
    INDEX idx_active_users (status, email_verified_at) WHERE status = 'ACTIVE',
    INDEX idx_pending_verification (status, created_at) WHERE status = 'PENDING_VERIFICATION',
    INDEX idx_security_check (failed_login_attempts, locked_until),
    INDEX idx_activity_tracking (last_login_at, last_activity_at)
);

-- ========== USER DEVICES TABLE ==========
CREATE TABLE user_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_name VARCHAR(255) NULL, -- User-defined device name
    device_fingerprint VARCHAR(255) NOT NULL, -- Hash unik dari atribut perangkat
    device_type ENUM('desktop', 'mobile', 'tablet', 'other') NOT NULL DEFAULT 'other',
    os_name VARCHAR(100) NULL,
    os_version VARCHAR(50) NULL,
    browser_name VARCHAR(100) NULL,
    browser_version VARCHAR(50) NULL,
    user_agent TEXT NULL,
    ip_address VARCHAR(45) NULL,
    country VARCHAR(3) NULL,
    city VARCHAR(100) NULL,
    status ENUM('trusted', 'untrusted', 'blocked') NOT NULL DEFAULT 'untrusted',
    is_current_device BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP NOT NULL,
    first_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY idx_user_device_fingerprint (user_id, device_fingerprint),
    INDEX idx_user_devices (user_id),
    INDEX idx_device_status (status),
    INDEX idx_device_type (device_type),
    INDEX idx_last_login_device (last_login_at),
    INDEX idx_current_device (is_current_device),
    INDEX idx_device_location (country, city)
);

-- ========== TRIGGERS FOR DENORMALIZED FIELDS ==========
DELIMITER //

CREATE TRIGGER tr_users_sync_permissions
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    -- Update can_login based on status
    SET NEW.can_login = CASE NEW.status
        WHEN 'ACTIVE' THEN TRUE
        WHEN 'INCOMPLETE_PROFILE' THEN TRUE
        WHEN 'SUSPENDED' THEN TRUE
        ELSE FALSE
    END;

    -- Update can_perform_actions based on status
    SET NEW.can_perform_actions = CASE NEW.status
        WHEN 'ACTIVE' THEN TRUE
        WHEN 'INCOMPLETE_PROFILE' THEN TRUE
        ELSE FALSE
    END;

    -- Set display_name if not provided (fallback to name)
    IF NEW.display_name IS NULL OR NEW.display_name = '' THEN
        SET NEW.display_name = NEW.name;
    END IF;

    -- Always update updated_at
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER tr_users_sync_permissions_insert
    BEFORE INSERT ON users
    FOR EACH ROW
BEGIN
    -- Set can_login based on initial status
    SET NEW.can_login = CASE NEW.status
        WHEN 'ACTIVE' THEN TRUE
        WHEN 'INCOMPLETE_PROFILE' THEN TRUE
        WHEN 'SUSPENDED' THEN TRUE
        ELSE FALSE
    END;

    -- Set can_perform_actions based on initial status
    SET NEW.can_perform_actions = CASE NEW.status
        WHEN 'ACTIVE' THEN TRUE
        WHEN 'INCOMPLETE_PROFILE' THEN TRUE
        ELSE FALSE
    END;

    -- Generate referral code if not provided (optional)
    -- IF NEW.referral_code IS NULL THEN
    --     SET NEW.referral_code = CONCAT(
    --         UPPER(SUBSTRING(MD5(CONCAT(NEW.email, UNIX_TIMESTAMP())), 1, 8))
    --     );
    -- END IF;

    -- Set display_name if not provided (fallback to name)
    IF NEW.display_name IS NULL OR NEW.display_name = '' THEN
        SET NEW.display_name = NEW.name;
    END IF;

    -- Set timestamps if not provided
    IF NEW.created_at IS NULL THEN
        SET NEW.created_at = CURRENT_TIMESTAMP;
    END IF;

    IF NEW.updated_at IS NULL THEN
        SET NEW.updated_at = CURRENT_TIMESTAMP;
    END IF;
END//

-- Trigger for user devices to update is_current_device
CREATE TRIGGER tr_user_devices_update_current
    AFTER INSERT ON user_devices
    FOR EACH ROW
BEGIN
    -- Set all other devices for this user as not current
    UPDATE user_devices
    SET is_current_device = FALSE
    WHERE user_id = NEW.user_id AND id != NEW.id;

    -- Set the new device as current
    UPDATE user_devices
    SET is_current_device = TRUE
    WHERE id = NEW.id;
END//

DELIMITER ;

-- ========== ENHANCED VIEWS ==========

-- View for active users with security info
CREATE VIEW active_users_security AS
SELECT
    u.id, u.name, u.email, u.status,
    u.last_login_at, u.last_activity_at, u.login_count,
    u.two_factor_confirmed_at IS NOT NULL as has_2fa,
    u.failed_login_attempts,
    u.locked_until IS NOT NULL as is_locked,
    u.password_changed_at,
    u.created_at, u.updated_at
FROM users u
WHERE u.status IN ('ACTIVE', 'INCOMPLETE_PROFILE')
  AND u.can_perform_actions = TRUE;

-- View for user statistics
CREATE VIEW user_statistics AS
SELECT
    COUNT(*) as total_users,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users,
    COUNT(CASE WHEN status = 'PENDING_VERIFICATION' THEN 1 END) as pending_users,
    COUNT(CASE WHEN email_verified_at IS NOT NULL THEN 1 END) as verified_emails,
    COUNT(CASE WHEN two_factor_confirmed_at IS NOT NULL THEN 1 END) as two_factor_enabled,
    COUNT(CASE WHEN failed_login_attempts > 0 THEN 1 END) as users_with_failed_attempts,
    COUNT(CASE WHEN locked_until IS NOT NULL THEN 1 END) as locked_users,
    AVG(login_count) as avg_login_count
FROM users;

-- View for user devices summary
CREATE VIEW user_device_summary AS
SELECT
    u.id as user_id,
    u.name,
    u.email,
    COUNT(ud.id) as total_devices,
    COUNT(CASE WHEN ud.status = 'trusted' THEN 1 END) as trusted_devices,
    COUNT(CASE WHEN ud.status = 'untrusted' THEN 1 END) as untrusted_devices,
    COUNT(CASE WHEN ud.status = 'blocked' THEN 1 END) as blocked_devices,
    MAX(ud.last_login_at) as last_device_login
FROM users u
LEFT JOIN user_devices ud ON u.id = ud.user_id
WHERE u.deleted_at IS NULL
GROUP BY u.id, u.name, u.email;

-- View for users needing admin attention (security focused)
CREATE VIEW users_security_alerts AS
SELECT
    u.id, u.name, u.email, u.status,
    u.failed_login_attempts,
    u.locked_until,
    DATEDIFF(CURRENT_TIMESTAMP, u.created_at) as days_since_creation,
    DATEDIFF(CURRENT_TIMESTAMP, u.updated_at) as days_since_update,
    DATEDIFF(CURRENT_TIMESTAMP, u.last_login_at) as days_since_last_login,
    DATEDIFF(CURRENT_TIMESTAMP, u.password_changed_at) as days_since_password_change,
    u.two_factor_confirmed_at IS NULL as needs_2fa_setup,
    u.created_at, u.updated_at, u.last_login_at, u.password_changed_at
FROM users u
WHERE (
    u.status IN ('SUSPENDED', 'BANNED', 'LOCKED') OR
    u.failed_login_attempts >= 3 OR
    u.locked_until IS NOT NULL OR
    (u.status = 'PENDING_VERIFICATION' AND u.created_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)) OR
    (u.password_changed_at IS NULL OR u.password_changed_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 DAY))
  )
ORDER BY u.updated_at DESC;

-- ========== SAMPLE DATA ==========
INSERT INTO users (
    name, email, password, status
) VALUES
('John Doe', 'john@example.com', '$2y$10$...', 'ACTIVE'),
('Jane Smith', 'jane@example.com', '$2y$10$...', 'PENDING_VERIFICATION'),
('Bob Johnson', 'bob@example.com', '$2y$10$...', 'SUSPENDED'),
('Alice Brown', 'alice@example.com', '$2y$10$...', 'INCOMPLETE_PROFILE');

-- Update security information for sample users
UPDATE users
SET email_verified_at = CURRENT_TIMESTAMP,
    last_login_at = CURRENT_TIMESTAMP,
    last_activity_at = CURRENT_TIMESTAMP,
    login_count = 15,
    password_changed_at = DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY),
    two_factor_confirmed_at = CURRENT_TIMESTAMP
WHERE email = 'john@example.com';

-- Sample device data
INSERT INTO user_devices (
    user_id, device_fingerprint, device_name, device_type,
    os_name, browser_name, user_agent, ip_address, status, last_login_at
)
-- SELECT
--     u.id,
--     MD5(CONCAT(u.id, 'chrome-desktop')),
--     'John\'s Laptop',
--     'desktop',
--     'Windows 10',
--     'Chrome',
--     'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
--     '192.168.1.100',
--     'trusted',
--     CURRENT_TIMESTAMP
-- FROM users u WHERE u.email = 'john@example.com';

-- ========== SECURITY & ANALYTICS QUERIES ==========

-- Query for security metrics
SELECT
    COUNT(*) as total_users,
    COUNT(CASE WHEN two_factor_confirmed_at IS NOT NULL THEN 1 END) as users_with_2fa,
    COUNT(CASE WHEN failed_login_attempts > 0 THEN 1 END) as users_with_failed_attempts,
    COUNT(CASE WHEN locked_until IS NOT NULL AND locked_until > CURRENT_TIMESTAMP THEN 1 END) as currently_locked_users,
    AVG(failed_login_attempts) as avg_failed_attempts,
    COUNT(CASE WHEN password_changed_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 DAY) THEN 1 END) as users_with_old_passwords
FROM users
WHERE status IN ('ACTIVE', 'INCOMPLETE_PROFILE', 'SUSPENDED');

-- Query for login activity in last 30 days
SELECT
    DATE(last_login_at) as login_date,
    COUNT(DISTINCT id) as unique_users_logged_in,
    SUM(CASE WHEN last_login_at = DATE(CURRENT_TIMESTAMP) THEN 1 ELSE 0 END) as today_logins
FROM users
WHERE last_login_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY)
  AND status = 'ACTIVE'
GROUP BY DATE(last_login_at)
ORDER BY login_date DESC;

-- Query for users who need password reset (old passwords)
SELECT
    id, name, email,
    password_changed_at,
    DATEDIFF(CURRENT_TIMESTAMP, COALESCE(password_changed_at, created_at)) as days_since_password_change,
    two_factor_confirmed_at IS NOT NULL as has_2fa
FROM users
WHERE status = 'ACTIVE'
  AND (password_changed_at IS NULL OR password_changed_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 DAY))
ORDER BY password_changed_at ASC NULLS FIRST;

-- Query for suspicious activity (multiple failed attempts)
SELECT
    id, name, email,
    failed_login_attempts,
    locked_until,
    last_login_at,
    DATEDIFF(CURRENT_TIMESTAMP, last_login_at) as days_since_last_login
FROM users
WHERE failed_login_attempts >= 3
  OR (locked_until IS NOT NULL AND locked_until > CURRENT_TIMESTAMP)
ORDER BY failed_login_attempts DESC, locked_until DESC;

-- #####################################################################
-- # Bagian 1: Skema Aplikasi SaaS Anda
-- # Tabel-tabel ini mengelola logika bisnis spesifik produk Anda,
-- # seperti organisasi, tim, dan proyek.
-- #####################################################################

-- Tabel untuk mengelola tenant atau organisasi
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX (name)
);

-- Tabel untuk mengelola tim, divisi, departemen, dll. (mendukung hierarki)
CREATE TABLE teams (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    parent_id UUID NULL, -- Untuk hierarki (misal: departemen di dalam divisi)
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES teams(id) ON DELETE CASCADE, -- Self-referencing key
    INDEX (parent_id)
);

-- Tabel untuk mengelola proyek atau aplikasi yang dibuat oleh pengguna/tim
-- Ini adalah tabel penghubung utama ke fungsionalitas OAuth Passport
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    team_id UUID NULL,
    user_id UUID NULL, -- Untuk proyek individual
    oauth_client_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'active', -- Contoh: active, archived, on_hold
    description TEXT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (oauth_client_id) REFERENCES oauth_clients(id) ON DELETE CASCADE,
    INDEX (status)
);


-- #####################################################################
-- # Bagian 2: Skema Bawaan Laravel Passport
-- # Tabel-tabel ini dibuat dan dikelola oleh Laravel Passport
-- # untuk menyediakan fungsionalitas server OAuth2.
-- #####################################################################

-- Tabel untuk menyimpan klien OAuth (aplikasi yang meminta akses)
CREATE TABLE oauth_clients (
    id UUID PRIMARY KEY,
    user_id UUID NULL,
    name VARCHAR(255) NOT NULL,
    secret VARCHAR(100) NOT NULL,
    provider VARCHAR(255) NULL,
    redirect TEXT NOT NULL,
    personal_access_client BOOLEAN NOT NULL,
    password_client BOOLEAN NOT NULL,
    revoked BOOLEAN NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    INDEX (user_id)
);

-- Tabel untuk menyimpan access token yang telah diterbitkan
CREATE TABLE oauth_access_tokens (
    id VARCHAR(100) PRIMARY KEY,
    user_id UUID NULL,
    client_id UUID NOT NULL,
    name VARCHAR(255) NULL,
    scopes TEXT NULL,
    revoked BOOLEAN NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    expires_at DATETIME NULL,
    INDEX (user_id),
    INDEX (client_id)
);

-- Tabel untuk menyimpan refresh token
CREATE TABLE oauth_refresh_tokens (
    id VARCHAR(100) PRIMARY KEY,
    access_token_id VARCHAR(100) NOT NULL,
    revoked BOOLEAN NOT NULL,
    expires_at DATETIME NULL,
    INDEX (access_token_id)
);

-- Tabel untuk menyimpan kode otorisasi sementara (untuk authorization code grant)
CREATE TABLE oauth_auth_codes (
    id VARCHAR(100) PRIMARY KEY,
    user_id UUID NOT NULL,
    client_id UUID NOT NULL,
    scopes TEXT NULL,
    revoked BOOLEAN NOT NULL,
    expires_at DATETIME NULL,
    INDEX (user_id),
    INDEX (client_id)
);

-- Tabel lookup untuk menandai klien mana yang bisa menerbitkan personal access token
CREATE TABLE oauth_personal_access_clients (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    INDEX (client_id)
);

-- #####################################################################
-- # Bagian 3: Skema untuk OIDC dan Identity Federation
-- # Tabel-tabel ini ditambahkan untuk mendukung login dengan pihak ketiga
-- # (Google, GitHub, SAML) dan untuk menyediakan klaim OIDC.
-- #####################################################################

-- Tabel untuk Identity Federation (menghubungkan user lokal dengan provider eksternal)
CREATE TABLE user_identities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    provider_name VARCHAR(255) NOT NULL, -- contoh: 'google', 'github', 'saml_okta'
    provider_id VARCHAR(255) NOT NULL, -- ID unik user di sistem provider
    access_token TEXT NULL, -- Token dari provider untuk API calls
    refresh_token TEXT NULL, -- Refresh token dari provider
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (provider_name, provider_id) -- Setiap user hanya bisa terhubung sekali per provider
);

-- Tabel untuk menyimpan klaim OIDC (informasi yang akan dimasukkan ke dalam ID Token)
-- Ini adalah pendekatan fleksibel untuk menyimpan klaim kustom maupun standar
CREATE TABLE oidc_claims (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    claim_type VARCHAR(255) NOT NULL, -- contoh: 'profile', 'email', 'phone_number', 'custom:department'
    claim_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX(user_id, claim_type)
);

-- #####################################################################
-- # Bagian 4: Skema untuk RBAC (Sesuai dengan Spatie/laravel-permission)
-- # Struktur RBAC yang canggih, tenant-aware, dan mendukung fitur modern.
-- #####################################################################

-- Tabel untuk menyimpan semua izin (permissions) yang ada di sistem
CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    guard_name VARCHAR(255) NOT NULL, -- Kompatibilitas dengan Spatie
    description TEXT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    UNIQUE (name, guard_name)
);

-- Tabel untuk menyimpan peran (roles) yang spesifik untuk setiap organisasi (tenant)
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    organization_id UUID NULL, -- NULL untuk peran level sistem (super-admin)
    name VARCHAR(255) NOT NULL,
    guard_name VARCHAR(255) NOT NULL, -- Kompatibilitas dengan Spatie
    description TEXT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    UNIQUE (name, guard_name, organization_id) -- Nama peran harus unik per guard per organisasi
);

-- Tabel pivot untuk menghubungkan peran dengan izin (many-to-many)
CREATE TABLE role_has_permissions (
    permission_id UUID NOT NULL,
    role_id UUID NOT NULL,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (permission_id, role_id)
);

-- Tabel pivot untuk menugaskan peran ke pengguna (mendukung periodical assignment)
CREATE TABLE model_has_roles (
    role_id UUID NOT NULL,
    model_type VARCHAR(255) NOT NULL, -- Selalu 'App\Models\User'
    model_id UUID NOT NULL, -- ID dari user
    organization_id UUID NOT NULL, -- Konteks tenant di mana peran ini berlaku
    starts_at TIMESTAMP NULL, -- Kapan peran mulai aktif (untuk periodical)
    expires_at TIMESTAMP NULL, -- Kapan peran berakhir (untuk periodical)
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, model_id, model_type, organization_id),
    INDEX (model_id, model_type)
);

-- Tabel pivot untuk memberikan izin langsung ke pengguna (direct permissions)
CREATE TABLE model_has_permissions (
    permission_id UUID NOT NULL,
    model_type VARCHAR(255) NOT NULL,
    model_id UUID NOT NULL,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY(permission_id, model_id, model_type),
    INDEX (model_id, model_type)
);

-- Tabel log untuk melacak aktivitas impersonasi
CREATE TABLE impersonation_logs (
    id UUID PRIMARY KEY,
    impersonator_id UUID NOT NULL, -- Admin yang melakukan impersonasi
    impersonated_id UUID NOT NULL, -- User yang di-impersonasi
    organization_id UUID NOT NULL, -- Konteks tenant saat impersonasi
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NULL, -- Diisi saat sesi impersonasi berakhir
    reason TEXT NULL, -- Alasan melakukan impersonasi
    FOREIGN KEY (impersonator_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (impersonated_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- #####################################################################
-- # Bagian 5: Skema untuk Otorisasi Lanjutan (Advanced Authorization)
-- #####################################################################

-- Tabel untuk mendefinisikan alur kerja persetujuan (multi-level)
CREATE TABLE approval_workflows (
    id UUID PRIMARY KEY,
    organization_id UUID NULL, -- NULL untuk workflow sistem
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Tabel untuk mendefinisikan langkah-langkah dalam sebuah alur kerja
CREATE TABLE approval_workflow_steps (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    step_level INT NOT NULL, -- Urutan langkah (1, 2, 3, ...)
    approver_role_id UUID NOT NULL, -- Peran yang dibutuhkan untuk menyetujui langkah ini
    name VARCHAR(255) NOT NULL,
    is_final_step BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (workflow_id) REFERENCES approval_workflows(id) ON DELETE CASCADE,
    FOREIGN KEY (approver_role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE (workflow_id, step_level)
);

-- Tabel generik untuk semua jenis permintaan persetujuan (polimorfik)
CREATE TABLE approval_requests (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    current_step_id UUID NULL, -- Langkah saat ini yang menunggu persetujuan
    requester_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    requestable_type VARCHAR(255) NOT NULL, -- Tipe model dari payload permintaan
    requestable_id UUID NOT NULL, -- ID dari payload permintaan
    status ENUM('pending', 'approved', 'rejected', 'cancelled') NOT NULL DEFAULT 'pending',
    reason TEXT NULL,
    FOREIGN KEY (workflow_id) REFERENCES approval_workflows(id) ON DELETE CASCADE,
    FOREIGN KEY (current_step_id) REFERENCES approval_workflow_steps(id) ON DELETE SET NULL,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX (status),
    INDEX (requestable_type, requestable_id)
);

-- Tabel "payload" untuk permintaan penugasan peran
CREATE TABLE role_assignment_payloads (
    id UUID PRIMARY KEY, -- ID ini akan menjadi 'requestable_id' di approval_requests
    target_user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    expires_at TIMESTAMP NULL,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabel "payload" untuk permintaan perubahan kebijakan impersonasi
CREATE TABLE impersonation_policy_change_payloads (
    id UUID PRIMARY KEY, -- ID ini akan menjadi 'requestable_id' di approval_requests
    policy_id UUID NULL, -- NULL jika ini adalah kebijakan baru
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    impersonator_conditions JSON NOT NULL,
    target_conditions JSON NOT NULL,
    contextual_conditions JSON NULL,
    effect ENUM('allow', 'deny') NOT NULL,
    priority INT NOT NULL,
    is_active BOOLEAN NOT NULL,
    FOREIGN KEY (policy_id) REFERENCES impersonation_policies(id) ON DELETE CASCADE
);


-- Tabel untuk ACL (Access Control List) pada sumber daya spesifik
CREATE TABLE resource_acls (
    id UUID PRIMARY KEY,
    permission_id UUID NOT NULL,
    actor_type VARCHAR(255) NOT NULL, -- 'App\Models\User' atau 'App\Models\Team'
    actor_id UUID NOT NULL, -- ID dari user atau team
    resource_type VARCHAR(255) NOT NULL, -- 'App\Models\Project' atau 'App\Models\Document'
    resource_id UUID NOT NULL, -- ID dari proyek atau dokumen spesifik
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    INDEX (actor_type, actor_id),
    INDEX (resource_type, resource_id)
);

-- Tabel untuk ABAC (Attribute-Based Access Control)
CREATE TABLE access_policies (
    id UUID PRIMARY KEY,
    organization_id UUID NULL, -- Kebijakan bisa spesifik per tenant
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    effect ENUM('allow', 'deny') NOT NULL,
    action VARCHAR(255) NOT NULL, -- Izin yang terpengaruh, contoh: 'projects.view'
    conditions JSON NOT NULL, -- Aturan dalam format JSON
    priority INT NOT NULL DEFAULT 0, -- Prioritas jika ada konflik kebijakan
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Tabel untuk Aturan Impersonasi (Impersonation Rules)
CREATE TABLE impersonation_policies (
    id UUID PRIMARY KEY,
    organization_id UUID NULL, -- Aturan bisa spesifik per tenant
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    impersonator_conditions JSON NOT NULL, -- Aturan untuk admin/pelaku
    target_conditions JSON NOT NULL, -- Aturan untuk user yang akan diimpersonasi
    contextual_conditions JSON NULL, -- Aturan berbasis konteks (waktu, IP, dll)
    effect ENUM('allow', 'deny') NOT NULL DEFAULT 'allow',
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);


-- #####################################################################
-- # Bagian 6: Skema untuk Riwayat Proses Bisnis (Business Process History)
-- #####################################################################

-- Tabel riwayat yang generik untuk semua jenis permintaan persetujuan
CREATE TABLE approval_request_histories (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    step_id UUID NULL, -- Langkah workflow yang terkait dengan riwayat ini
    actor_id UUID NOT NULL, -- Pengguna yang menyebabkan perubahan (requester/approver)
    status_from VARCHAR(50) NULL, -- Status sebelumnya (bisa NULL untuk pembuatan awal)
    status_to VARCHAR(50) NOT NULL, -- Status baru
    comment TEXT NULL, -- Komentar atau catatan dari aktor
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (request_id) REFERENCES approval_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (step_id) REFERENCES approval_workflow_steps(id) ON DELETE SET NULL,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabel untuk melacak riwayat keanggotaan pengguna dalam tim
CREATE TABLE team_membership_histories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    team_id UUID NOT NULL,
    actor_id UUID NOT NULL, -- Pengguna yang menambahkan/menghapus
    action ENUM('joined', 'left', 'invited') NOT NULL,
    comment TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabel untuk melacak riwayat perubahan status pengguna
CREATE TABLE user_status_histories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    actor_id UUID NOT NULL, -- Admin yang mengubah status
    status_from VARCHAR(50) NOT NULL,
    status_to VARCHAR(50) NOT NULL,
    reason TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- #####################################################################
-- # Bagian 7: Skema untuk Konfigurasi Sistem (Dynamic & Typed)
-- #####################################################################

-- Tabel untuk mengelompokkan parameter agar mudah dikelola
CREATE TABLE parameter_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

-- Tabel untuk parameter sistem global dengan tipe data dan validasi
CREATE TABLE system_parameters (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    parameter_key VARCHAR(255) NOT NULL UNIQUE,
    parameter_value TEXT NOT NULL,
    type ENUM('string', 'integer', 'boolean', 'json', 'text') NOT NULL DEFAULT 'string',
    description TEXT NULL,
    validation_rules TEXT NULL, -- Menyimpan aturan validasi, contoh: 'required|min:5'
    is_tenant_overridable BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (group_id) REFERENCES parameter_groups(id) ON DELETE CASCADE
);

-- Tabel untuk parameter yang spesifik per tenant (menimpa nilai global)
CREATE TABLE organization_parameters (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    parameter_id UUID NOT NULL, -- Merujuk ke definisi parameter di system_parameters
    parameter_value TEXT NOT NULL, -- Nilai yang ditimpa
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (parameter_id) REFERENCES system_parameters(id) ON DELETE CASCADE,
    UNIQUE (organization_id, parameter_id)
);

-- Tabel untuk melacak riwayat perubahan pada parameter penting
CREATE TABLE parameter_histories (
    id UUID PRIMARY KEY,
    parameter_id UUID NOT NULL,
    organization_id UUID NULL, -- NULL jika perubahan pada level sistem
    actor_id UUID NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    FOREIGN KEY (parameter_id) REFERENCES system_parameters(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- #####################################################################
-- # Bagian 8: Skema untuk Onboarding & Lifecycle Pengguna
-- #####################################################################

-- Tabel untuk mendefinisikan langkah-langkah dalam sebuah proses onboarding
CREATE TABLE onboarding_steps (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    step_order INT NOT NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    UNIQUE (step_order)
);

-- Tabel pivot untuk melacak kemajuan onboarding setiap pengguna
CREATE TABLE user_onboarding_progress (
    user_id UUID NOT NULL,
    step_id UUID NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (step_id) REFERENCES onboarding_steps(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, step_id)
);

-- Tabel "payload" untuk permintaan sinkronisasi data dari identity provider
CREATE TABLE identity_data_update_payloads (
    id UUID PRIMARY KEY, -- ID ini akan menjadi 'requestable_id' di approval_requests
    user_id UUID NOT NULL,
    provider_name VARCHAR(255) NOT NULL,
    proposed_changes JSON NOT NULL, -- Menyimpan data lama dan baru, contoh: {"email": {"from": "a", "to": "b"}}
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- #####################################################################
-- # Bagian 9: Skema untuk Profil, Alamat, & Data Regional
-- #####################################################################

-- Tabel untuk menyimpan data profil pengguna yang terpisah dari data otentikasi
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE, -- Relasi one-to-one dengan tabel users
    first_name VARCHAR(255) NULL,
    last_name VARCHAR(255) NULL,
    date_of_birth DATE NULL,
    gender ENUM('male', 'female', 'other', 'prefer_not_to_say') NULL,
    nationality_country_id INT UNSIGNED NULL,
    phone_number VARCHAR(50) NULL,
    job_title VARCHAR(255) NULL,
    bio TEXT NULL,
    profile_picture_url VARCHAR(255) NULL,
    social_links JSON NULL, -- contoh: {"linkedin": "url", "twitter": "url"}
    timezone VARCHAR(100) NULL,
    locale VARCHAR(10) NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (nationality_country_id) REFERENCES countries(id) ON DELETE SET NULL
);

-- Tabel master untuk negara
CREATE TABLE countries (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    iso_code_2 CHAR(2) NOT NULL UNIQUE,
    iso_code_3 CHAR(3) NOT NULL UNIQUE
);

-- Tabel master untuk provinsi/negara bagian
CREATE TABLE provinces (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    country_id INT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE
);

-- Tabel master untuk kota/kabupaten
CREATE TABLE cities (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    province_id INT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    FOREIGN KEY (province_id) REFERENCES provinces(id) ON DELETE CASCADE
);

-- Tabel master untuk kecamatan
CREATE TABLE districts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    city_id INT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE CASCADE
);

-- Tabel master untuk desa/kelurahan
CREATE TABLE villages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    district_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20) NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE,
    INDEX (postal_code)
);

-- Tabel polimorfik untuk menyimpan alamat (bisa untuk user, organisasi, dll)
CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    addressable_type VARCHAR(255) NOT NULL, -- Contoh: 'App\Models\User', 'App\Models\Organization'
    addressable_id UUID NOT NULL,
    label VARCHAR(255) NOT NULL, -- Contoh: 'Home', 'Office', 'Billing'
    street_address_1 VARCHAR(255) NOT NULL,
    street_address_2 VARCHAR(255) NULL,
    village_id BIGINT UNSIGNED NULL,
    district_id BIGINT UNSIGNED NULL,
    city_id INT UNSIGNED NULL,
    province_id INT UNSIGNED NULL,
    country_id INT UNSIGNED NOT NULL,
    postal_code VARCHAR(20) NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (village_id) REFERENCES villages(id) ON DELETE SET NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE SET NULL,
    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE SET NULL,
    FOREIGN KEY (province_id) REFERENCES provinces(id) ON DELETE SET NULL,
    FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE,
    INDEX (addressable_type, addressable_id)
);

-- #####################################################################
-- # Bagian 10: Skema untuk Provisioning (SCIM & Webhooks)
-- #####################################################################

-- Tabel untuk mendefinisikan sistem target untuk provisioning
CREATE TABLE provisioning_targets (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL, -- Contoh: 'Jira Cloud', 'Slack Workspace'
    driver VARCHAR(255) NOT NULL, -- Contoh: 'scim_v2', 'slack_api', 'custom_webhook'
    api_endpoint VARCHAR(255) NOT NULL,
    credentials JSON NOT NULL, -- Menyimpan token, dll. secara terenkripsi
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Tabel antrian untuk event provisioning
CREATE TABLE provisioning_events (
    id UUID PRIMARY KEY,
    target_id UUID NOT NULL,
    provisionable_type VARCHAR(255) NOT NULL, -- Contoh: 'App\Models\User', 'App\Models\Team'
    provisionable_id UUID NOT NULL,
    action ENUM('create', 'update', 'delete') NOT NULL,
    payload JSON NOT NULL, -- Data yang akan dikirim
    status ENUM('pending', 'processing', 'success', 'failed') NOT NULL DEFAULT 'pending',
    attempts INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP NULL,
    next_attempt_at TIMESTAMP NULL, -- Untuk mekanisme retry
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (target_id) REFERENCES provisioning_targets(id) ON DELETE CASCADE,
    INDEX (status),
    INDEX (provisionable_type, provisionable_id)
);

-- Tabel riwayat untuk setiap upaya provisioning
CREATE TABLE provisioning_event_histories (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    status ENUM('success', 'failed') NOT NULL,
    response_code INT NULL,
    response_body TEXT NULL,
    attempted_at TIMESTAMP NOT NULL,
    FOREIGN KEY (event_id) REFERENCES provisioning_events(id) ON DELETE CASCADE
);

-- #####################################################################
-- # Bagian 11: Skema untuk Manajemen Perangkat & MFA
-- #####################################################################

-- Tabel untuk melacak perangkat yang digunakan pengguna untuk login
CREATE TABLE user_devices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_fingerprint VARCHAR(255) NOT NULL, -- Hash unik dari atribut perangkat
    user_agent TEXT NULL,
    ip_address VARCHAR(45) NULL,
    status ENUM('trusted', 'untrusted') NOT NULL DEFAULT 'untrusted',
    last_login_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user_id, device_fingerprint)
);

-- Tabel untuk menyimpan metode MFA yang telah dikonfigurasi oleh pengguna
CREATE TABLE mfa_methods (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type ENUM('totp', 'sms', 'email', 'fido2', 'backup_code') NOT NULL,
    name VARCHAR(255) NOT NULL, -- Nama yang diberikan pengguna, contoh: 'My iPhone Authenticator'
    credentials JSON NOT NULL, -- Menyimpan secret (terenkripsi), nomor telepon, atau credential FIDO2
    is_primary BOOLEAN NOT NULL DEFAULT false,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    verified_at TIMESTAMP NULL, -- Kapan metode ini berhasil diverifikasi
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
-- Tabel untuk menyimpan tantangan MFA sementara selama proses login
CREATE TABLE mfa_challenges (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    method_id UUID NOT NULL,
    challenge_token VARCHAR(255) NOT NULL UNIQUE, -- Token sekali pakai untuk verifikasi
    status ENUM('pending', 'verified', 'expired') NOT NULL DEFAULT 'pending',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (method_id) REFERENCES mfa_methods(id) ON DELETE CASCADE,
    INDEX (status)
);

-- #####################################################################
-- # Bagian 12: Skema untuk Pengaturan Umum (General Setup)
-- # Struktur dinamis untuk mengelola kebijakan bisnis fundamental
-- # yang dapat dikonfigurasi per tenant, lengkap dengan riwayat.
-- #####################################################################

-- Tabel untuk mengelompokkan pengaturan agar mudah dikelola
CREATE TABLE setup_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

-- Tabel untuk mendefinisikan semua pengaturan umum yang tersedia di sistem
CREATE TABLE setup_definitions (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    `key` VARCHAR(255) NOT NULL UNIQUE, -- contoh: 'mfa.policy', 'password.complexity_rules'
    type ENUM('string', 'integer', 'boolean', 'json', 'text') NOT NULL,
    default_value TEXT NOT NULL,
    description TEXT NULL,
    validation_rules TEXT NULL, -- Menyimpan aturan validasi, contoh: 'required|in:optional,mandatory'
    is_tenant_configurable BOOLEAN NOT NULL DEFAULT true, -- Apakah tenant bisa mengubah ini?
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (group_id) REFERENCES setup_groups(id) ON DELETE CASCADE
);

-- Tabel untuk menyimpan nilai pengaturan yang spesifik untuk setiap tenant
CREATE TABLE organization_setups (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    setup_definition_id UUID NOT NULL,
    value TEXT NOT NULL, -- Nilai yang ditimpa oleh tenant
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (setup_definition_id) REFERENCES setup_definitions(id) ON DELETE CASCADE,
    UNIQUE (organization_id, setup_definition_id)
);

-- Tabel untuk melacak riwayat perubahan pada pengaturan umum
CREATE TABLE setup_histories (
    id UUID PRIMARY KEY,
    setup_definition_id UUID NOT NULL,
    organization_id UUID NULL, -- NULL jika perubahan pada level global/default
    actor_id UUID NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    FOREIGN KEY (setup_definition_id) REFERENCES setup_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- #####################################################################
-- # Bagian 13: Skema untuk Siklus Hidup Akun (Password Recovery, dll.)
-- #####################################################################

-- Tabel untuk menyimpan token pemulihan kata sandi yang bersifat sementara
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    status ENUM('pending', 'completed', 'expired') NOT NULL DEFAULT 'pending',
    expires_at TIMESTAMP NOT NULL,
    ip_address_request VARCHAR(45) NULL,
    user_agent_request TEXT NULL,
    ip_address_completion VARCHAR(45) NULL,
    user_agent_completion TEXT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX (status)
);

-- JOB Enums
-- ENUM untuk tipe job
CREATE TYPE job_type_enum AS ENUM (
    'MANUAL',         -- dijalankan secara manual (misal dari UI)
    'SCHEDULED',      -- dijalankan oleh scheduler
    'SYSTEM',         -- dijalankan oleh sistem internal
    'EVENT'           -- dijalankan karena event tertentu
);

-- ENUM untuk status eksekusi job
CREATE TYPE job_status_enum AS ENUM (
    'STARTING',
    'STARTED',
    'FAILED',
    'COMPLETED',
    'STOPPED'
);

-- ENUM untuk status step
CREATE TYPE step_status_enum AS ENUM (
    'STARTING',
    'STARTED',
    'FAILED',
    'COMPLETED',
    'SKIPPED'
);

-- ENUM untuk log level
CREATE TYPE log_level_enum AS ENUM (
    'TRACE',
    'DEBUG',
    'INFO',
    'WARN',
    'ERROR'
);

-- ENUM untuk parameter type (opsional tapi berguna)
CREATE TYPE param_type_enum AS ENUM (
    'STRING',
    'LONG',
    'DOUBLE',
    'DATE',
    'JSON'
);
-- JOB Table
CREATE TABLE jobs (
           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
           name VARCHAR(255) NOT NULL,
           unique_key VARCHAR(255) NOT NULL,
           description TEXT NULL,
           job_type job_type_enum DEFAULT 'MANUAL',
           is_active BOOLEAN NOT NULL DEFAULT true,

           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

           CONSTRAINT uq_jobs_name_key UNIQUE (name, unique_key)
       );

CREATE TABLE job_parameters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    param_key VARCHAR(255) NOT NULL,
    param_value TEXT NOT NULL,
    param_type param_type_enum NOT NULL DEFAULT 'STRING',
    is_required BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_job_param_key UNIQUE (job_id, param_key)
);
CREATE TABLE job_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    parent_execution_id UUID NULL REFERENCES job_executions(id) ON DELETE SET NULL,
    run_id VARCHAR(100) NOT NULL,
    status job_status_enum NOT NULL,
    exit_code VARCHAR(100) NULL,
    exit_message TEXT NULL,
    triggered_by VARCHAR(100) NULL,
    payload JSONB NULL,
    retry_count INT DEFAULT 0,
    host VARCHAR(100) NULL,

    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_job_run_id UNIQUE (job_id, run_id)
);
CREATE TABLE job_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id UUID NOT NULL REFERENCES job_executions(id) ON DELETE CASCADE,
    step_name VARCHAR(255) NOT NULL,
    status step_status_enum NOT NULL,
    read_count INT DEFAULT 0,
    write_count INT DEFAULT 0,
    skip_count INT DEFAULT 0,
    error_message TEXT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    retry_count INT DEFAULT 0,
    duration_ms BIGINT GENERATED ALWAYS AS (EXTRACT(EPOCH FROM (ended_at - started_at)) * 1000)::BIGINT STORED,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE job_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id UUID NOT NULL REFERENCES job_executions(id) ON DELETE CASCADE,
    step_id UUID NULL REFERENCES job_steps(id) ON DELETE SET NULL,
    level log_level_enum NOT NULL DEFAULT 'INFO',
    message TEXT NOT NULL,
    context JSONB NULL,
    logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
