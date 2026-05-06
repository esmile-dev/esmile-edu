# 兑换码模块设计规范

**模块**: esmile-edu-redeem
**依赖**: esmile-edu-common, esmile-edu-user, esmile-edu-course

---

## 1. Domain 层

### 1.1 枚举定义

#### RedeemCodeStatus（兑换码状态枚举）

| 枚举值 | 说明 |
|--------|------|
| `PENDING` | 待兑换 |
| `REDEEMED` | 已兑换 |
| `EXPIRED` | 已过期 |

### 1.2 值对象定义

#### RedeemCodeValue（兑换码值对象）

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空、8位、大写字母数字（A-Z, 0-9） |

**生成规则**: 随机生成 8 位大写字母数字组合，如 `A1B2C3D4`

### 1.3 聚合根 - RedeemCode

**字段列表**:

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | Long | 主键 | 自动生成 |
| code | RedeemCodeValue | 兑换码 | 非空、唯一 |
| courseId | Long | 课程ID | 非空，引用Course聚合 |
| status | RedeemCodeStatus | 状态 | 非空 |
| usedBy | Long | 使用者ID | 可为空，引用User聚合 |
| usedAt | LocalDateTime | 使用时间 | 可为空 |
| expiresAt | LocalDateTime | 兑换码有效期 | 非空（必须晚于当前时间） |
| courseExpiresAt | LocalDateTime | 兑换后课程权限期限 | 可为空（null表示永久） |
| createdBy | Long | 创建者ID | 非空，引用User聚合（教师） |
| createdAt | LocalDateTime | 创建时间 | 自动设置 |
| updatedAt | LocalDateTime | 更新时间 | 自动设置 |

**索引**: `code` 字段必须唯一索引

### 1.4 仓储接口 - RedeemCodeRepository

| 方法签名 | 说明 |
|----------|------|
| `Optional<RedeemCode> findById(Long id)` | 根据ID查询 |
| `Optional<RedeemCode> findByCode(String code)` | 根据兑换码查询 |
| `RedeemCode save(RedeemCode redeemCode)` | 保存兑换码 |
| `List<RedeemCode> findByCourseId(Long courseId)` | 查询课程的所有兑换码 |
| `List<RedeemCode> findByCreatedBy(Long educatorId)` | 查询教师创建的所有兑换码 |

### 1.5 业务规则

| 规则ID | 规则描述 |
|--------|----------|
| RDM-001 | 兑换码格式: 8位大写字母数字，如 `A1B2C3D4` |
| RDM-002 | 兑换码一次性使用，兑换后状态变为 `REDEEMED` |
| RDM-003 | 超过 `expiresAt` 未兑换，状态自动变为 `EXPIRED` |
| RDM-004 | 兑换时检查关联课程状态，课程不存在或未发布则拒绝 |
| RDM-005 | 兑换后创建 Enrollment，有效期由 `courseExpiresAt` 决定 |
| RDM-006 | 兑换码状态变更后不可逆（REDEEMED/EXPIRED） |
| RDM-007 | 外部系统调用仅能生成，不能兑换 |

---

## 2. Application 层

### 2.1 Driving Ports（用例接口）

#### RedeemCodeUseCase

| 方法签名 | 说明 |
|----------|------|
| `RedeemCode generateCode(Long educatorId, Long courseId, LocalDateTime expiresAt, LocalDateTime courseExpiresAt)` | 教师生成兑换码 |
| `RedeemResult redeemCode(String code, Long redeemerId)` | 学生兑换课程 |

#### RedeemCodeGenerationService（外部系统接口）

| 方法签名 | 说明 |
|----------|------|
| `List<String> generateForExternal(Long courseId, LocalDateTime expiresAt, LocalDateTime courseExpiresAt, int quantity)` | 外部系统批量生成兑换码 |

### 2.2 Command/Query DTO 定义

#### GenerateCodeCommand（生成兑换码命令）

| 字段 | 类型 | 校验 |
|------|------|------|
| courseId | Long | @NotNull |
| expiresAt | LocalDateTime | @NotNull, @Future |
| courseExpiresAt | LocalDateTime | 可为空 |

#### RedeemCodeRequest（兑换请求）

| 字段 | 类型 | 校验 |
|------|------|------|
| code | String | @NotBlank |

#### ExternalGenerateRequest（外部系统生成请求）

| 字段 | 类型 | 校验 |
|------|------|------|
| courseId | Long | @NotNull |
| expiresAt | LocalDateTime | @NotNull |
| courseExpiresAt | LocalDateTime | 可为空 |
| quantity | Integer | @NotNull, @Min(1), @Max(100) |

### 2.3 Response DTO 定义

#### RedeemCodeResponse

