CREATE TABLE outbox_events
(
    id             UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    aggregate_id   VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        JSONB        NOT NULL,
    status         VARCHAR(50)  NOT NULL    DEFAULT 'PENDING',
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sent_at        TIMESTAMP WITH TIME ZONE
);