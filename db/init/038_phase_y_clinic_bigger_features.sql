-- Phase Y: clinic bigger features — messaging, homework, group sessions,
-- therapist matching, journaling, messaging plans, corporate packages.

-- پیام‌رسانیِ امنِ بینِ‌جلسه‌ای (مراجع <-> درمانگر)
CREATE TABLE IF NOT EXISTS clinic_messages (
    id BIGSERIAL PRIMARY KEY,
    therapist_id BIGINT NOT NULL REFERENCES therapists(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    sender_type VARCHAR(20) NOT NULL,
    body TEXT NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_clinic_messages_thread ON clinic_messages(therapist_id, user_id, created_at);

-- تکلیف/تمرینِ بینِ‌جلسه‌ای
CREATE TABLE IF NOT EXISTS clinic_homework (
    id BIGSERIAL PRIMARY KEY,
    therapist_id BIGINT NOT NULL REFERENCES therapists(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    due_date TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_clinic_homework_user ON clinic_homework(user_id, therapist_id);

-- یادداشتِ روزانه (ژورنال) با اشتراکِ اختیاری با درمانگر
CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    shared_with_therapist_id BIGINT REFERENCES therapists(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_journal_entries_user ON journal_entries(user_id, created_at);

-- پرسشنامه‌ی تطبیقِ درمانگر (ادمین‌مدیریت، بی‌حالت — بر اساسِ تگ امتیازدهی می‌شود)
CREATE TABLE IF NOT EXISTS therapist_match_questions (
    id BIGSERIAL PRIMARY KEY,
    question_text TEXT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

-- پلنِ اشتراکِ پیام‌رسانیِ نامحدود (خریداری‌شده از طریقِ محصولِ لینک‌شده به درمانگر)
CREATE TABLE IF NOT EXISTS messaging_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    therapist_id BIGINT NOT NULL REFERENCES therapists(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, therapist_id)
);

ALTER TABLE therapists ADD COLUMN IF NOT EXISTS messaging_product_id BIGINT;

-- جلسه‌ی گروهی: ظرفیت به‌جایِ باینریِ isBooked (سازگار به‌عقب، is_booked هم‌زمان نگه‌داشته می‌شود)
ALTER TABLE availability_slots ADD COLUMN IF NOT EXISTS capacity INT NOT NULL DEFAULT 1;
ALTER TABLE availability_slots ADD COLUMN IF NOT EXISTS booked_count INT NOT NULL DEFAULT 0;
UPDATE availability_slots SET booked_count = 1 WHERE is_booked = TRUE AND booked_count = 0;

-- بسته‌ی مشاوره‌ی سازمانی: سازمان (از فازِ W) صندلیِ جلسه برایِ یک درمانگرِ مشخص می‌خرد
CREATE TABLE IF NOT EXISTS clinic_organization_seats (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    therapist_id BIGINT NOT NULL REFERENCES therapists(id),
    session_count INT NOT NULL DEFAULT 1,
    assigned_user_id BIGINT,
    assigned_email VARCHAR(200),
    assigned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_clinic_org_seats_org ON clinic_organization_seats(organization_id, therapist_id);
