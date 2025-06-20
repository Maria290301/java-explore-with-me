-- Удаление таблиц
DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS compilation_events CASCADE;
DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS categories CASCADE;

-- USERS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(1000) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- CATEGORIES
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- EVENTS
CREATE TABLE events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255),
    annotation TEXT NOT NULL,
    description TEXT,
    event_date TIMESTAMP,
    state VARCHAR(50),
    initiator_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_on TIMESTAMP,
    published_on TIMESTAMP,
    participant_limit INTEGER,
    paid BOOLEAN,
    request_moderation BOOLEAN,
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    CONSTRAINT fk_event_initiator FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- REQUESTS
CREATE TABLE requests (
    id BIGSERIAL PRIMARY KEY,
    created TIMESTAMP,
    status VARCHAR(50),
    event_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    CONSTRAINT fk_request_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_request_user FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE
);

-- COMPILATIONS
CREATE TABLE compilations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE
);

-- COMPILATION-EVENTS (many-to-many)
CREATE TABLE compilation_events (
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_compilation FOREIGN KEY (compilation_id) REFERENCES compilations(id) ON DELETE CASCADE,
    CONSTRAINT fk_compilation_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);