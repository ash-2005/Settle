ALTER TABLE activity_events
    ALTER COLUMN payload TYPE TEXT USING payload::text;
