CREATE TABLE validation_errors (
    id               BIGSERIAL    PRIMARY KEY,
    job_execution_id BIGINT       NOT NULL,
    step_name        VARCHAR(255) NOT NULL,
    raw_data         VARCHAR(1000),
    error_message    VARCHAR(1000),
    created_at       TIMESTAMP    NOT NULL
);

