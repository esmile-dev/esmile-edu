CREATE TABLE IF NOT EXISTS learning_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    watched_seconds INTEGER DEFAULT 0,
    is_completed BOOLEAN DEFAULT false,
    last_watched_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_learning_progress_user_course ON learning_progress(user_id, course_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_learning_progress_user_lesson ON learning_progress(user_id, lesson_id);

ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS current_lesson_id BIGINT;
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS progress_percent INTEGER DEFAULT 0;
