-- First drop tables with foreign key dependencies
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS edit_proposal CASCADE;

-- Then drop the tables they depend on
DROP TABLE IF EXISTS restroom CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create user table
CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    token VARCHAR(255),
    refresh_token VARCHAR(255)
);

-- Create restroom table
CREATE TABLE restroom (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    hours VARCHAR(255),
    amenities TEXT[],
    avg_rating DOUBLE PRECISION DEFAULT 0,
    visit_count BIGINT DEFAULT 0
);

-- Create review table
CREATE TABLE review (
    id SERIAL PRIMARY KEY,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    cleanliness INT NOT NULL CHECK (cleanliness >= 1 AND cleanliness <= 5),
    comment TEXT,
    helpful_votes INT DEFAULT 0,
    user_id VARCHAR(255) NOT NULL,
    restroom_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(username),
    FOREIGN KEY (restroom_id) REFERENCES restroom(id)
);

-- Edit proposals table
CREATE TABLE edit_proposal (
    id BIGINT PRIMARY KEY,
    restroom_id BIGINT NOT NULL REFERENCES restroom(id),
    proposed_name VARCHAR(255),
    proposed_address VARCHAR(255),
    proposed_hours VARCHAR(255),
    proposed_amenities TEXT,
    proposer_user_id VARCHAR(255) NOT NULL REFERENCES users(username),
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_restroom FOREIGN KEY (restroom_id) REFERENCES restroom(id),
    CONSTRAINT fk_proposer FOREIGN KEY (proposer_user_id) REFERENCES users(username)
);