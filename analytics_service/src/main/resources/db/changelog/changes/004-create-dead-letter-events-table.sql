CREATE TABLE dead_letter_events
(
    id            UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    payload       TEXT NOT NULL,
    error_message TEXT NOT NULL,
    received_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);