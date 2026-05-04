# 模块设计 - 索引

本目录包含三个核心业务模块的设计规范。

## 模块列表

| 模块 | 文件 | 描述 |
|------|------|------|
| 用户模块 | [user-module.md](user-module.md) | 用户注册、登录、认证、用户管理 |
| 课程模块 | [course-module.md](course-module.md) | 课程、章节、课时、选课、视频上传 |
| 兑换码模块 | [redeem-module.md](redeem-module.md) | 兑换码生成、兑换、状态管理 |

## 实现顺序

**推荐按以下顺序实现**：

1. **module 层** - 实体定义（user → course → redeem）
2. **biz 层** - 业务聚合服务
3. **api 层** - 接口定义
4. **dto 层** - 数据传输对象

## 包结构对应

```
module/user/     →  UserEntity, UserRepository
module/course/   →  CourseEntity, ChapterEntity, LessonEntity, EnrollmentEntity, *Repository
module/redeem/   →  RedeemCodeEntity, RedeemCodeRepository

biz/             →  UserBizService, CourseBizService, RedeemBizService
api/             →  UserController, CourseController, RedeemCodeController
dto/             →  *Request, *Response
```

## Agent 使用指南

### 1. 理解模块结构

AI Agent 应首先阅读：
- module 层的实体定义
- biz 层的业务聚合逻辑
- api 层的接口定义

### 2. 实现顺序

按 **module → biz → api → dto** 的顺序实现

### 3. 关键检查点

- [ ] module 层仅包含 Entity 和 Repository
- [ ] biz 层负责跨模块业务聚合
- [ ] api 层仅做参数校验，不含业务逻辑
- [ ] dto 层包含请求和响应对象
