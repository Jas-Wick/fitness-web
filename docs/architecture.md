# 系统架构设计

> 本文是《FitHub：基于 Spring Boot + Vue 的智能健身管理平台》的完整架构设计文档。实施约定（精炼版）见根目录 [CLAUDE.md](../CLAUDE.md)，数据库设计见 [database.md](database.md)。

## 1. 总体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    前端客户端 (Vue3 Web)                       │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────┐  │
│  │  Vue3 + TS    │  │  Element Plus │  │ Pinia / Router  │  │
│  │  视图层        │  │  UI 组件      │  │ 状态/路由        │  │
│  └───────┬───────┘  └───────────────┘  └─────────────────┘  │
│  ┌───────▼───────────────────────────────────────────────┐  │
│  │  Axios 请求封装 + 拦截器（注入 JWT / 统一错误处理）      │  │
│  └───────────────────────┬───────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────┘
                           │ HTTPS / JSON (RESTful)
┌──────────────────────────▼──────────────────────────────────┐
│                  后端 Spring Boot 3 单体应用                   │
│  ┌─────────┐   ┌──────────────┐   ┌──────────────────────┐  │
│  │ 安全层   │   │ Controller 层 │   │ 全局异常/参数校验      │  │
│  │ Spring  │→  │  (REST API)  │→  │ 统一返回 Result<T>     │  │
│  │ Security│   └──────┬───────┘   └──────────────────────┘  │
│  │ + JWT   │          │                                      │
│  └─────────┘   ┌──────▼───────┐   ┌──────────────────────┐  │
│                │ Service 层    │   │ AI 模块（预留接口）    │  │
│                │ 业务逻辑       │   │ AiService/Provider   │  │
│                └──────┬───────┘   └──────────────────────┘  │
│                ┌──────▼───────┐                              │
│                │ Mapper 层      │  MyBatis-Plus              │
│                └──────┬───────┘                              │
└───────────────────────┼─────────────────────────────────────┘
        ┌───────────────┼────────────────┐
   ┌────▼────┐    ┌─────▼─────┐    ┌─────▼─────┐
   │ MySQL 8 │    │  Redis 7  │    │ 文件存储   │
   │ 主数据库 │    │ 缓存/会话  │    │ 头像/图片  │
   └─────────┘    └───────────┘    └───────────┘
