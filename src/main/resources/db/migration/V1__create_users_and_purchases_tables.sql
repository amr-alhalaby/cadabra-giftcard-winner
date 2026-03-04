CREATE TABLE users (
    id          BIGINT       PRIMARY KEY,
    name        VARCHAR(255),
    username    VARCHAR(255),
    email       VARCHAR(255),
    phone       VARCHAR(255),
    website     VARCHAR(255),

    -- Address (flattened)
    street      VARCHAR(255),
    suite       VARCHAR(255),
    city        VARCHAR(255),
    zipcode     VARCHAR(255),
    lat         VARCHAR(255),
    lng         VARCHAR(255),

    -- Company (flattened)
    company_name         VARCHAR(255),
    company_catch_phrase VARCHAR(255),
    company_bs           VARCHAR(255)
);

CREATE TABLE purchases (
    id      BIGSERIAL      PRIMARY KEY,
    user_id BIGINT         NOT NULL,
    amount  DECIMAL(19, 2) NOT NULL
);

