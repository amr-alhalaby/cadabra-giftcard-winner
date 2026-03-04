ALTER TABLE purchases
    ADD CONSTRAINT fk_purchases_user
    FOREIGN KEY (user_id) REFERENCES users(id);

