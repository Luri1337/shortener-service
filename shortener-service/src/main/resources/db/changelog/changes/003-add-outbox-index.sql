CREATE INDEX idx_outbox_events_status_created_at
    ON outbox_events (status, created_at);