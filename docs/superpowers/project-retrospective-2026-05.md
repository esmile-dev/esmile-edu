# esmile-edu 项目复盘

**复盘日期**: 2026-05-06
**项目阶段**: MVP 开发中
**参与人员**: 开发团队

---

## 1. 项目概述

### 1.1 项目定位
面向大学生的专业课程和技能培训教育平台，连接个人教育者和生源。

### 1.2 技术栈
- **前端**: Vue 3 + Vite + shadcn/ui + TypeScript
- **后端**: Spring Boot 3.x (Java 21) + Spring Data JPA
- **数据库**: PostgreSQL
- **视频托管**: 腾讯云 VOD
- **认证**: JWT

### 1.3 项目规模
- MVP 目标：1个月内上线，验证核心业务流程
- 团队：2开发 + 1测试

---

## 2. 会话历程回顾

### 2.1 主要工作阶段

#### 阶段一：项目初始化与环境搭建
- 搭建 Spring Boot 3.x 后端项目
- 搭建 Vue 3 + Vite + TypeScript 前端项目
- 配置 PostgreSQL 数据库连接
- 集成 Flyway 数据库迁移

#### 阶段二：核心功能开发
**已完成功能**:
- 用户系统（邮箱验证码登录、角色管理）
- 课程系统（CRUD、章节、课时、视频）
- 兑换码系统
- 管理员功能

#### 阶段三：视频播放功能
- 腾讯云 VOD 集成
- Key 防盗链签名 URL 生成
- TCPlayer 前端播放器集成
- 播放权限校验

#### 阶段四：调试与修复
- CORS 跨域问题修复
- Flyway 迁移错误排查
- 课程封面图片问题解决
- 测试数据准备

---

## 3. 成功经验总结

### 3.1 架构设计

#### ✅ 分层架构清晰
```
api → biz → module → common
```
- **优点**: 依赖方向单一，职责明确，易于测试和维护
- **可复用**: 该架构模式适用于类似的单体分层项目

#### ✅ 使用 Java Record 作为 DTO
```java
public record VideoPlaybackResponse(
    String playbackUrl,
    Integer duration,
    String coverImage
) {}
```
- **优点**: 不可变、简洁、内置 equals/hashCode/toString
- **可复用**: 建议作为项目标配

#### ✅ 前端类型定义与 API 响应一致
- 后端 Java record → 前端 TypeScript interface 保持一致
- 便于前后端联调

### 3.2 安全设计

#### ✅ IDOR 漏洞防护
- 所有资源访问通过 enrollment 验证用户权限
- 使用 `AuthContext.getCurrentUserId()` 获取当前用户，避免直接传参

#### ✅ 管理员接口授权校验
- 添加了管理员角色验证
- 防止普通用户访问管理端 API

### 3.3 开发流程

#### ✅ 使用 Plan Agent 规划复杂功能
- 视频播放功能实现前，通过 `planner` agent 制定了详细的 10 步计划
- 每个 Task 独立可验证

#### ✅ 善用并行 Agent 执行
- 对于独立的调研任务，使用 `Explore` agent 并行执行
- 节省时间，提高效率

### 3.4 视频播放方案

#### ✅ 签名 URL 方案
- 后端按需生成签名播放 URL
- 支持 Key 防盗链
- 权限校验在后端完成

#### ✅ TCPlayer 集成
- CDN 引入方式，无需构建
- videoId 模式支持后端动态获取播放 URL

---

## 4. 失败教训总结

### 4.1 Flyway 数据库迁移

#### ❌ 问题：迁移文件顺序混乱
```
V1__init_schema.sql    (users, courses, chapters, lessons, enrollments)
V2__add_verification_code.sql
V3__test_data.sql
```
- V3 中的表引用在 V1 中不存在（因为 V1 包含多张表）
- 导致 Flyway 报 "relation does not exist" 错误

#### ❌ 问题：测试数据依赖外部图片
```sql
INSERT INTO courses (cover_image) VALUES ('https://example.com/xxx.jpg');
```
- 使用了无效的外部 URL
- 导致课程封面无法展示

