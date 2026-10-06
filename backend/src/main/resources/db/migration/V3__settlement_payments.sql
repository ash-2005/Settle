-- A payment is evidence that someone paid. It only changes balances once the recipient confirms.
CREATE TABLE settlement_payments (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES groups (id),
    from_person_id UUID NOT NULL REFERENCES people (id),
    to_person_id UUID NOT NULL REFERENCES people (id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ,
    CHECK (from_person_id <> to_person_id)
);

CREATE INDEX idx_settlement_payments_group ON settlement_payments (group_id, status);
CREATE INDEX idx_settlement_payments_from ON settlement_payments (from_person_id);
CREATE INDEX idx_settlement_payments_to ON settlement_payments (to_person_id);
