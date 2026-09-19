ALTER TABLE click_events DROP CONSTRAINT uk_correlation_id;
ALTER TABLE click_events DROP COLUMN correlation_id;
ALTER TABLE click_events ADD COLUMN event_id VARCHAR(255);
ALTER TABLE click_events ADD CONSTRAINT uk_event_id UNIQUE (event_id);