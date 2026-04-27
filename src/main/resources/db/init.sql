DROP TABLE IF EXISTS otp_codes;
DROP TABLE IF EXISTS otp_config;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE otp_config (
                            id BIGINT PRIMARY KEY DEFAULT 1,
                            code_length INT NOT NULL,
                            ttl_seconds INT NOT NULL,
                            CONSTRAINT only_one_config CHECK (id = 1)
);

CREATE TABLE otp_codes (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           operation_id VARCHAR(100) NOT NULL,
                           code VARCHAR(20) NOT NULL,
                           status VARCHAR(20) NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           expires_at TIMESTAMP NOT NULL
);

INSERT INTO otp_config (id, code_length, ttl_seconds)
VALUES (1, 6, 300);