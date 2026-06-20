CREATE UNIQUE INDEX IF NOT EXISTS ux_addresses_user_default
    ON addresses(user_id)
    WHERE is_default = TRUE;