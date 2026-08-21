-- V1 identity, groups, expenses, activity (source of truth is expenses, not a balances table)

CREATE TABLE users (
    id UUID PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    username VARCHAR(40) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE people (
    id UUID PRIMARY KEY,
    phone VARCHAR(20) UNIQUE,
    user_id UUID UNIQUE REFERENCES users (id),
    display_name VARCHAR(120) NOT NULL,
    created_by_user_id UUID REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE invites (
    id UUID PRIMARY KEY,
    person_id UUID NOT NULL REFERENCES people (id),
    token VARCHAR(64) NOT NULL UNIQUE,
    created_by_user_id UUID NOT NULL REFERENCES users (id),
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ
);

CREATE TABLE groups (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(32) NOT NULL,
    owner_user_id UUID NOT NULL REFERENCES users (id),
    approval_mode VARCHAR(32) NOT NULL DEFAULT 'OFF',
    approval_threshold NUMERIC(19, 2),
    allow_cross_settlement BOOLEAN NOT NULL DEFAULT FALSE,
    deletion_policy VARCHAR(32) NOT NULL DEFAULT 'CREATOR_DIRECT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE group_members (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES groups (id),
    person_id UUID NOT NULL REFERENCES people (id),
    role VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    left_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (group_id, person_id)
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    group_id UUID REFERENCES groups (id),
    created_by_user_id UUID NOT NULL REFERENCES users (id),
    description VARCHAR(240) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    category VARCHAR(32),
    split_method VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    approval_status VARCHAR(32) NOT NULL,
    expense_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE expense_payers (
    expense_id UUID NOT NULL REFERENCES expenses (id),
    person_id UUID NOT NULL REFERENCES people (id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    PRIMARY KEY (expense_id, person_id)
);

CREATE TABLE expense_participants (
    expense_id UUID NOT NULL REFERENCES expenses (id),
    person_id UUID NOT NULL REFERENCES people (id),
    share_amount NUMERIC(19, 2) NOT NULL,
    share_count INTEGER,
    percentage NUMERIC(9, 4),
    PRIMARY KEY (expense_id, person_id)
);

CREATE TABLE activity_events (
    id UUID PRIMARY KEY,
    actor_person_id UUID REFERENCES people (id),
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_id UUID NOT NULL,
    audience_scope VARCHAR(32) NOT NULL,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE activity_audience (
    event_id UUID NOT NULL REFERENCES activity_events (id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES people (id),
    PRIMARY KEY (event_id, person_id)
);

CREATE INDEX idx_group_members_group_status ON group_members (group_id, status);
CREATE INDEX idx_expenses_group ON expenses (group_id);
CREATE INDEX idx_expense_payers_person ON expense_payers (person_id);
CREATE INDEX idx_expense_participants_person ON expense_participants (person_id);
CREATE INDEX idx_activity_entity ON activity_events (entity_type, entity_id, created_at);
CREATE INDEX idx_people_phone ON people (phone);