| 字段 | 类型 |
|------|------|
| id | Long |
| code | String |
| courseId | Long |
| status | RedeemCodeStatus |
| expiresAt | LocalDateTime |
| courseExpiresAt | LocalDateTime |
| createdAt | LocalDateTime |

#### RedeemResult（兑换结果）

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 是否成功 |
| courseId | Long | 课程ID |
| courseTitle | String | 课程标题 |
| enrollmentExpiresAt | LocalDateTime | 选课到期时间 |
| errorCode | String | 错误码（失败时） |
| errorMessage | String | 错误信息（失败时） |

#### ExternalGenerateResponse（外部系统生成响应）

| 字段 | 类型 |
|------|------|
| codes | List\<String> | 生成的兑换码列表 |

### 2.4 业务规则约束

| 约束 | 说明 |
|------|------|
| 兑换码生成权限 | 仅课程教师可以为自己创建的课程生成兑换码 |
| 兑换码兑换权限 | 任何学生/教师都可以兑换（需验证课程有效性） |
| 外部系统权限 | 外部系统仅能生成，不能兑换 |
| 兑换前验证 | 必须验证：兑换码存在、未过期、未使用、课程已发布 |
| 兑换后操作 | 兑换成功需创建 Enrollment |

---

## 3. Infrastructure 层

### 3.1 实现约束

| 约束 | 说明 |
|------|------|
| JPA 实现 | RedeemCodeRepository 必须使用 JPA 实现 |
| 唯一约束 | 数据库层 `code` 字段必须唯一 |
| 索引 | `courseId`, `createdBy` 字段需要索引 |

### 3.2 外部系统认证

#### API Key 认证

- 外部系统调用需携带 `X-API-Key` 请求头
- API Key 存储在环境变量或配置中心
- 验证失败返回 401 Unauthorized

---

## 4. API 层

### 4.1 Controller 端点列表

#### StudentRedeemController

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/student/codes/redeem` | 兑换课程 | Bearer Token |

#### ExternalRedeemController

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/redeem-codes/apply` | 批量生成兑换码 | API Key |

### 4.2 请求/响应格式说明

#### 学生兑换

- **Request Body**: `RedeemCodeRequest`
- **Response (成功)**: `ApiResponse<RedeemResult>` (200)
- **Response (失败)**: `ApiResponse<RedeemResult>` (400)

#### 外部系统生成

- **Request Header**: `X-API-Key: <api_key>`
- **Request Body**: `ExternalGenerateRequest`
- **Response**: `ApiResponse<ExternalGenerateResponse>`

### 4.3 状态码映射

| 业务结果 | HTTP 状态码 |
|----------|-------------|
| 兑换成功 | 200 |
| 兑换码不存在 | 400 |
| 兑换码已使用 | 400 |
| 兑换码已过期 | 400 |
| 课程不存在 | 400 |
| 课程未发布 | 400 |
| API Key 无效 | 401 |

---

## 5. 错误码

| 错误码 | 含义 |
|--------|------|
| 10301 | 兑换码不存在 |
| 10302 | 兑换码已过期 |
| 10303 | 兑换码已被使用 |
| 10304 | 兑换码关联课程不可用 |
| 10305 | 课程不存在 |
| 10306 | 课程未发布 |
| 10307 | API Key 无效 |
| 10308 | 超出最大生成数量 |

---

## 6. 与其他模块的交互

### 6.1 依赖的聚合

| 聚合 | 引用方式 | 交互说明 |
|------|----------|----------|
| User | userId (Long) | 验证兑换者身份、记录创建者 |
| Course | courseId (Long) | 验证课程存在和发布状态 |
| Enrollment | 跨模块调用 | 兑换成功后创建选课记录 |

### 6.2 跨模块通信

**兑换成功后同步**:
```
RedeemCode.redeem()
    → EnrollmentUseCase.enrollStudent()
    → 创建 Enrollment 记录
```

---

## 7. 验证清单

### Domain 层验证

- [ ] RedeemCode 聚合根字段完整
- [ ] 兑换码值对象校验规则正确（8位大写字母数字）
- [ ] 状态转换规则正确（REDEEMED/EXPIRED 不可逆）
- [ ] RedeemCodeRepository 方法签名正确

### Application 层验证

- [ ] RedeemCodeUseCase 处理生成和兑换逻辑
- [ ] 兑换前完整验证（存在性、过期、已使用、课程状态）
- [ ] 兑换成功后正确创建 Enrollment

### Infrastructure 层验证

- [ ] JPA Repository 实现 Repository 接口
- [ ] API Key 认证实现正确

### API 层验证

- [ ] Controller 路径与 API 规范一致
- [ ] 学生端和外部系统端正确分离
- [ ] 错误响应格式正确