#### 🔧 根因分析
1. V1 初始化脚本过大，包含 6 张表的创建
2. 没有明确的数据模型版本管理
3. 测试数据使用了未验证的外部资源

#### ✅ 改进措施
1. **拆分迁移文件**: 每张表单独一个迁移文件
2. **验证外部资源**: 使用真实可访问的图片 URL 或本地资源
3. **迁移依赖注释**: 在 V3 开头注释说明依赖的表

### 4.2 CORS 配置问题

#### ❌ 问题：前端请求跨域失败
```
Access to fetch at 'http://localhost:10328/api/v1/student/courses'
from origin 'http://localhost:5173' has been blocked by CORS policy:
Cross-Origin-Embedding-Policy: strict-origin-when-cross-origin
```

#### 🔧 根因分析
- 后端 CORS 配置只允许了 `http://localhost:5173`
- 但浏览器的 `sec-fetch-site` 检查更严格
- 开发时前端 Vite 使用不同端口

#### ✅ 改进措施
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",  // Vite 默认
                "http://localhost:10328",  // 可能的其他端口
                "http://localhost:3000"    // 预留
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### 4.3 前端资源路径问题

#### ❌ 问题：课程封面无法展示
- 封面 URL 指向无效路径
- 没有配置静态资源服务

#### 🔧 根因分析
1. 没有统一的图片存储方案
2. 直接使用外部 URL 没有验证可用性

#### ✅ 改进措施（待实施）
- 方案A: 使用腾讯云 COS 存储图片
- 方案B: 使用公共图床服务（如 Imgur）
- 方案C: 自建图床服务
- 统一图片 URL 格式，配置静态资源映射

### 4.4 Mock 验证码日志问题

#### ❌ 问题：无法实时查看后端日志获取验证码
- 验证码通过 System.out.println 输出
- 开发时难以快速定位

#### 🔧 根因分析
- 使用 System.out 而非正式日志框架
- 日志级别未正确配置

#### ✅ 改进措施
```java
// 使用 Lombok @Slf4j
@Slf4j
@Service
public class EmailService {

    public void sendVerificationCode(String email, String code) {
        // 正确做法
        log.info("【DEV】验证码已发送至: {}, 验证码: {}", email, code);

        // 而非
        // System.out.println("验证码: " + code);
    }
}
```

---

## 5. 技术债务

### 5.1 已识别但未解决

| 债务项 | 影响 | 优先级 |
|--------|------|--------|
| 视频上传为 Mock 模式 | 无法真实上传视频 | P1 |
| 邮件发送为 Mock 模式 | 无法真实发送邮件 | P1 |
| JWT 密钥硬编码 | 安全风险 | P1 |
| API Key 未配置 | 外部系统无法调用 | P2 |
| 数据统计功能未实现 | 无法查看运营数据 | P2 |
| 缺少单元测试覆盖率 | 质量无保障 | P2 |

### 5.2 架构演进建议

| 阶段 | 触发条件 | 动作 |
|------|----------|------|
| Phase 1: MVP | 当前 | 单模块 + 分层包 |
| Phase 2: 增长期 | PMF 验证，团队扩展 | 抽取 biz 层为独立模块 |
| Phase 3: 规模化 | 团队 > 5 人 | 完整 DDD 分层 + 事件驱动 |

---

## 6. 流程改进建议

### 6.1 开发前

#### ✅ 必做项
1. **GitHub 代码搜索**: 在实现新功能前，先搜索现有实现和开源方案
2. **数据库迁移评审**: 多人 Review 迁移脚本，避免顺序问题
3. **外部资源验证**: 所有外部 URL 必须验证可用性后再使用

#### ❌ 避免项
1. 不要在迁移脚本中使用未经测试的外部资源
2. 不要创建超过 500 行的迁移文件
3. 不要跳过本地测试直接提交

### 6.2 开发中

#### ✅ 最佳实践
1. **小步提交**: 每个功能点单独提交，便于回溯
2. **持续集成**: 提交前运行编译检查
3. **日志规范**: 统一使用 Slf4j，日志级别正确

