# 数据模型 - 实体设计规范

---

## 1. 基础实体

### 1.1 BaseEntity（抽象基类）

所有实体必须继承 BaseEntity：

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| createdAt | LocalDateTime | 创建时间 | 非空、创建时自动设置、不可更新 |
| updatedAt | LocalDateTime | 更新时间 | 可为空、更新时自动设置 |

**注解要求**:
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

---

## 2. 实体定义

### 2.1 User（用户实体）

**表名**: `users`

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| email | VARCHAR(255) | 非空、唯一 |
| nickname | VARCHAR(100) | 非空 |
| avatar | VARCHAR(500) | 可为空 |
| role | VARCHAR(20) | 非空 |
| status | VARCHAR(20) | 非空 |
| created_at | TIMESTAMP | 非空 |
| updated_at | TIMESTAMP | 可为空 |

**约束**:
- `role` 只能是 `STUDENT`, `TEACHER`, `ADMIN`
- `status` 只能是 `PENDING_APPROVAL`, `ACTIVE`, `DISABLED`

### 2.2 Course（课程实体）

**表名**: `courses`

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| educator_id | BIGINT | 非空，外键引用 `users(id)` |
| title | VARCHAR(200) | 非空 |
| description | TEXT | 可为空 |
| cover_image | VARCHAR(500) | 可为空 |
| status | VARCHAR(20) | 非空，默认 `DRAFT` |
| created_at | TIMESTAMP | 非空 |
| updated_at | TIMESTAMP | 可为空 |

**约束**:
- `status` 只能是 `DRAFT`, `PUBLISHED`
- `educator_id` 必须引用已存在的用户

**级联关系**:
- 与 Chapter: 一对多，父删除级联删除子

### 2.3 Chapter（章节实体）

**表名**: `chapters`

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| course_id | BIGINT | 非空，外键引用 `courses(id)`，删除级联 |
| title | VARCHAR(200) | 非空 |
| order_num | INT | 非空 |
| created_at | TIMESTAMP | 非空 |
| updated_at | TIMESTAMP | 可为空 |

**约束**:
- `order_num` 同一课程内必须唯一

### 2.4 Lesson（课时实体）

**表名**: `lessons`

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| chapter_id | BIGINT | 非空，外键引用 `chapters(id)`，删除级联 |
| title | VARCHAR(200) | 非空 |
| video_id | VARCHAR(100) | 可为空，腾讯云 VOD FileId |
| video_url | VARCHAR(500) | 可为空 |
| duration | INT | 可为空，视频时长（秒） |
| order_num | INT | 非空 |
| status | VARCHAR(20) | 非空，默认 `PROCESSING` |
| created_at | TIMESTAMP | 非空 |
| updated_at | TIMESTAMP | 可为空 |

**约束**:
- `status` 只能是 `PROCESSING`, `READY`, `FAILED`

### 2.5 Enrollment（选课实体）

**表名**: `enrollments`

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| user_id | BIGINT | 非空，外键引用 `users(id)` |
| course_id | BIGINT | 非空，外键引用 `courses(id)` |
| status | VARCHAR(20) | 非空 |
| expires_at | TIMESTAMP | 可为空，null 表示永久有效 |
| created_at | TIMESTAMP | 非空 |
| updated_at | TIMESTAMP | 可为空 |

**约束**:
- `status` 只能是 `ACTIVE`, `EXPIRED`
- `(user_id, course_id)` 组合必须唯一

### 2.6 RedeemCode（兑换码实体）

**表名**: `redeem_codes`

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| code | VARCHAR(20) | 非空、唯一 |
| course_id | BIGINT | 非空，外键引用 `courses(id)` |
| status | VARCHAR(20) | 非空，默认 `PENDING` |
| used_by | BIGINT | 可为空，外键引用 `users(id)` |
| used_at | TIMESTAMP | 可为空 |
| expires_at | TIMESTAMP | 非空 |
| course_expires_at | TIMESTAMP | 可为空 |
| created_by | BIGINT | 非空，外键引用 `users(id)` |
| created_at | TIMESTAMP | 非空 |
| updated_at | TIMESTAMP | 可为空 |

**约束**:
- `status` 只能是 `PENDING`, `REDEEMED`, `EXPIRED`
- `code` 必须唯一索引

---

## 3. 索引设计

### 3.1 主键索引

每个表都有主键索引 `PRIMARY KEY (id)`

### 3.2 唯一索引

| 表名 | 字段 | 说明 |
|------|------|------|
| users | email | 邮箱唯一 |
| redeem_codes | code | 兑换码唯一 |
| enrollments | (user_id, course_id) | 选课唯一 |

### 3.3 普通索引

| 表名 | 字段 | 说明 |
|------|------|------|
| courses | educator_id | 查询教师的课程 |
| chapters | course_id | 查询课程的章节 |
| lessons | chapter_id | 查询章节的课时 |
| enrollments | user_id | 查询用户的选课 |
| enrollments | course_id | 查询课程的选课 |
| redeem_codes | course_id | 查询课程的兑换码 |
| redeem_codes | created_by | 查询教师创建的兑换码 |

---

## 4. 实体关系图

```
┌─────────────┐       ┌─────────────┐
│    User     │       │   Course    │
├─────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)     │
│ email       │  │    │ educator_id │──┐
│ nickname    │  └───→│ title       │  │
│ role        │       │ status      │  │
│ status      │       └─────────────┘  │
└─────────────┘              │         │
       │                     │         │
       │              ┌──────┴──────┐   │
       │              │   Chapter   │   │
       │              ├─────────────┤   │
       │              │ id (PK)     │   │
       │              │ course_id   │←──┘
       │              │ title       │
       └──────────────│ order_num   │
                      └──────┬──────┘
                             │
                      ┌──────┴──────┐
                      │   Lesson    │
                      ├─────────────┤
                      │ id (PK)     │
                      │ chapter_id  │←──┐
                      │ title       │   │
                      │ video_id    │   │
                      │ status      │   │
                      └─────────────┘   │
                                          │
┌─────────────┐       ┌─────────────┐   │
│ RedeemCode  │       │ Enrollment  │   │
├─────────────┤       ├─────────────┤   │
│ id (PK)     │       │ id (PK)     │   │
│ code        │       │ user_id     │←──┘
│ course_id   │←──────│ course_id   │←──┘
│ status      │       │ status      │
│ used_by     │←──────│ expires_at  │
│ expires_at  │       └─────────────┘
└─────────────┘
```

---

## 5. 命名规范

### 5.1 表名命名

- 使用复数名词：`users`, `courses`, `chapters`
- 小写字母 + 下划线
- 避免缩写

### 5.2 列名命名

- 小写字母 + 下划线：`user_id`, `created_at`
- 外键列：`{entity}_id` 格式
- 时间列：`{action}_at` 格式

### 5.3 约束命名

| 约束类型 | 命名格式 | 示例 |
|----------|----------|------|
| 主键 | `pk_{table}` | `pk_users` |
| 唯一索引 | `uk_{table}_{column}` | `uk_users_email` |
| 普通索引 | `idx_{table}_{column}` | `idx_courses_educator_id` |
| 外键 | `fk_{table}_{ref_table}` | `fk_courses_educator_id` |
