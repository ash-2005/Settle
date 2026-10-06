-- Friends can settle directly, outside any group.
ALTER TABLE settlement_payments ALTER COLUMN group_id DROP NOT NULL;
