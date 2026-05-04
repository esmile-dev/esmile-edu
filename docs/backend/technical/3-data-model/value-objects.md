# 数据模型 - 值对象设计规范

---

## 1. 值对象概述

### 1.1 值对象特征

| 特征 | 说明 |
|------|------|
| 不可变 | 创建后不能修改 |
| 按值比较 | 相等性基于属性值，而非引用 |
| 无唯一标识 | 没有身份概念 |
| 描述领域概念 | 用于描述领域的无标识概念 |

### 1.2 值对象使用场景

- 邮箱、手机号等标识符
- 金额、数量等度量值
- 地址、坐标等组合值
- 任何不需要身份但需要校验的概念

---

## 2. 值对象定义

### 2.1 Email（邮箱值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空、包含 `@` 和 `.` |

**校验实现要求**:
- 构造函数中校验格式
- 校验失败抛出 `IllegalArgumentException`
- 提供静态工厂方法 `of(String value)`

### 2.2 Nickname（昵称值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空、1-100 字符 |

### 2.3 AvatarUrl（头像URL值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 可为空、若非空必须以 `http` 或 `https` 开头 |

### 2.4 Title（标题值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空、1-200 字符 |

### 2.5 Description（描述值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 可为空、最大 5000 字符 |

### 2.6 CoverImageUrl（封面图URL值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 可为空、若非空必须以 `http` 或 `https` 开头 |

### 2.7 RedeemCodeValue（兑换码值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空、恰好 8 字符、仅大写字母(A-Z)和数字(0-9) |

**正则表达式**: `^[A-Z0-9]{8}$`

**生成规则**:
- 使用安全随机数生成
- 从 `[A-Z0-9]` 字符集中随机选取 8 个字符
- 示例: `A1B2C3D4`, `X9Y8Z7W6`

### 2.8 Duration（视频时长值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| seconds | Integer | 非空、非负整数 |

**辅助方法**:
- `toDisplayString()`: 转换为 `HH:mm:ss` 格式
- `toMinutes()`: 转换为分钟数

### 2.9 VideoId（腾讯云VOD VideoId值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空 |

### 2.10 VideoUrl（视频播放URL值对象）

| 属性 | 类型 | 校验规则 |
|------|------|----------|
| value | String | 非空、以 `http` 或 `https` 开头 |

---

## 3. 值对象与实体的映射

### 3.1 嵌入方式

值对象在 JPA 中使用 `@Embedded` 注解：

```java
@Embeddable
public class EmailAttribute {
    @Column(name = "email")
    private String value;
}
```

### 3.2 各实体中的值对象使用

| 实体 | 值对象 | 字段名 |
|------|--------|--------|
| User | Email | email |
| User | Nickname | nickname |
| User | AvatarUrl | avatar |
| Course | Title | title |
| Course | Description | description |
| Course | CoverImageUrl | coverImage |
| Lesson | Title | title |
| Lesson | VideoId | videoId |
| Lesson | VideoUrl | videoUrl |
| Lesson | Duration | duration |
| RedeemCode | RedeemCodeValue | code |

---

## 4. 值对象设计约束

| 约束 | 说明 |
|------|------|
| 不可变性 | 值对象必须是不可变的（final 字段，无 setter） |
| 构造校验 | 构造函数中完成所有校验，校验失败抛出异常 |
| equals/hashCode | 按值实现，用于集合去重和比较 |
| toString | 提供有意义的字符串表示 |

---

## 5. 常见错误

| 错误 | 正确做法 |
|------|----------|
| 在值对象中存储可空字段未处理 | 使用空对象模式或 Optional |
| 值对象引用实体 | 值对象只能引用其他值对象 |
| 值对象有状态变化方法 | 设计为不可变，提供转换方法 |
| 在 JPA 中使用 Optional\<ValueObject> | JPA 不支持 Optional 嵌入对象，直接使用值对象类型 |
