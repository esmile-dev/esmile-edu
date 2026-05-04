# 部署指南 - 构建与运行

---

## 1. 构建

### 1.1 构建命令

```bash
# 全量构建
mvn clean package

# 跳过测试
mvn clean package -DskipTests

# 指定环境打包
mvn clean package -Pprod -DskipTests
```

### 1.2 构建产物

| 产物 | 路径 | 说明 |
|------|------|------|
| JAR 文件 | `target/*.jar` | 可执行 JAR |
| 依赖 JAR | `target/lib/` | 依赖库 |

---

## 2. 运行

### 2.1 运行命令

```bash
# 开发环境
java -jar esmile-edu.jar

# 指定环境
java -jar esmile-edu.jar --spring.profiles.active=prod

# 指定端口
java -jar esmile-edu.jar --server.port=8081
```

### 2.2 运行参数

| 参数 | 说明 |
|------|------|
| `--server.port` | 服务端口 |
| `--spring.profiles.active` | 激活的环境 |
| `--spring.datasource.url` | 数据库 URL |
| `--jwt.secret` | JWT 密钥 |

---

## 3. Docker

### 3.1 构建镜像

```bash
docker build -t esmile-edu:latest .
```

### 3.2 运行容器

```bash
docker run -d \
  -p 8080:8080 \
  --name esmile-edu \
  esmile-edu:latest
```

### 3.3 Docker Compose

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=db
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      POSTGRES_DB: esmile_edu
      POSTGRES_USER: esmile_user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
```

---

## 4. 验证

### 4.1 接口验证

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# API 验证
curl http://localhost:8080/api/v1/student/auth/send-code \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

### 4.2 日志验证

```bash
# 查看启动日志
docker logs esmile-edu

# 查看实时日志
docker logs -f esmile-edu
```

---

## 5. 常用运维命令

| 命令 | 说明 |
|------|------|
| `jps -l` | 查看 Java 进程 |
| `kill -9 <pid>` | 强制终止进程 |
| `tail -f app.log` | 查看实时日志 |
| `curl localhost:8080/actuator/health` | 健康检查 |

---

## 6. 约束

| 约束 | 说明 |
|------|------|
| JDK 版本 | 必须使用 JDK 25 |
| 端口占用 | 确保端口未被占用 |
| 内存配置 | 生产环境建议至少 2GB 堆内存 |
| 日志目录 | 确保有足够磁盘空间 |