```

### 分层职责（高内聚低耦合）

| 层 | 职责 |
|---|---|
| `controller` | 只做参数接收/校验、调用 service、返回 Result，不写业务 |
| `service` / `service/impl` | 业务逻辑、事务边界、缓存编排 |
| `mapper` | 纯数据访问（MyBatis-Plus BaseMapper），复杂 SQL 走 XML |
| `entity` / `dto` / `vo` | 表映射 / 入参 / 出参，三者分离，禁止直接暴露 entity |
| `security` | 认证/授权独立成包 |
| `common` | 通用结果、异常、常量 |
| `ai` | 独立模块，与业务解耦，不写死具体大模型 |

### 关键技术决策

- **单体应用**（不做微服务）：满足"避免过度设计"，模块间用 Service 接口解耦，未来可拆
- **无物理外键**：遵循阿里规范，外键关系在应用层维护，用索引保证查询性能
- **JWT 无状态 + Redis 吊销**：access token 短时效，refresh token 存 Redis 支持登出/换发

## 2. 技术选型

| 层 | 选型 | 版本 | 选型理由 |
|---|---|---|---|
| 语言 | Java | 21 LTS | 长期支持、虚拟线程等新特性 |
| 框架 | Spring Boot | 3.3.x | 企业标准，Spring 6 生态 |
| Web | Spring MVC | 内置 | RESTful API |
| ORM | MyBatis-Plus | 3.5.7 | 单表 CRUD 零 SQL、逻辑删除、分页插件 |
| 数据库 | MySQL | 8.0+ | utf8mb4、成熟稳定 |
| 缓存 | Redis + Spring Data Redis | 7.x / Lettuce | 缓存/会话/token 吊销/限流 |
| 安全 | Spring Security + jjwt | Security 6 / jjwt 0.12 | 过滤器链鉴权、BCrypt、JWT 无状态 |
| 校验 | spring-boot-starter-validation | 内置 | jakarta validation 注解校验 |
| 工具 | Lombok | 内置 | 减少样板代码 |
| 日志 | SLF4J + Logback | 内置 | 分环境配置、滚动文件 |
| 接口文档 | springdoc-openapi | 2.x | Swagger UI，便于联调 |
| 文件存储 | 本地 + 抽象接口 | — | 默认本地磁盘，预留 MinIO/OSS |
| 前端框架 | Vue 3 + TypeScript | 3.4+ | 组合式 API、类型安全 |
| 构建 | Vite | 5.x | 快速开发与打包 |
| UI | Element Plus | 2.x | 成熟桌面风格组件库 |
| 状态/路由 | Pinia + Vue Router | — | 官方推荐 |
| HTTP | Axios | 1.x | 拦截器统一注入 token |
| 图表 | ECharts | 5.x | 趋势图、BMI、营养比例 |
| 部署 | Nginx + Docker | — | 前端静态托管 + `/api` 反向代理；docker-compose 编排 MySQL/Redis/后端/前端 |
| 测试 | spring-boot-starter-test | 内置 | 单元 + 集成测试 |

## 3. 项目目录结构（Monorepo）

```
fitness-web/                              # FitHub 项目根目录（仓库名沿用 fitness-web）
├── README.md
├── CLAUDE.md                             # 项目级实施说明书
├── docker-compose.yml                    # 本地 MySQL8 + Redis7
├── docs/                                 # 设计文档
├── server/                               # ── 后端 Spring Boot ──
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/fitness/
│       │   ├── FitnessApplication.java
│       │   ├── common/
│       │   │   ├── result/               # Result<T>、ResultCode、PageResult<T>
│       │   │   ├── exception/            # BusinessException、GlobalExceptionHandler
│       │   │   ├── constant/             # 常量/枚举
│       │   │   └── util/                 # 工具类（UserCodeGenerator 等）
│       │   ├── config/                   # MybatisPlus/Redis/Cors/OpenApi
│       │   ├── security/                 # JwtUtil、JwtAuthFilter、SecurityConfig、UserDetailsServiceImpl
│       │   ├── controller/               # auth/user/training/food/exercise/body/post/admin
│       │   ├── service/  + impl/
│       │   ├── mapper/
│       │   ├── entity/                   # 数据库实体
│       │   ├── dto/                      # 入参对象
│       │   ├── vo/                       # 出参对象
│       │   ├── ai/                       # AiService + provider/ + dto/
│       └── resources/
│           ├── application.yml / -dev / -prod
│           ├── logback-spring.xml
│           ├── mapper/                   # 复杂 SQL XML（可选）
│           └── db/ schema.sql
└── client/                               # ── 前端 Vue3 + Vite ──
    ├── package.json
    ├── vite.config.ts
    ├── nginx.conf + Dockerfile           # Web 部署（Nginx 静态托管 + 反向代理）
    └── src/ main.ts / App.vue / api / stores / router / types / views / components / utils
