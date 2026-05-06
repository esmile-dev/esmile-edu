# 数据模型 - PostgreSQL DDL 规范

---

## 1. DDL 脚本结构

```sql
-- ============================================
-- esmile-edu 数据库 Schema
-- ============================================

-- 1. 通用部分
-- 2. 用户模块
-- 3. 课程模块
-- 4. 兑换码模块
-- ============================================
```

---

## 2. 表创建顺序

```
1. users                    -- 用户表（无依赖）
2. courses                 -- 课程表（依赖 users）
3. chapters                -- 章节表（依赖 courses）
4. lessons                 -- 课时表（依赖 chapters）
5. enrollments             -- 选课表（依赖 users, courses）
6. redeem_codes             -- 兑换码表（依赖 users, courses）
```

---

## 3. DDL 脚本

### 3.1 用户表

```sql
-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    avatar VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 唯一约束
CREATE UNIQUE INDEX uk_users_email ON users(email);

-- 约束条件
ALTER TABLE users ADD CONSTRAINT chk_users_role
    CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));

ALTER TABLE users ADD CONSTRAINT chk_users_status
    CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'DISABLED'));
```

### 3.2 课程表

```sql
-- 课程表
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    educator_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    cover_image VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 外键约束
ALTER TABLE courses ADD CONSTRAINT fk_courses_educator_id
    FOREIGN KEY (educator_id) REFERENCES users(id);

-- 索引
CREATE INDEX idx_courses_educator_id ON courses(educator_id);
CREATE INDEX idx_courses_status ON courses(status);

-- 约束条件
ALTER TABLE courses ADD CONSTRAINT chk_courses_status
    CHECK (status IN ('DRAFT', 'PUBLISHED'));
```

### 3.3 章节表

```sql
-- 章节表
CREATE TABLE chapters (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    order_num INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 外键约束
ALTER TABLE chapters ADD CONSTRAINT fk_chapters_course_id
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;

-- 索引
CREATE INDEX idx_chapters_course_id ON chapters(course_id);

-- 唯一约束（同一课程内排序号唯一）
CREATE UNIQUE INDEX uk_chapters_course_order ON chapters(course_id, order_num);
```

### 3.4 课时表

```sql
-- 课时表
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    video_id VARCHAR(100),
    video_url VARCHAR(500),
    duration INT,
    order_num INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 外键约束
ALTER TABLE lessons ADD CONSTRAINT fk_lessons_chapter_id
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE;

-- 索引
CREATE INDEX idx_lessons_chapter_id ON lessons(chapter_id);

-- 唯一约束（同一章节内排序号唯一）
CREATE UNIQUE INDEX uk_lessons_chapter_order ON lessons(chapter_id, order_num);

-- 约束条件
ALTER TABLE lessons ADD CONSTRAINT chk_lessons_status
    CHECK (status IN ('PROCESSING', 'READY', 'FAILED'));
```

### 3.5 选课表

```sql
-- 选课表
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 外键约束
ALTER TABLE enrollments ADD CONSTRAINT fk_enrollments_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE enrollments ADD CONSTRAINT fk_enrollments_course_id
    FOREIGN KEY (course_id) REFERENCES courses(id);

-- 唯一约束（防止重复选课）
CREATE UNIQUE INDEX uk_enrollments_user_course ON enrollments(user_id, course_id);

-- 索引
CREATE INDEX idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);

-- 约束条件
ALTER TABLE enrollments ADD CONSTRAINT chk_enrollments_status
    CHECK (status IN ('ACTIVE', 'EXPIRED'));
```

### 3.6 兑换码表

```sql
-- 兑换码表
CREATE TABLE redeem_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    used_by BIGINT,
    used_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    course_expires_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 外键约束
ALTER TABLE redeem_codes ADD CONSTRAINT fk_redeem_codes_course_id
    FOREIGN KEY (course_id) REFERENCES courses(id);

ALTER TABLE redeem_codes ADD CONSTRAINT fk_redeem_codes_used_by
    FOREIGN KEY (used_by) REFERENCES users(id);

ALTER TABLE redeem_codes ADD CONSTRAINT fk_redeem_codes_created_by
    FOREIGN KEY (created_by) REFERENCES users(id);

-- 唯一约束
CREATE UNIQUE INDEX uk_redeem_codes_code ON redeem_codes(code);

-- 索引
CREATE INDEX idx_redeem_codes_course_id ON redeem_codes(course_id);
CREATE INDEX idx_redeem_codes_created_by ON redeem_codes(created_by);
CREATE INDEX idx_redeem_codes_status ON redeem_codes(status);

-- 约束条件
ALTER TABLE redeem_codes ADD CONSTRAINT chk_redeem_codes_status
    CHECK (status IN ('PENDING', 'REDEEMED', 'EXPIRED'));
```

---

## 4. 审计字段自动更新触发器

```sql
-- 更新时间戳触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为需要审计的表创建触发器
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_courses_updated_at
    BEFORE UPDATE ON courses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chapters_updated_at
    BEFORE UPDATE ON chapters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lessons_updated_at
    BEFORE UPDATE ON lessons
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enrollments_updated_at
    BEFORE UPDATE ON enrollments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_redeem_codes_updated_at
    BEFORE UPDATE ON redeem_codes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

## 5. 初始数据

```sql
-- 创建管理员账户（密码需要单独设置）
INSERT INTO users (email, nickname, role, status)
VALUES ('admin@esmile.edu', 'Administrator', 'ADMIN', 'ACTIVE');
```

---

## 6. 数据库配置建议

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| `timezone` | `UTC+8` | 中国时区 |
| `standard_conforming_strings` | `on` | 启用标准 SQL 字符串 |
| `max_connections` | `100` | 根据应用需求调整 |
