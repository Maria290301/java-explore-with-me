CREATE TABLE IF NOT EXISTS endpoint_stats (
    id SERIAL PRIMARY KEY,
    app VARCHAR(255),
    uri VARCHAR(255),
    ip VARCHAR(255),
    date TIMESTAMP NOT NULL
);

