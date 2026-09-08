CREATE SEQUENCE click_seq START 1 INCREMENT 50;

CREATE TABLE click_events
(
    id             BIGINT PRIMARY KEY DEFAULT nextval('click_seq'),
    short_code     VARCHAR(8)               NOT NULL,
    original_url   VARCHAR                  NOT NULL,
    clicked_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    user_agent     VARCHAR(512),
    correlation_id VARCHAR                  NOT NULL

);