#### ❌ 常见陷阱
1. **过早优化**: 在 MVP 阶段追求完美的架构
2. **过度设计**: 为可能的未来需求添加复杂抽象
3. **复制粘贴**: 从其他项目复制代码而不理解其原理

### 6.3 调试阶段

#### ✅ 调试技巧
1. **查看后端日志**: 使用 `tail -f` 实时监控日志
2. **浏览器 DevTools**: Network 面板查看请求详情
3. **数据库直接查询**: 使用 psql 验证数据状态

#### ❌ 避免项
1. 不要猜测错误原因，使用日志和断点定位
2. 不要忽略警告信息
3. 不要在生产环境调试

---

## 7. 知识沉淀

### 7.1 项目规范清单

#### 代码规范
- [x] 使用有明确含义的变量名
- [x] 禁止魔法数字
- [x] 函数不超过 ~40 行
- [x] 删除死代码
- [x] 使用 Lombok 减少样板代码
- [x] 优先使用 immutable 对象

#### Git 规范
- [x] 禁止 `git add -A` 或 `git add .`
- [x] 提交前确认文件列表
- [x] 遵循 conventional commits
- [x] 禁止 force push 到 main/master

### 7.2 常见问题速查

| 问题 | 解决方案 |
|------|----------|
| Flyway "relation does not exist" | 检查迁移文件顺序，确保依赖的表已创建 |
| CORS 跨域错误 | 后端配置 allowedOrigins 包含前端端口 |
| 验证码无法收到 | 检查日志输出中的 Mock 验证码 |
| 课程封面不显示 | 验证图片 URL 是否可访问 |

### 7.3 工具链

| 场景 | 工具 |
|------|------|
| API 调试 | Postman / curl |
| 数据库查看 | pgAdmin / psql |
| 后端日志 | `cd backend && ./mvnw spring-boot:run` |
| 前端调试 | Vite Dev Server + Chrome DevTools |
| 视频播放测试 | TCPlayer Demo Page |

---

## 8. 下一步行动

### 8.1 立即行动（本周）

| 任务 | 负责人 | 状态 |
|------|--------|------|
| 解决课程封面图片问题 | 待认领 | ⬜ |
| 配置生产环境 JWT 密钥 | 待认领 | ⬜ |
| 验证腾讯云 VOD 真实上传 | 待认领 | ⬜ |

### 8.2 短期规划（2周内）

| 任务 | 说明 |
|------|------|
| 接入真实邮件服务 | 使用腾讯云邮件服务替换 Mock |
| 完善单元测试 | 覆盖率提升至 80% |
| 管理员数据统计 | 基础运营数据展示 |

### 8.3 中期规划（1个月）

| 任务 | 说明 |
|------|------|
| 生产环境部署 | Docker 容器化部署 |
| 监控告警 | 日志收集 + 异常告警 |
| 性能优化 | 数据库索引、缓存 |

---

## 9. 经验教训一句话

1. **迁移脚本要小而独立**，避免大文件导致的依赖混乱
2. **外部资源必须验证**，不要假设 URL 可用
3. **日志要用正式框架**，System.out 只适合临时调试
4. **CORS 配置要留有余地**，开发环境端口可能变化
5. **MVP 阶段避免过度工程**，简单方案先跑通

---

## 附录

### A. 相关文档

| 文档 | 路径 |
|------|------|
| MVP PRD | `docs/specs/2026-05-02-esmile-edu-mvp-prd.md` |
| 视频播放设计 | `docs/superpowers/specs/2026-05-05-video-playback-design.md` |
| 项目规范 | `CLAUDE.md` |

### B. 提交记录

```
c586f60 chore: add tencent.vod.signature-expire configuration
140472d feat(frontend): support videoId mode with TCPlayer
2aaa3c7 feat(frontend): add TCPlayer CDN dependencies to index.html
7fa018f feat(video): add GET /video/playback-url/{videoId} endpoint
9986073 feat(video): add getPlaybackUrl with enrollment permission check
```

### C. 复盘会议记录

（待补充）