```

> 后端基包 `com.fitness`，artifactId `fitness-server`；前端 npm 包名 `fithub-client`。

## 4. Redis 缓存设计

### 缓存内容与 Key 设计

| Key 格式 | 类型 | 内容 | TTL | 失效/更新策略 |
|---|---|---|---|---|
| `fitness:user:info:{userId}` | String(JSON) | 用户信息 VO | 30min | 更新资料时删除；读时 Cache-Aside 回填 |
| `fitness:user:info:null:{userId}` | String | 空值标记（防穿透） | 1min | 查无此人时写空值 |
| `fitness:refresh:{userId}` | String | refresh token | 7d | 登录写入；登出/换发删除；滑动续期 |
| `fitness:blacklist:{jti}` | String | 已吊销 access token | 2h | 登出时写入 |
| `fitness:exercise:hot` | ZSet | 热门动作（score=view_count） | 10min | 定时刷新；浏览时 ZINCRBY |
| `fitness:post:hot` | ZSet | 热门帖子（score=综合热度） | 10min | 定时刷新；浏览/点赞时 ZINCRBY |
| `fitness:post:detail:{postId}` | String(JSON) | 帖子详情 | 5min | 更新时删除；读时回填 |
| `fitness:stats:site` | String(JSON) | 网站统计 | 5min | 定时重算 |
| `fitness:like:{userId}:{type}:{targetId}` | String | 点赞状态 | 永久 | 点赞写入、取消删除 |
| `fitness:login:fail:{username}` | String | 登录失败计数 | 15min | 防暴力破解限流 |

> 环境前缀：`fitness:dev:` / `fitness:prod:`（生产/开发隔离）。

### 缓存策略（四防 + 一致性）

- **读策略**：Cache-Aside（旁路）—— 命中返回；未命中查 DB → 回填 → 返回
- **防穿透**：空值缓存（1min）；高并发场景可加布隆过滤器
- **防击穿**：热点 key 加互斥锁 `setnx` 重建，或逻辑过期
- **防雪崩**：TTL 加随机偏移（±10%）
- **一致性**：先更新 DB 再删缓存（高并发写延迟双删）；计数类 `INCR/ZINCRBY` + 定时批量刷回 DB

## 5. REST API 概览

统一前缀 `/api`，返回 `Result<T>{code, message, data}`；分页返回 `PageResult<T>{records, total, page, size}`。

| 模块 | 路由 | 方法 | 说明 | 权限 |
|---|---|---|---|---|
| 认证 | `/api/auth/register` | POST | 注册 | 公开 |
| 认证 | `/api/auth/login` | POST | 登录 | 公开 |
| 认证 | `/api/auth/refresh` | POST | 刷新 token | 公开 |
| 认证 | `/api/auth/logout` | POST | 登出 | 登录 |
| 用户 | `/api/user/profile` | GET/PUT | 查看/修改资料 | 登录 |
| 用户 | `/api/user/password` | PUT | 修改密码 | 登录 |
| 用户 | `/api/user/avatar` | POST | 头像上传 | 登录 |
| 训练 | `/api/training/**` | CRUD | 打卡记录增删改查 | 登录 |
| 训练 | `/api/training/streak` | GET | 连续打卡天数 | 登录 |
| 训练 | `/api/training/calendar` | GET | 打卡日历 | 登录 |
| 饮食 | `/api/food/**` | CRUD | 饮食记录 | 登录 |
| 饮食 | `/api/food/stat` | GET | 每日/营养比例统计 | 登录 |
| 动作 | `/api/exercise/**` | GET | 列表/搜索/分类/详情 | 公开 |
| 动作 | `/api/exercise/hot` | GET | 热门动作 | 公开 |
| 身体 | `/api/body/**` | CRUD | 身体数据记录 | 登录 |
| 身体 | `/api/body/trend` | GET | 趋势数据 | 登录 |
| BMI | `/api/bmi/current` | GET | 当前 BMI 与评价 | 登录 |
| BMI | `/api/bmi/history` | GET | 历史 BMI 曲线 | 登录 |
| 论坛 | `/api/post/**` | CRUD | 帖子发布/列表/详情 | 登录 |
| 论坛 | `/api/post/{id}/like` | POST | 点赞/取消 | 登录 |
| 论坛 | `/api/post/{id}/favorite` | POST | 收藏/取消 | 登录 |
| 论坛 | `/api/comment/**` | CRUD | 评论 | 登录 |
| 管理 | `/api/admin/exercise/**` | CRUD | 动作库管理 | ADMIN |
| 管理 | `/api/admin/user/**` | GET | 用户管理 | ADMIN |

## 6. AI 扩展预留（不绑定供应商）

按"暂不确定"决策，只定义抽象接口 + DTO，不实现具体 Provider：

```java
// 业务门面：按能力拆多个接口，互不耦合
public interface AiService {
    TrainingPlan generateTrainingPlan(TrainingPlanRequest req);  // AI 生成训练计划
    DietAnalysis   analyzeDiet(DietAnalysisRequest req);         // AI 饮食分析
    ExerciseCorrect correctExercise(ExerciseCorrectRequest req); // AI 动作纠正
    String         chat(QaRequest req);                          // AI 健身问答
}

// Provider SPI：第三方大模型统一抽象，未来可插拔
public interface AiProvider {
    String chat(ChatRequest request);   // 统一对话协议
    String name();                      // provider 标识
}
```

- 配置驱动：`ai.provider`、`ai.api-key`、`ai.base-url` 走 `application.yml` + 环境变量
- HTTP 调用用 Spring `RestClient`/`WebClient`，不直接依赖厂商 SDK
- `ai/` 模块与 `service` 解耦，业务依赖 `AiService` 接口而非具体实现，新增供应商只需加一个 `AiProvider` 实现类
