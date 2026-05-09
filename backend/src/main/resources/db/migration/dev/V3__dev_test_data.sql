-- V3__dev_test_data.sql
-- Development sample data (Idempotent - safe to run multiple times)
-- Password for all users: password123 (BCrypt encoded)

-- ============================================================
-- Users: 1 admin, 3 teachers, 3 students
-- ============================================================
INSERT INTO users (email, password, nickname, avatar, role, status, created_at, updated_at) VALUES
  ('admin@esmile.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', NULL, 'ADMIN',   'ACTIVE', NOW() - INTERVAL '90 days', NOW()),
  ('zhang.wei@esmile.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张伟',     'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangwei',   'TEACHER', 'ACTIVE', NOW() - INTERVAL '80 days', NOW()),
  ('li.na@esmile.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李娜',     'https://api.dicebear.com/7.x/avataaars/svg?seed=lina',       'TEACHER', 'ACTIVE', NOW() - INTERVAL '70 days', NOW()),
  ('wang.qiang@esmile.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王强',     'https://api.dicebear.com/7.x/avataaars/svg?seed=wangqiang',  'TEACHER', 'ACTIVE', NOW() - INTERVAL '60 days', NOW()),
  ('student1@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵小明',    'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming',   'STUDENT', 'ACTIVE', NOW() - INTERVAL '50 days', NOW()),
  ('student2@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '刘小红',    'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaohong',   'STUDENT', 'ACTIVE', NOW() - INTERVAL '40 days', NOW()),
  ('student3@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陈小华',    'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaohua',    'STUDENT', 'ACTIVE', NOW() - INTERVAL '30 days', NOW())
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- 6 Courses (use explicit IDs for referential integrity)
-- ============================================================
INSERT INTO courses (id, title, description, educator_id, cover, status, published_at, created_at, updated_at) VALUES
  (1, 'Java 基础入门',
   '本课程面向零基础学员，系统讲解 Java 语言核心概念。从环境搭建到面向对象编程，再到常用集合框架，循序渐进地掌握 Java 开发基础。课程包含大量实战练习，帮助学员快速上手。',
   2, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800', 'PUBLISHED', NOW() - INTERVAL '60 days', NOW() - INTERVAL '65 days', NOW()),
  (2, 'Python 数据分析',
   '从 Python 基础语法到数据分析全流程，涵盖 NumPy、Pandas、Matplotlib 三大核心库。通过真实数据集案例，学习数据清洗、探索性分析和可视化展示，培养数据驱动决策能力。',
   3, 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=800', 'PUBLISHED', NOW() - INTERVAL '50 days', NOW() - INTERVAL '55 days', NOW()),
  (3, '前端开发实战',
   '全面学习现代前端开发技术栈，包括 HTML5、CSS3、JavaScript ES6+ 以及 Vue.js 框架。课程以项目驱动方式教学，最终完成一个完整的单页应用开发。',
   4, 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800', 'PUBLISHED', NOW() - INTERVAL '40 days', NOW() - INTERVAL '45 days', NOW()),
  (4, 'Spring Boot 微服务',
   '深入学习 Spring Boot 框架与微服务架构设计。涵盖 RESTful API、数据库集成、安全认证、服务注册与发现等核心主题，适合有 Java 基础的开发者进阶学习。',
   2, 'https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?w=800', 'PUBLISHED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '35 days', NOW()),
  (5, '人工智能导论',
   '系统介绍人工智能基础理论与应用实践。内容包括机器学习、深度学习、自然语言处理和计算机视觉入门，配合 Python 实战代码演示，帮助学员建立 AI 知识体系。',
   3, 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800', 'PUBLISHED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '25 days', NOW()),
  (6, '数据库设计',
   '讲解关系型数据库设计原理与实践，包括 ER 模型、范式理论、SQL 高级查询、索引优化与事务管理。课程正在制作中，敬请期待。',
   4, 'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800', 'DRAFT', NULL, NOW() - INTERVAL '10 days', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Chapters (explicit IDs)
-- ============================================================
-- Course 1: Java 基础入门
INSERT INTO chapters (id, title, course_id, position, created_at, updated_at) VALUES
  (1,  '第一章：Java 入门',      1, 1, NOW() - INTERVAL '64 days', NOW()),
  (2,  '第二章：面向对象编程',     1, 2, NOW() - INTERVAL '63 days', NOW()),
  (3,  '第三章：集合框架',        1, 3, NOW() - INTERVAL '62 days', NOW()),
-- Course 2: Python 数据分析
  (4,  '第一章：Python 基础',     2, 1, NOW() - INTERVAL '54 days', NOW()),
  (5,  '第二章：NumPy 与 Pandas', 2, 2, NOW() - INTERVAL '53 days', NOW()),
  (6,  '第三章：数据可视化',       2, 3, NOW() - INTERVAL '52 days', NOW()),
-- Course 3: 前端开发实战
  (7,  '第一章：HTML5 与 CSS3', 3, 1, NOW() - INTERVAL '44 days', NOW()),
  (8,  '第二章：JavaScript',    3, 2, NOW() - INTERVAL '43 days', NOW()),
  (9,  '第三章：Vue.js 实战',   3, 3, NOW() - INTERVAL '42 days', NOW()),
-- Course 4: Spring Boot 微服务
  (10, '第一章：Spring Boot 基础',  4, 1, NOW() - INTERVAL '34 days', NOW()),
  (11, '第二章：RESTful API 开发', 4, 2, NOW() - INTERVAL '33 days', NOW()),
-- Course 5: 人工智能导论
  (12, '第一章：AI 概述',         5, 1, NOW() - INTERVAL '24 days', NOW()),
  (13, '第二章：机器学习基础',     5, 2, NOW() - INTERVAL '23 days', NOW()),
-- Course 6: 数据库设计 (DRAFT)
  (14, '第一章：关系模型', 6, 1, NOW() - INTERVAL '9 days', NOW()),
  (15, '第二章：SQL 进阶', 6, 2, NOW() - INTERVAL '8 days', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Lessons (explicit IDs)
-- ============================================================
-- Course 1 lessons
INSERT INTO lessons (id, title, chapter_id, course_id, position, video_url, video_id, duration, status, created_at, updated_at) VALUES
  (1,  '1.1 Java 简介与环境搭建',  1, 1, 1, 'https://example.com/videos/java-intro.mp4',        'vid-j-001', 1800, 'READY', NOW() - INTERVAL '64 days', NOW()),
  (2,  '1.2 第一个 Java 程序',    1, 1, 2, 'https://example.com/videos/java-hello.mp4',         'vid-j-002', 2100, 'READY', NOW() - INTERVAL '64 days', NOW()),
  (3,  '1.3 变量与数据类型',      1, 1, 3, 'https://example.com/videos/java-variables.mp4',     'vid-j-003', 2400, 'READY', NOW() - INTERVAL '64 days', NOW()),
  (4,  '1.4 运算符与表达式',      1, 1, 4, 'https://example.com/videos/java-operators.mp4',     'vid-j-004', 1500, 'READY', NOW() - INTERVAL '64 days', NOW()),
  (5,  '2.1 类与对象',           2, 1, 1, 'https://example.com/videos/java-class.mp4',          'vid-j-005', 3000, 'READY', NOW() - INTERVAL '63 days', NOW()),
  (6,  '2.2 封装与构造器',        2, 1, 2, 'https://example.com/videos/java-encapsulation.mp4', 'vid-j-006', 2700, 'READY', NOW() - INTERVAL '63 days', NOW()),
  (7,  '2.3 继承与多态',          2, 1, 3, 'https://example.com/videos/java-inheritance.mp4',   'vid-j-007', 3300, 'READY', NOW() - INTERVAL '63 days', NOW()),
  (8,  '2.4 接口与抽象类',        2, 1, 4, 'https://example.com/videos/java-interface.mp4',     'vid-j-008', 2400, 'READY', NOW() - INTERVAL '63 days', NOW()),
  (9,  '3.1 List 集合',          3, 1, 1, 'https://example.com/videos/java-list.mp4',           'vid-j-009', 2700, 'READY', NOW() - INTERVAL '62 days', NOW()),
  (10, '3.2 Map 集合',           3, 1, 2, 'https://example.com/videos/java-map.mp4',            'vid-j-010', 3000, 'READY', NOW() - INTERVAL '62 days', NOW()),
  (11, '3.3 Set 集合与迭代器',    3, 1, 3, 'https://example.com/videos/java-set.mp4',            'vid-j-011', 2100, 'READY', NOW() - INTERVAL '62 days', NOW()),
-- Course 2 lessons
  (12, '1.1 Python 环境搭建',     4, 2, 1, 'https://example.com/videos/py-setup.mp4',      'vid-p-001', 1500, 'READY', NOW() - INTERVAL '54 days', NOW()),
  (13, '1.2 基本数据类型',         4, 2, 2, 'https://example.com/videos/py-types.mp4',      'vid-p-002', 2400, 'READY', NOW() - INTERVAL '54 days', NOW()),
  (14, '1.3 函数与模块',           4, 2, 3, 'https://example.com/videos/py-functions.mp4',  'vid-p-003', 2700, 'READY', NOW() - INTERVAL '54 days', NOW()),
  (15, '2.1 NumPy 数组操作',      5, 2, 1, 'https://example.com/videos/py-numpy.mp4',      'vid-p-004', 3000, 'READY', NOW() - INTERVAL '53 days', NOW()),
  (16, '2.2 Pandas DataFrame',   5, 2, 2, 'https://example.com/videos/py-pandas.mp4',      'vid-p-005', 3600, 'READY', NOW() - INTERVAL '53 days', NOW()),
  (17, '2.3 数据清洗技巧',         5, 2, 3, 'https://example.com/videos/py-cleaning.mp4',   'vid-p-006', 2700, 'READY', NOW() - INTERVAL '53 days', NOW()),
  (18, '3.1 Matplotlib 基础',     6, 2, 1, 'https://example.com/videos/py-matplotlib.mp4',  'vid-p-007', 2400, 'READY', NOW() - INTERVAL '52 days', NOW()),
  (19, '3.2 高级图表制作',         6, 2, 2, 'https://example.com/videos/py-charts.mp4',      'vid-p-008', 3000, 'READY', NOW() - INTERVAL '52 days', NOW()),
-- Course 3 lessons
  (20, '1.1 HTML5 语义化标签',    7, 3, 1, 'https://example.com/videos/fe-html.mp4',       'vid-f-001', 1800, 'READY', NOW() - INTERVAL '44 days', NOW()),
  (21, '1.2 CSS3 布局与动画',     7, 3, 2, 'https://example.com/videos/fe-css.mp4',        'vid-f-002', 2700, 'READY', NOW() - INTERVAL '44 days', NOW()),
  (22, '1.3 响应式设计',           7, 3, 3, 'https://example.com/videos/fe-responsive.mp4', 'vid-f-003', 2400, 'READY', NOW() - INTERVAL '44 days', NOW()),
  (23, '2.1 ES6+ 新特性',         8, 3, 1, 'https://example.com/videos/fe-es6.mp4',        'vid-f-004', 3000, 'READY', NOW() - INTERVAL '43 days', NOW()),
  (24, '2.2 异步编程与 Promise',   8, 3, 2, 'https://example.com/videos/fe-async.mp4',      'vid-f-005', 2700, 'READY', NOW() - INTERVAL '43 days', NOW()),
  (25, '2.3 DOM 操作与事件',       8, 3, 3, 'https://example.com/videos/fe-dom.mp4',        'vid-f-006', 2400, 'READY', NOW() - INTERVAL '43 days', NOW()),
  (26, '3.1 Vue.js 组件开发',     9, 3, 1, 'https://example.com/videos/fe-vue-comp.mp4',    'vid-f-007', 3600, 'READY', NOW() - INTERVAL '42 days', NOW()),
  (27, '3.2 Vue Router 与状态管理', 9, 3, 2, 'https://example.com/videos/fe-vue-router.mp4', 'vid-f-008', 3000, 'READY', NOW() - INTERVAL '42 days', NOW()),
  (28, '3.3 项目实战与部署',        9, 3, 3, 'https://example.com/videos/fe-deploy.mp4',     'vid-f-009', 3300, 'READY', NOW() - INTERVAL '42 days', NOW()),
-- Course 4 lessons
  (29, '1.1 Spring Boot 快速入门',  10, 4, 1, 'https://example.com/videos/sb-intro.mp4',    'vid-s-001', 2400, 'READY', NOW() - INTERVAL '34 days', NOW()),
  (30, '1.2 自动配置原理',          10, 4, 2, 'https://example.com/videos/sb-autoconf.mp4', 'vid-s-002', 3000, 'READY', NOW() - INTERVAL '34 days', NOW()),
  (31, '1.3 数据库集成 JPA',        10, 4, 3, 'https://example.com/videos/sb-jpa.mp4',     'vid-s-003', 3600, 'READY', NOW() - INTERVAL '34 days', NOW()),
  (32, '2.1 控制器与路由',          11, 4, 1, 'https://example.com/videos/sb-rest.mp4',     'vid-s-004', 2700, 'READY', NOW() - INTERVAL '33 days', NOW()),
  (33, '2.2 请求参数与响应处理',     11, 4, 2, 'https://example.com/videos/sb-params.mp4',  'vid-s-005', 2400, 'READY', NOW() - INTERVAL '33 days', NOW()),
  (34, '2.3 异常处理与校验',        11, 4, 3, 'https://example.com/videos/sb-error.mp4',    'vid-s-006', 2100, 'READY', NOW() - INTERVAL '33 days', NOW()),
-- Course 5 lessons
  (35, '1.1 人工智能发展简史',     12, 5, 1, 'https://example.com/videos/ai-history.mp4',   'vid-a-001', 1800, 'READY', NOW() - INTERVAL '24 days', NOW()),
  (36, '1.2 AI 应用场景概览',     12, 5, 2, 'https://example.com/videos/ai-apps.mp4',      'vid-a-002', 2400, 'READY', NOW() - INTERVAL '24 days', NOW()),
  (37, '1.3 Python 在 AI 中的应用', 12, 5, 3, 'https://example.com/videos/ai-python.mp4',  'vid-a-003', 2700, 'READY', NOW() - INTERVAL '24 days', NOW()),
  (38, '2.1 监督学习基础',         13, 5, 1, 'https://example.com/videos/ai-supervised.mp4', 'vid-a-004', 3000, 'READY', NOW() - INTERVAL '23 days', NOW()),
  (39, '2.2 模型评估与调优',       13, 5, 2, 'https://example.com/videos/ai-eval.mp4',       'vid-a-005', 3300, 'READY', NOW() - INTERVAL '23 days', NOW()),
-- Course 6 lessons (DRAFT, PROCESSING)
  (40, '1.1 ER 模型设计',       14, 6, 1, NULL, NULL, NULL, 'PROCESSING', NOW() - INTERVAL '9 days', NOW()),
  (41, '1.2 范式理论',           14, 6, 2, NULL, NULL, NULL, 'PROCESSING', NOW() - INTERVAL '9 days', NOW()),
  (42, '2.1 复杂查询与子查询',   15, 6, 1, NULL, NULL, NULL, 'PROCESSING', NOW() - INTERVAL '8 days', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Redeem Codes
-- ============================================================
-- Course 4 (Spring Boot) - 张伟(id=2) created
INSERT INTO redeem_codes (code, course_id, status, created_by, used_at, used_by, expires_at, course_expires_at, created_at, updated_at) VALUES
  ('SB2026A1', 4, 'REDEEMED', 2, NOW() - INTERVAL '15 days', 5, NOW() + INTERVAL '60 days', NOW() + INTERVAL '365 days', NOW() - INTERVAL '25 days', NOW() - INTERVAL '15 days'),
  ('SB2026A2', 4, 'PENDING',  2, NULL, NULL, NOW() + INTERVAL '60 days', NOW() + INTERVAL '365 days', NOW() - INTERVAL '25 days', NOW()),
  ('SB2026A3', 4, 'PENDING',  2, NULL, NULL, NOW() + INTERVAL '60 days', NOW() + INTERVAL '365 days', NOW() - INTERVAL '25 days', NOW()),
  ('SB2026X1', 4, 'EXPIRED',  2, NULL, NULL, NOW() - INTERVAL '5 days',  NOW() + INTERVAL '180 days', NOW() - INTERVAL '40 days', NOW())
ON CONFLICT (code) DO NOTHING;

-- Course 5 (AI) - 李娜(id=3) created
INSERT INTO redeem_codes (code, course_id, status, created_by, used_at, used_by, expires_at, course_expires_at, created_at, updated_at) VALUES
  ('AI2026B1', 5, 'REDEEMED', 3, NOW() - INTERVAL '10 days', 6, NOW() + INTERVAL '90 days', NOW() + INTERVAL '365 days', NOW() - INTERVAL '18 days', NOW() - INTERVAL '10 days'),
  ('AI2026B2', 5, 'PENDING',  3, NULL, NULL, NOW() + INTERVAL '90 days', NOW() + INTERVAL '365 days', NOW() - INTERVAL '18 days', NOW()),
  ('AI2026B3', 5, 'PENDING',  3, NULL, NULL, NOW() + INTERVAL '90 days', NOW() + INTERVAL '365 days', NOW() - INTERVAL '18 days', NOW())
ON CONFLICT (code) DO NOTHING;

-- Course 1 (Java) - 张伟(id=2) created
INSERT INTO redeem_codes (code, course_id, status, created_by, expires_at, course_expires_at, created_at, updated_at) VALUES
  ('JAVA2026',  1, 'PENDING', 2, NOW() + INTERVAL '30 days', NOW() + INTERVAL '180 days', NOW() - INTERVAL '5 days', NOW()),
  ('JAVAPLUS',  1, 'PENDING', 2, NOW() + INTERVAL '30 days', NOW() + INTERVAL '180 days', NOW() - INTERVAL '5 days', NOW())
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Enrollments
-- ============================================================
-- 赵小明(5) -> Course 4 (via redeem SB2026A1)
INSERT INTO enrollments (user_id, course_id, enrolled_at, status, expires_at, created_at, updated_at)
VALUES (5, 4, NOW() - INTERVAL '15 days', 'ACTIVE', NOW() + INTERVAL '350 days', NOW() - INTERVAL '15 days', NOW())
ON CONFLICT (user_id, course_id) DO NOTHING;

-- 刘小红(6) -> Course 5 (via redeem AI2026B1)
INSERT INTO enrollments (user_id, course_id, enrolled_at, status, expires_at, created_at, updated_at)
VALUES (6, 5, NOW() - INTERVAL '10 days', 'ACTIVE', NOW() + INTERVAL '355 days', NOW() - INTERVAL '10 days', NOW())
ON CONFLICT (user_id, course_id) DO NOTHING;

-- 赵小明(5) -> Course 1, Course 3
INSERT INTO enrollments (user_id, course_id, enrolled_at, status, expires_at, created_at, updated_at)
VALUES (5, 1, NOW() - INTERVAL '45 days', 'ACTIVE', NOW() + INTERVAL '320 days', NOW() - INTERVAL '45 days', NOW())
ON CONFLICT (user_id, course_id) DO NOTHING;
INSERT INTO enrollments (user_id, course_id, enrolled_at, status, expires_at, created_at, updated_at)
VALUES (5, 3, NOW() - INTERVAL '30 days', 'ACTIVE', NOW() + INTERVAL '335 days', NOW() - INTERVAL '30 days', NOW())
ON CONFLICT (user_id, course_id) DO NOTHING;

-- 刘小红(6) -> Course 2
INSERT INTO enrollments (user_id, course_id, enrolled_at, status, expires_at, created_at, updated_at)
VALUES (6, 2, NOW() - INTERVAL '35 days', 'ACTIVE', NOW() + INTERVAL '330 days', NOW() - INTERVAL '35 days', NOW())
ON CONFLICT (user_id, course_id) DO NOTHING;

-- 陈小华(7) -> Course 1, Course 2
INSERT INTO enrollments (user_id, course_id, enrolled_at, status, expires_at, created_at, updated_at)
VALUES (7, 1, NOW() - INTERVAL '20 days', 'ACTIVE', NOW() + INTERVAL '345 days', NOW() - INTERVAL '20 days', NOW())
ON CONFLICT (user_id, course_id) DO NOTHING;
INSERT INTO enrollments (user_id, course_id, enrolled_at, status, expires_at, created_at, updated_at)
VALUES (7, 2, NOW() - INTERVAL '15 days', 'ACTIVE', NOW() + INTERVAL '350 days', NOW() - INTERVAL '15 days', NOW())
ON CONFLICT (user_id, course_id) DO NOTHING;

-- ============================================================
-- Reset sequences to match explicit IDs
-- ============================================================
SELECT setval('courses_id_seq',  (SELECT COALESCE(MAX(id), 1) FROM courses));
SELECT setval('chapters_id_seq', (SELECT COALESCE(MAX(id), 1) FROM chapters));
SELECT setval('lessons_id_seq',  (SELECT COALESCE(MAX(id), 1) FROM lessons));
