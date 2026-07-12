-- Phase K: appointment-booking refinements.
-- Therapist session mode + location, and confidential patient notes.
-- Idempotent: safe to run on an existing database.

ALTER TABLE therapists ADD COLUMN IF NOT EXISTS mode     VARCHAR(20) NOT NULL DEFAULT 'ONLINE';
ALTER TABLE therapists ADD COLUMN IF NOT EXISTS location VARCHAR(300);

CREATE TABLE IF NOT EXISTS patient_notes (
    id             BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointments (id) ON DELETE CASCADE,
    counselor_id   BIGINT NOT NULL,
    note           TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_patient_notes_appointment ON patient_notes (appointment_id);
