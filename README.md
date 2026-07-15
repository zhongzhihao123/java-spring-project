# ☕ Java Spring 微服务 — AI 平台业务后端

> 企业级 Java 微服务体系，承载业务逻辑（用户、商品、订单、通知），  
> 与 Python AI 服务（NLP / 推荐 / CV / MLOps）**分工协作，互补共存**。

---

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                       前端桌面 (:3000)                       │
│                 (Vue3 + qiankun)                             │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│               ☕ Java 统一网关 (:8000)                        │
│         Spring Cloud Gateway                                │
│  /api/users /api/business /api/nlp /api/cv /api/dbadmin...  │
└──┬───────┬───────┬───────┬───────┬───────┬───────┬──────────┘
   │       │       │       │       │       │       │
   ▼       ▼       ▼       ▼       ▼       ▼       ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌──────────┐
│User │ │Biz  │ │Notif│ │NLP  │ │Rec  │ │CV   │ │DB Admin  │
│Svc  │ │Svc  │ │Svc  │ │Svc  │ │Svc  │ │Svc  │ │(代理)    │
│:8101│ │:8102│ │:8103│ │:8001│ │:8002│ │:8003│ │          │
│Java │ │Java │ │Java │ │Python│ │Python│ │Python│ │:8005    │
│用户/│ │商品/│ │通知/│ │AI   │ │AI   │ │AI   │ │Python内  │
│认证 │ │订单 │ │消息 │ │问答 │ │推荐 │ │视觉 │ │部网关    │
│JWT  │ │     │ │MQ   │ │     │ │     │ │     │ │直连MySQL│
└─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └──────────┘
                                                           │
                                          ┌────────────────┘
                                          ▼
                          ┌──────────────────────────────┐
                          │   Infrastructure              │
                          │  MySQL(:3307) Redis(:6379)    │
                          │  RabbitMQ(:5672) MinIO(:9000) │
                          └──────────────────────────────┘

  注：Python AI 服务不走自己的网关，直接由 Java 网关代理。
  Python 内部网关 (:8005) 仅用于 DB Admin 和健康检查。

## 📁 项目结构

```
java-spring-project/
├── pom.xml                        # 父 POM (Spring Boot 3.2 + Spring Cloud 2023)
├── .mvn/wrapper/                  # Maven Wrapper (无需本地装 Maven)
├── docker-compose.yml             # Docker Compose 编排
├── common/                        # 共享库
│   ├── dto/                       #   ApiResponse, PageRequest, PageResponse
│   └── exception/                 #   BusinessException
├── gateway-service/               # API 网关 (:8000)
│   ├── config/                    #   CORS, 限流
│   └── filter/                    #   JWT 全局鉴权过滤器
├── user-service/                  # 用户服务 (:8101)
│   ├── entity/                    #   User
│   ├── repository/                #   UserRepository
│   ├── service/                   #   UserService (注册/登录/JWT)
│   ├── controller/                #   UserController
│   └── config/                    #   全局异常处理
├── business-service/              # 业务服务 (:8102)
│   ├── entity/                    #   Product, Order
│   ├── repository/                #   ProductRepository, OrderRepository
│   ├── service/                   #   BusinessService
│   ├── controller/                #   BusinessController
│   └── client/                    #   Feign 客户端 (调用通知服务)
└── notification-service/          # 通知服务 (:8103)
    ├── config/                    #   RabbitMQ 队列配置
    ├── listener/                  #   事件监听 (跨语言消费 Python 端消息)
    ├── service/                   #   站内信服务
    └── controller/                #   NotificationController
```

---

## 🚀 快速启动

### 前置条件

| 依赖 | 版本 | 说明 |
|------|------|------|
| Java | 21+ | `java -version` 确认 |
| MySQL | 8.0+ | 端口 3307 (与 AI-system 共用) |
| RabbitMQ | 3.x+ | 端口 5672 (与 AI-system 共用) |
| Redis | 7.x+ | 端口 6379 (与 AI-system 共用) |

### 本地开发

