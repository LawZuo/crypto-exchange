ALTER TABLE `account_wallet`
    ADD UNIQUE KEY `uk_account_wallet_user_currency_type` (`user_id`, `currency`, `wallet_type`);
