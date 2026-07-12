-- Clinic vertical: therapists, availability slots, appointments.
-- Idempotent: safe to run on an existing database.
CREATE TABLE IF NOT EXISTS therapists (
    id                       BIGSERIAL PRIMARY KEY,
    name                     VARCHAR(150) NOT NULL,
    slug                     VARCHAR(180) NOT NULL UNIQUE,
    specialty                VARCHAR(150),
    bio                      TEXT,
    photo_url                VARCHAR(500),
    session_price            NUMERIC(12,2) NOT NULL DEFAULT 0,
    session_duration_minutes INT NOT NULL DEFAULT 45,
    product_id               BIGINT,
    is_active                BOOLEAN NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS availability_slots (
    id           BIGSERIAL PRIMARY KEY,
    therapist_id BIGINT NOT NULL REFERENCES therapists (id) ON DELETE CASCADE,
    start_time   TIMESTAMPTZ NOT NULL,
    end_time     TIMESTAMPTZ NOT NULL,
    is_booked    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS appointments (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    therapist_id   BIGINT NOT NULL REFERENCES therapists (id) ON DELETE CASCADE,
    slot_id        BIGINT NOT NULL REFERENCES availability_slots (id) ON DELETE CASCADE,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    video_room_url VARCHAR(500),
    notes          TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_slots_therapist_start ON availability_slots (therapist_id, start_time);
CREATE INDEX IF NOT EXISTS idx_appointments_user ON appointments (user_id);