```bash
# 1. 使用 Maven Wrapper 编译（自动下载 Maven）
./mvnw clean install -DskipTests

# 2. 按顺序启动微服务

# 终端 1 — 用户服务
cd user-service && ../mvnw spring-boot:run

# 终端 2 — 业务服务
cd business-service && ../mvnw spring-boot:run

# 终端 3 — 通知服务
cd notification-service && ../mvnw spring-boot:run

# 终端 4 — 网关
cd gateway-service && ../mvnw spring-boot:run
```

### Docker 部署

```bash
docker compose up -d --build
```

---

## 📡 API 接口

### 网关 (`localhost:8000`) — 统一入口

| 路径 | 转发至 | 类型 | 说明 |
|------|--------|------|------|
| `/api/users/**` | user-service:8101 | Java | 用户注册/登录/信息 |
| `/api/business/**` | business-service:8102 | Java | 商品/订单 CRUD |
| `/api/notifications/**` | notification-service:8103 | Java | 通知发送/消息查询 |
| `/api/nlp/**` | nlp-service:8001 | Python AI | 知识库问答 |
| `/api/recommend/**` | recommend-service:8002 | Python AI | 智能推荐 |
| `/api/cv/**` | cv-service:8003 | Python AI | 计算机视觉 |
| `/api/mlops/**` | mlops-service:8004 | Python AI | 模型训练/监控 |
| `/api/dbadmin/**` | Python 内部网关:8005 | Python | 数据库管理 |
| `/api/health` | Python 内部网关:8005 | Python | 健康检查 |
| `/api/info` | Python 内部网关:8005 | Python | 服务信息 |

### 用户服务

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/users/register` | 注册 |
| POST | `/api/users/login` | 登录 |
| GET  | `/api/users/me` | 当前用户 |

### 业务服务

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/business/products` | 商品列表 |
| GET  | `/api/business/products/{id}` | 商品详情 |
| POST | `/api/business/products` | 创建商品 |
| PUT  | `/api/business/products/{id}` | 更新商品 |
| DELETE| `/api/business/products/{id}` | 删除商品 |
| GET  | `/api/business/orders` | 我的订单 |
| GET  | `/api/business/orders/{no}` | 订单详情 |
| POST | `/api/business/orders` | 创建订单 |

### 通知服务

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/notifications/send` | 发送通知 |
| GET  | `/api/notifications/messages` | 我的消息 |

---

## 🧩 核心特性

### JWT 鉴权
- 网关 `AuthGlobalFilter` 统一拦截所有请求
- 白名单路径（login / register / health）免鉴权
- 解析后 Token 中的 `userId`/`username`/`role` 通过请求头透传

### Feign 服务调用
- `business-service` 通过 `NotificationClient` 调用 `notification-service`
- 实现服务间 RPC，链路完整

### RabbitMQ 跨语言通信
- Java 端声明 `ai.events` Topic Exchange（与 Python 端同 Exchange）
- 消费 `notification.*` 路由的消息
- Python 端发事件 → Java 端收 → 触发送通知

### 统一响应格式
所有接口返回标准 `ApiResponse<T>` 结构：
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-07-15T12:00:00"
}
```

---

## 🔗 与 Python AI 系统的关系

| | Python AI 系统 | Java 业务系统 |
|------|-------------|-------------|
| **定位** | AI 能力：NLP / 推荐 / CV / MLOps | 业务能力：用户 / 商品 / 订单 / 通知 |
| **网关** | 内部网关 :8005（仅供 Java 调用） | **统一网关 :8000**（前端唯一入口） |
| **认证** | 已移除 | JWT Bearer（user-service 管理） |
| **数据库** | MySQL + Milvus（向量） | MySQL（同库，不同表） |
| **消息** | RabbitMQ 发送 AI 事件 | 消费事件触发通知 |
| **部署** | Docker Compose + K8s | Docker Compose |

**前端只需要记住一个端口 8000**。AI 服务和数据库管理路由由 Java 网关统一代理，对前端透明。
